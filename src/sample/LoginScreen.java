package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javax.swing.*;
import java.sql.*;
import java.util.Properties;

public class LoginScreen {


    public Button RegisterBtn;
    public Button LogInBtn;
    public TextField usernameTBox;
    public PasswordField passwordPBox;

    private String hash(String s){

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void registerClicked(ActionEvent actionEvent) {

        Properties properties=new Properties();
        properties.setProperty("user","Jasa");
        properties.setProperty("password","1234");
        properties.setProperty("useSSL","false");
        properties.setProperty("serverTimezone","UTC");
        String url = "jdbc:mysql://77.78.232.142:3306/chess";
        String username=usernameTBox.getText();
        String password=passwordPBox.getText();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,properties);
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select * From Player Where username=?");
            upit.setString(1,username);
            ResultSet result = upit.executeQuery();
            if(result.next()==false && !username.isEmpty() && !password.isEmpty()){
                upit=conn.prepareStatement("Insert into Player values(null,?,?,default,default,default,default)");
                upit.setString(1,username);
                upit.setString(2,hash(password));
                upit.executeUpdate();
                Alert a=new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Registered, you can now login.");
                a.setTitle("Success");
                a.setHeaderText("");
                a.show();
                passwordPBox.clear();
                usernameTBox.clear();
            }else {
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Already Registered");
                a.setTitle("ERROR");
                a.setHeaderText("");
                a.show();
                passwordPBox.clear();
                usernameTBox.clear();
            }
        conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void logInClicked(ActionEvent actionEvent) {

        Properties properties=new Properties();
        properties.setProperty("user","Jasa");
        properties.setProperty("password","1234");
        properties.setProperty("useSSL","false");
        properties.setProperty("serverTimezone","UTC");
        String url = "jdbc:mysql://77.78.232.142:3306/chess";
        String username=usernameTBox.getText();
        String password=passwordPBox.getText();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,properties);
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select * From Player Where username=? and password=?");
            upit.setString(1,username);
            upit.setString(2,hash(password));
            ResultSet result = upit.executeQuery();
            if(result.next()==false){
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Login failed!");
                a.setTitle("ERROR");
                a.setHeaderText("");
                a.show();
                passwordPBox.clear();
                usernameTBox.clear();
            }else{
                Alert a=new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Logged in!");
                a.setTitle("Logged in!");
                a.setHeaderText("");
                a.showAndWait();
                MainMenu.username=username;
                passwordPBox.clear();
                usernameTBox.clear();

                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                    Parent root = (Parent) fxmlLoader.load();
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.show();
                    ((Stage)usernameTBox.getScene().getWindow()).close();
                } catch(Exception e) {
                    e.printStackTrace();
                }


            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
