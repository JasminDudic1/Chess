package Chess;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CreateRoom implements Initializable {


    public Button backBtn;
    public CheckBox colorCBox;
    public TextField nameText;
    public TextField passText;
    public Pane backgroundPane;
    private int id;
    private Tab currentTab;
    private int roomID=0;

    public void setCurrentTab(Tab t){
        currentTab=t;
    }

    private String hash(String s){

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void setId(int id){
        this.id=id;
    }

    public void backClicked(ActionEvent actionEvent) {

        currentTab.getTabPane().getTabs().remove(currentTab);

    }

    public void createClicked(ActionEvent actionEvent) {

        if(nameText.getText().isEmpty())return;
        
        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn=ConnectionDAO.getConn();
            PreparedStatement ps;
            ps=conn.prepareStatement("Select id from room where roomname=?");
            ps.setString(1,nameText.getText());
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Already exists");
                a.setHeaderText("Exists");
                a.showAndWait();
                rs.close();
                return;
            }

            if(colorCBox.isSelected()){

               if(ConnectionDAO.isOnline()) ps=conn.prepareStatement("INSERT into room values(0,'',0,?,?,?)");
               else ps=conn.prepareStatement("INSERT into room values(null,'',0,?,?,?)");
            }

            else{
                if(ConnectionDAO.isOnline())  ps=conn.prepareStatement("INSERT into room values(0,'',?,0,?,?)");
                else ps=conn.prepareStatement("INSERT into room values(null,'',?,0,?,?)");
            }

            ps.setInt(1,id);
            ps.setString(2,nameText.getText());
            if(passText.getText().isEmpty())ps.setString(3,"");
            else ps.setString(3,hash(passText.getText()));


            ps.executeUpdate();

            ps=conn.prepareStatement("Select Max(id) from room");
            rs.close();
            rs=ps.executeQuery();

            if(rs.next()) roomID=rs.getInt(1);
            rs.close();

            ChessPiece.Color bojaIgraca= ChessPiece.Color.WHITE;
            if(colorCBox.isSelected())bojaIgraca= ChessPiece.Color.BLACK;


            try {

                ChessRoom controller=new ChessRoom();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/ChessRoom.fxml"),ConnectionDAO.getResourcebundle());
                fxmlLoader.setController(controller);
                Parent root = (Parent) fxmlLoader.load();
                controller.setRoomId(roomID);
                controller.draw(bojaIgraca);
                controller.setCurrentTab(currentTab);
                currentTab.setText(roomID+":"+nameText.getText());
                currentTab.setContent(root);
            } catch(Exception e) {
                e.printStackTrace();
            }


        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }




    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        BackgroundImage bimg=new BackgroundImage(new Image("backgroundimages/createRoomGif.gif",800,550,false,false), BackgroundRepeat.SPACE,BackgroundRepeat.SPACE
                , BackgroundPosition.CENTER, BackgroundSize.DEFAULT);

        backgroundPane.setBackground(new Background(bimg));
        
    }
}
