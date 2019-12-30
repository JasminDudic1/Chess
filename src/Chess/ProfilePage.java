package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProfilePage {

    public ListView pastGamesList;
    public Label usernameLab, statusLab, ratingLab, winsLab, lossesLab, drawsLab;
    public ChoiceBox playersCBox;
    PreparedStatement getPastGames, getPlayerStats, getPlayers, getMoves,getWhiteBlackFromPastGames;
    private int currentPlayerID;
    private int loggedInID;
    private Tab currentTab;

    public ProfilePage() {

    }

    public void setCurrentTab(Tab currentTab) {
        this.currentTab = currentTab;
    }

    public void setPlayer(int currentPlayer, int loggedInID) {

        pastGamesList.setStyle("-fx-background-color: white;");
        this.currentPlayerID = currentPlayer;
        this.loggedInID = loggedInID;

        Connection conn = ConnectionDAO.getConn();
        try {
            getPastGames = conn.prepareStatement("Select * from pastgames where white=? or black=?");
            getPastGames.setInt(1, currentPlayer);
            getPastGames.setInt(2, currentPlayer);

            getPlayerStats = conn.prepareStatement("Select * from player where id=?");
            getPlayerStats.setInt(1, currentPlayer);

            getPlayers = conn.prepareStatement("Select id,username,rating from player");

            getMoves = conn.prepareStatement("Select moves from pastgames where id=?");

            getWhiteBlackFromPastGames=conn.prepareStatement("Select white,black from pastgames where id=?");

        } catch (SQLException e) {
            e.printStackTrace();
        }


        new Thread(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> refresh());
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private ResultSet getPlayerStats(int id) {

        ResultSet rs = null;
        Connection conn = ConnectionDAO.getConn();
        try {
            PreparedStatement ps = conn.prepareStatement("Select * from player where id=? limit 1");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if(rs.next()) return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void refresh() {

        boolean show = playersCBox.isShowing();

        Connection conn = ConnectionDAO.getConn();
        Object o = (Object) pastGamesList.getSelectionModel().getSelectedItem();

        pastGamesList.getItems().clear();


        //region PastGames
        try {
            getPlayerStats.setInt(1, currentPlayerID);
            ResultSet rs = getPastGames.executeQuery();

            while (rs.next()) {
                String s=rs.getInt(1)+" :White: ";
                if(getPlayerStats(rs.getInt(2))==null)s+="Rufus";
                else s+=getPlayerStats(rs.getInt(2)).getString(2);

                s+=" :Black: ";
                if(getPlayerStats(rs.getInt(3))==null)s+="Dufus";
                else s+=getPlayerStats(rs.getInt(3)).getString(2);

                s+=" :Winner: ";
                if(getPlayerStats(rs.getInt(4))==null){
                    if(rs.getInt(2)==rs.getInt(4))s+="Rufus";
                    else s+="Dufus";
                }
                else s+=getPlayerStats(rs.getInt(4)).getString(2);

                s+=" :Date: "+rs.getInt(6);


                /*String s = (rs.getInt(1) + " :White: " + getPlayerStats(rs.getInt(2)).getString(2) +
                        " :Black: " + getPlayerStats(rs.getInt(3)).getString(2) +
                        " :Winner: " + getPlayerStats(rs.getInt(4)).getString(2) +
                        " :Date: " + rs.getString(6));*/

                pastGamesList.getItems().add(s);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (o != null) pastGamesList.getSelectionModel().select(o);
        //endregion


        o = (Object) playersCBox.getSelectionModel().getSelectedItem();
        playersCBox.getItems().clear();

        //region Players

        try {

            ResultSet rs = getPlayers.executeQuery();

            while (rs.next())
                playersCBox.getItems().add(rs.getInt(1) + " : " + rs.getString(2) + " : " + rs.getInt(3));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (o != null) playersCBox.getSelectionModel().select(o);

        //endregion

        //region Labels
        try {
            //getPlayerStats.setInt(1,currentPlayerID);
            usernameLab.setText("Username:" + getPlayerStats(currentPlayerID).getString(2));
            winsLab.setText("Wins:" + getPlayerStats(currentPlayerID).getInt(4));
            lossesLab.setText("Losses:" + getPlayerStats(currentPlayerID).getInt(5));
            drawsLab.setText("Draws:" + getPlayerStats(currentPlayerID).getInt(6));
            ratingLab.setText("Rating:" + getPlayerStats(currentPlayerID).getInt(7));
            if (getPlayerStats(currentPlayerID).getInt(8) == 0) {
                statusLab.setText("Offline");
                statusLab.setStyle("-fx-background-color:red;");
            } else {
                statusLab.setText("Online");
                statusLab.setStyle("-fx-background-color:green;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //endregion

        if (show) playersCBox.show();

    }

    public void playerChangedClicked(ActionEvent actionEvent) {

        if (playersCBox.getSelectionModel().getSelectedItem() != null) {
            String s = playersCBox.getSelectionModel().getSelectedItem().toString();
            s = s.substring(0, s.indexOf(":") - 1);
            if (Integer.parseInt(s) != currentPlayerID) {
                currentPlayerID = Integer.parseInt(s);
                refresh();
            }
        }

    }

    public void pastGameClicked(MouseEvent mouseEvent) {

        if (pastGamesList.getSelectionModel().getSelectedItem() != null) {

            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
            a.setContentText("Do you want to watch this replay?");
            a.showAndWait();

            if (a.getResult() == ButtonType.YES) {

                String s = pastGamesList.getSelectionModel().getSelectedItem().toString();
                s = s.substring(0, s.indexOf(":") - 1);
                System.out.println("Id je "+s+"|");

                try {

                    int id = Integer.parseInt(s);
                    //region GetMoves
                    getMoves.setInt(1, id);
                    ResultSet rs = getMoves.executeQuery();
                    if (!rs.next()) {
                        System.out.println("Nema1");
                        refresh();
                        return;
                    }
                    String moves = rs.getString(1);
                    rs.close();
                    //endregion

                    //region GetWhiteAndBlack
                    getWhiteBlackFromPastGames.setInt(1,id);
                    rs=getWhiteBlackFromPastGames.executeQuery();
                    if(!rs.next()){
                        System.out.println("Nema2");
                        refresh();
                        return;
                    }
                    int whiteid=rs.getInt(1);
                    int blackid=rs.getInt(2);
                    rs.close();
                    //endregion

                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChessRoom.fxml"));
                    Parent root = (Parent) fxmlLoader.load();

                    ChessRoom controller = fxmlLoader.getController();
                    controller.setRoomId(0);

                    Tab tab=new Tab("Review",root);
                    tab.setOnClosed(e->controller.closeRoom());

                    controller.setCurrentTab(tab);

                    currentTab.getTabPane().getTabs().add(tab);
                    currentTab.getTabPane().getSelectionModel().select(tab);

                    controller.importGame(moves,whiteid,blackid);


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }





}
