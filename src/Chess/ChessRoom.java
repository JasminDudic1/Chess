package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChessRoom {

    public GridPane boardGridPane;
    public Label opponentLab;
    public Label playerLab;
    public Label colorLab;
    public TextArea errorText;
    public Button previousBtn,nextBtn,lastBtn,firstBtn;
    private Board b;
    private int roomId;
    private PreparedStatement getOpponent, getWhiteStatment, getBlackStatment,getWhiteID,getBlackID;
    boolean running=true;
    private ChessPiece.Color bojaIgraca= ChessPiece.Color.WHITE;
    private Tab currentTab;
    boolean rematch=false;
    boolean isImport=false;
    boolean isSpectate=false;
    private int playerID=0;

    public void setCurrentTab(Tab t){
        currentTab=t;
    }

    public void setRoomId(int roomId) {

        this.roomId = roomId;
    }

    public void setPlayerLabels(int playerWhite,int playerBlack){

       if(bojaIgraca==ChessPiece.Color.WHITE){
           playerID=playerWhite;
           System.out.println("White igrac je "+playerWhite);
       }
       else {
           playerID=playerBlack;
           System.out.println("Black igrac je "+playerBlack);
       }

        try {
            getWhiteStatment =ConnectionDAO.getConn().prepareStatement("Select username from player where id=?");
            getWhiteStatment.setInt(1,playerWhite);

            getBlackStatment =ConnectionDAO.getConn().prepareStatement("Select username from player where id=?");
            getBlackStatment.setInt(1,playerBlack);

            getWhiteID=ConnectionDAO.getConn().prepareStatement("Select white from room where id=?");
            getWhiteID.setInt(1,roomId);

            getBlackID=ConnectionDAO.getConn().prepareStatement("Select black from room where id=?");
            getBlackID.setInt(1,roomId);

            ResultSet rwhite= getWhiteStatment.executeQuery();
            rwhite.next();
            ResultSet rblack= getBlackStatment.executeQuery();
            rblack.next();

            if(isImport){
                playerLab.setText(rwhite.getString(1));
                opponentLab.setText(rblack.getString(1));
                return;
            }

            if(b.getCurrentPlayer()== ChessPiece.Color.WHITE){
                playerLab.setText(rwhite.getString(1));
                opponentLab.setText(rblack.getString(1));
            }else{
                opponentLab.setText(rwhite.getString(1));
                playerLab.setText(rblack.getString(1));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void checkForOpponent(){

        if(isImport)return;

        try {
            ResultSet rs=getOpponent.executeQuery();
            if(!rs.next()) System.out.println("Uhm fix here");

            if(rs.getInt(1)==-1 || rs.getInt(1)==-1){
                running=false;
                closeRoom();
                return;
            }

            if(rs.getInt(1)>0 && rs.getInt(2)>0){

                System.out.println("Ubacujem u sobu:"+roomId);
                b.setGameReady();

                if(rematch==false) {
                    setPlayerLabels(rs.getInt(1),rs.getInt(2));
                }
                else {
                    setPlayerLabels(rs.getInt(2),rs.getInt(1));
                }
                b.setPlayersIds(rs.getInt(1),rs.getInt(2));
                b.setController(this);
                running=false;


            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void draw(ChessPiece.Color bojaIgraca){

        this.bojaIgraca=bojaIgraca;
        b=new Board(boardGridPane, bojaIgraca);
        b.setColorLab(colorLab);
        b.setRoomId(roomId);
        if(bojaIgraca==null){
            isSpectate=true;
            b.spectateGame();
        }

        try {
            getOpponent=ConnectionDAO.getConn().prepareStatement("Select white,black from room where id=?");
            getOpponent.setInt(1,roomId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        new Thread(()->{

            while(running){
                try {
                    Platform.runLater(()->checkForOpponent());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

        }).start();

    }

    public void closeRoom() {


        if(isImport || isSpectate){
            currentTab.getTabPane().getTabs().remove(currentTab);
            return;
        }

        running=false;
        Connection conn=ConnectionDAO.getConn();

        try {
            PreparedStatement upit = conn.prepareStatement("Select white,black from room where id=?");
            upit.setInt(1,roomId);
            ResultSet rs=upit.executeQuery();
            rs.next();
            System.out.println("Played "+playerID+" je obrisao sobu "+roomId+ " jer je white bio "
                    +rs.getInt(1)+" a black "+rs.getInt(2));
            if(rs.getInt(1)<=0 || rs.getInt(2)<=0){
                upit=conn.prepareStatement("delete from room where id=?");
                upit.setInt(1,roomId);
                upit.executeUpdate();

            }else{

                if(bojaIgraca== ChessPiece.Color.WHITE){
                    upit=conn.prepareStatement("Update room set white=0 where id=?");
                    upit.setInt(1,roomId);
                    upit.executeUpdate();
                }else{
                    upit=conn.prepareStatement("Update room set black=0 where id=?");
                    upit.setInt(1,roomId);
                    upit.executeUpdate();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        currentTab.getTabPane().getTabs().remove(currentTab);

    }

    public void exitClicked(ActionEvent actionEvent) {
            endChessRoom();
    }

    public void endChessRoom(){
        b.endGame();
        closeRoom();
    }

    public void rematch(){

        rematch=true;
        running=true;
        clearRoom();
        if(bojaIgraca== ChessPiece.Color.WHITE) {
            bojaIgraca=ChessPiece.Color.BLACK;
            draw(bojaIgraca);
        }
        else if(bojaIgraca== ChessPiece.Color.BLACK) {
            bojaIgraca=ChessPiece.Color.WHITE;
            draw(bojaIgraca);
        }
    }

    private void clearRoom(){

        if(roomId!=0) {

            Connection conn=ConnectionDAO.getConn();
            try {

                PreparedStatement upit = conn.prepareStatement("Update room set moves=?,white=?,black=? where id=?");
                upit.setString(1, "");

                int whiteID=getWhite();
                int blackID=getBlack();
                if(whiteID==-1 || blackID==-1){
                    System.out.println("Neki je -1");
                    closeRoom();
                    return;
                }

                if(whiteID==0){

                    upit.setInt(2,playerID);
                    upit.setInt(3,blackID);//prepisujem istu vrijednost
                    System.out.println("Igrac "+playerID+" je "+1);

                }else if (blackID==0){

                    upit.setInt(2,whiteID);//prepisujem istu vrijendost
                    upit.setInt(3,playerID);
                    System.out.println("Igrac "+playerID+" je "+2);

                }else if (playerID==whiteID){

                    upit.setInt(2,0);
                    upit.setInt(3,whiteID);
                    System.out.println("Igrac "+playerID+" je "+3);

                }else if (playerID==blackID){
                    upit.setInt(2,blackID);
                    upit.setInt(3,0);
                    System.out.println("Igrac "+playerID+" je "+4);

                }else System.out.println("WHAT!!!!");

                upit.setInt(4, roomId);
                upit.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void leaveRoom(){

        running=false;
        Connection conn=ConnectionDAO.getConn();

        try {

           int whiteID=getWhite();
           int blackID=getBlack();
            PreparedStatement upit=conn.prepareStatement("Select white,black from room where id=?");
            upit.setInt(1,roomId);
            ResultSet rs=upit.executeQuery();
            rs.next();
            if(rs.getInt(1)==-1 && rs.getInt(2)==-1){
                closeRoom();
                return;
            }

             upit=conn.prepareStatement("Update room set white=-1, black=-1 where id=?");
            upit.setInt(1,roomId);
            upit.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        currentTab.getTabPane().getTabs().remove(currentTab);

    }

    public void importGame(String moves,int whiteid,int blackid){

        isImport=true;

        lastBtn.setDisable(false);
        firstBtn.setDisable(false);
        nextBtn.setDisable(false);
        previousBtn.setDisable(false);

        b=new Board(boardGridPane, ChessPiece.Color.WHITE);
        b.setColorLab(colorLab);
        b.setRoomId(0);
        b.setPlayersIds(whiteid,blackid);
        b.setController(this);

        b.importGame(moves);



    }

    public void nextClicked(ActionEvent actionEvent) {

        b.next();

    }

    public void previousClicked(ActionEvent actionEvent) {
        b.previous();
    }

    public void firstClicked(ActionEvent actionEvent) {
        b.first();
    }

    public void lastClicked(ActionEvent actionEvent) {
        b.last();
    }

    private int getWhite(){

        try {
            ResultSet rs=getWhiteID.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
return 0;

    }

    private int getBlack(){
        try {
            ResultSet rs=getBlackID.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


}
