package Chess;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProfilePage {

    public ListView pastGamesList;
    public Label usernameLab,statusLab,ratingLab,winsLab,lossesLab,drawsLab;
    public ChoiceBox playersCBox;
    private int currentPlayerID;
    private int loggedInID;
    PreparedStatement getPastGames,getPlayerStats,getPlayers;
    private Tab currentTab;

    public void setCurrentTab(Tab currentTab) {
        this.currentTab = currentTab;
    }

    public void setPlayer(int currentPlayer,int loggedInID){

        pastGamesList.setStyle("-fx-background-color: white;");
        this.currentPlayerID=currentPlayer;
        this.loggedInID=loggedInID;

        Connection conn=ConnectionDAO.getConn();
        try {
            getPastGames=conn.prepareStatement("Select * from pastgames where white=? or black=?");
            getPastGames.setInt(1,currentPlayer);
            getPastGames.setInt(2,currentPlayer);

            getPlayerStats=conn.prepareStatement("Select * from player where id=?");
            getPlayerStats.setInt(1,currentPlayer);

            getPlayers=conn.prepareStatement("Select id,username,rating from player");

        } catch (SQLException e) {
            e.printStackTrace();
        }


        new Thread(()->{
            while (true) {
                try {
                    Platform.runLater(()->refresh());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public ProfilePage() {


    }


    private ResultSet getPlayerStats(int id){

        ResultSet rs=null;
        Connection conn=ConnectionDAO.getConn();
        try {
            PreparedStatement ps=conn.prepareStatement("Select * from player where id=? limit 1");
            ps.setInt(1,id);
             rs=ps.executeQuery();
             rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }



    private void refresh(){

        boolean show=playersCBox.isShowing();

       Connection conn=ConnectionDAO.getConn();
       Object o=(Object)pastGamesList.getSelectionModel().getSelectedItem();

       pastGamesList.getItems().clear();
       

       //region PastGames
        try {
            getPlayerStats.setInt(1,currentPlayerID);
            ResultSet rs=getPastGames.executeQuery();

            while(rs.next()){
               String s=(rs.getInt(1)+" :White: "+getPlayerStats(rs.getInt(2)).getString(2)+
                       " :Black: "+getPlayerStats(rs.getInt(3)).getString(2)+
                       " :Winner: "+getPlayerStats(rs.getInt(4)).getString(2)+
                       " :Date: "+rs.getString(6));
               pastGamesList.getItems().add(s);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(o!=null)pastGamesList.getSelectionModel().select(o);
        //endregion


        o=(Object)playersCBox.getSelectionModel().getSelectedItem();
        playersCBox.getItems().clear();

        //region Players

        try {

            ResultSet rs=getPlayers.executeQuery();

            while(rs.next()){
               playersCBox.getItems().add(rs.getInt(1)+" : "+rs.getString(2)+" : "+rs.getInt(3));
               if(rs.getInt(1)==currentPlayerID && playersCBox.getSelectionModel().getSelectedItem()==null)
                   playersCBox.getSelectionModel().selectLast();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(o!=null)pastGamesList.getSelectionModel().select(o);

        //endregion

        //region Labels
        try {
        //getPlayerStats.setInt(1,currentPlayerID);
        usernameLab.setText("Username:"+getPlayerStats(currentPlayerID).getString(2));
        winsLab.setText("Wins:"+getPlayerStats(currentPlayerID).getInt(4));
        lossesLab.setText("Losses:"+getPlayerStats(currentPlayerID).getInt(5));
        drawsLab.setText("Draws:"+getPlayerStats(currentPlayerID).getInt(6));
        ratingLab.setText("Rating:"+getPlayerStats(currentPlayerID).getInt(7));
        if(getPlayerStats(currentPlayerID).getInt(8)==0){
            statusLab.setText("Offline");
            statusLab.setStyle("-fx-background-color:red;");
        }
        else {
            statusLab.setText("Online");
            statusLab.setStyle("-fx-background-color:green;");
        }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //endregion

        if(show)playersCBox.show();

    }


    public void playerChangedClicked(MouseEvent mouseEvent) {


        if(playersCBox.getSelectionModel().getSelectedItem()!=null) {
            String s = playersCBox.getSelectionModel().getSelectedItem().toString();
            s = s.substring(0, s.indexOf(":") - 1);
            if(Integer.parseInt(s)!=currentPlayerID){
                currentPlayerID=Integer.parseInt(s);
                System.out.println("Changed to " + s);
                refresh();
            }
        }

    }
}
