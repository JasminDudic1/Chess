package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ChessRoom implements Initializable {

    public GridPane boardGridPane;
    public Label OpponentLab;
    public Label PlayerLab;
    public Label colorLab;
    private Board b;
    private int roomId;
    private PreparedStatement getOpponent,getWhite,getBlack;
    boolean running=true;
    private ChessPiece.Color bojaIgraca= ChessPiece.Color.WHITE;

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    private void setPlayerLabels(int playerWhite,int playerBlack){


        try {
            getWhite=ConnectionDAO.getConn().prepareStatement("Select username from player where id=?");
            getWhite.setInt(1,playerWhite);

            getBlack=ConnectionDAO.getConn().prepareStatement("Select username from player where id=?");
            getBlack.setInt(1,playerBlack);

            ResultSet rwhite=getWhite.executeQuery();
            rwhite.next();
            ResultSet rblack=getBlack.executeQuery();
            rblack.next();

            if(b.getCurrentPlayer()== ChessPiece.Color.WHITE){
                PlayerLab.setText(rwhite.getString(1));
                OpponentLab.setText(rblack.getString(1));
            }else{
                OpponentLab.setText(rwhite.getString(1));
                PlayerLab.setText(rblack.getString(1));
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void checkForOpponent(){

        try {
            ResultSet rs=getOpponent.executeQuery();
            if(!rs.next()) System.out.println("Uhm fix here");
            if(rs.getInt(1)!=0 && rs.getInt(2)!=0){
                System.out.println("Ubacujem u sobu:"+roomId);
                b.setGameReady();
                running=false;
                setPlayerLabels(rs.getInt(1),rs.getInt(2));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {



    }

    public void draw(ChessPiece.Color bojaIgraca){

        this.bojaIgraca=bojaIgraca;
        b=new Board(boardGridPane, bojaIgraca);
        b.setColorLab(colorLab);
        b.setRoomId(roomId);
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

        running=false;
        Connection conn=ConnectionDAO.getConn();

        try {
            PreparedStatement upit = conn.prepareStatement("Select white,black from room where id=?");
            upit.setInt(1,roomId);
            ResultSet rs=upit.executeQuery();
            rs.next();
            if(rs.getInt(1)!=0 || rs.getInt(2)!=0){
                upit=conn.prepareStatement("Drop from room where id=?");
                upit.setInt(1,roomId);
                upit.executeUpdate();
                System.out.println("Obrisana soba");
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


    }

    public void exitClicked(ActionEvent actionEvent) {
        closeRoom();

    }
}
