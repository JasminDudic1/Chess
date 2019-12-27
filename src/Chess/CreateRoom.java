package Chess;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;

public class CreateRoom {


    public Button backBtn;
    public CheckBox colorCBox;
    public TextField nameText;
    public TextField passText;
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

        Stage stage = (Stage) backBtn.getScene().getWindow();

    }

    public void createClicked(ActionEvent actionEvent) {

        if(nameText.getText().isEmpty())return;
        
        try {
            /*Properties properties=new Properties();
            properties.setProperty("user","Jasa");
            properties.setProperty("password","1234");
            properties.setProperty("useSSL","false");
            properties.setProperty("serverTimezone","UTC");
            String dburl = "jdbc:mysql://77.78.232.142:3306/chess";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dburl,properties);*/

            Class.forName("org.sqlite.JDBC");
            Connection conn=ConnectionDAO.getConn();
            PreparedStatement upit;
            upit=conn.prepareStatement("Select id from room where roomname=?");
            upit.setString(1,nameText.getText());
            ResultSet rs=upit.executeQuery();
            if(rs.next()){
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Already exists");
                a.setHeaderText("Exists");
                a.showAndWait();
                rs.close();
                return;
            }

            if(colorCBox.isSelected()) upit=conn.prepareStatement("INSERT into room values(null,'','',0,?,?,?)");
            else upit=conn.prepareStatement("INSERT into room values(null,'','',?,0,?,?)");
            upit.setInt(1,id);
            upit.setString(2,nameText.getText());
            if(passText.getText().isEmpty())upit.setString(3,"");
            else upit.setString(3,hash(passText.getText()));


            upit.executeUpdate();

            upit=conn.prepareStatement("Select Max(id) from room");
            rs.close();
            rs=upit.executeQuery();

            if(rs.next()) roomID=rs.getInt(1);
            rs.close();

            ChessPiece.Color bojaIgraca= ChessPiece.Color.WHITE;
            if(colorCBox.isSelected())bojaIgraca= ChessPiece.Color.BLACK;


            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChessRoom.fxml"));
                Parent root = (Parent) fxmlLoader.load();

                ChessRoom controller = fxmlLoader.getController();
                controller.setRoomId(roomID);
                controller.draw(bojaIgraca);
                controller.setCurrentTab(currentTab);
                currentTab.setText(roomID+":"+nameText.getText());
                currentTab.setContent(root);

                /*Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setOnHiding(e->controller.stop());
                stage.show();

                ((Stage) backBtn.getScene().getWindow()).close();*/


                // ((Stage)usernameTBox.getScene().getWindow()).close();
            } catch(Exception e) {
                e.printStackTrace();
            }


        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }




    }
}
