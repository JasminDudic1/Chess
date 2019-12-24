package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainMenu implements Initializable {

    public String username="";
    public ChoiceBox roomsCBox;
    public PasswordField passwordPBox;
    private int id=0;
    private ArrayList<Integer> rooms=new ArrayList<Integer>();


    private String hash(String s){

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void setId(int id) {

        this.id = id;
        usernameLab.setText("Logged in as : "+username+" "+id);
    }

    public Label usernameLab;

    public void setUsername(String username)
    {
        System.out.println("Called");

        this.username = username;
        usernameLab.setText("Logged in as : "+username);
    }

    public void exitClicked(ActionEvent actionEvent)
    {
        stop();
        System.exit(0);
    }

    public void startClicked(ActionEvent actionEvent) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("sample.fxml"));
            Parent root = (Parent) fxmlLoader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            //Main.stg.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    //refresh();


    }

    public void stop(){

        System.out.printf("Logging out");
        for(int i:rooms)
            System.out.println(i);

        try {
            /* Properties properties=new Properties();
        properties.setProperty("user","Jasa");
        properties.setProperty("password","1234");
        properties.setProperty("useSSL","false");
        properties.setProperty("serverTimezone","UTC");
        String url = "jdbc:mysql://77.78.232.142:3306/chess";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url,properties);*/

            Class.forName("org.sqlite.JDBC");
            Connection conn=ConnectionDAO.getConn();
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("update player set online=0 where username=?");
            upit.setString(1,username);
            upit.executeUpdate();
            for(int i:rooms){
                upit=conn.prepareStatement("delete from room where id=? and (white!=0 or black!=0)");
                upit.setInt(1,i);
                upit.executeUpdate();
            }
        } catch (ClassNotFoundException | SQLException e) {

            e.printStackTrace();
        }

    }

    public void refresh(){

        roomsCBox.getItems().clear();


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
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select id,roomName From room where white!=? and black!=?");
            upit.setInt(1,id);
            upit.setInt(2,id);
            ResultSet result = upit.executeQuery();
            // if(result.next()==false) System.out.println("Nema");
            while(result.next()){

                String s=result.getInt(1)+" : "+result.getString(2);
                upit=conn.prepareStatement("Select white,black,password From room where id=?");
                upit.setInt(1,Integer.parseInt(s.substring(0,s.indexOf(":")-1)));

                ResultSet result2=upit.executeQuery();
                result2.next();
                if(result2.getInt(1)==1)s+=":Black Empty";
                else s+=":White Empty";
                if(!result2.getString(3).isEmpty()) s+=":password";

                roomsCBox.getItems().add(s);

                result2.close();
            }


        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        if(roomsCBox.getItems().size()!=0)
            roomsCBox.getSelectionModel().selectLast();

    }

    public void enterClicked(ActionEvent actionEvent) {


        if(roomsCBox.getSelectionModel().getSelectedItem()==null)return;

        ChessPiece.Color bojaIgraca= ChessPiece.Color.WHITE;
        String s=roomsCBox.getSelectionModel().getSelectedItem().toString();
        int pom=Integer.parseInt(s.substring(0,s.indexOf(':')-1));

        Connection conn=ConnectionDAO.getConn();
        PreparedStatement upit= null;
        try {
            upit = conn.prepareStatement("Select white,black,password from room where id=? ");
            upit.setInt(1,pom);
            ResultSet result=upit.executeQuery();
            result.next();
            String pass="";
            if(!passwordPBox.getText().isEmpty())pass=hash(passwordPBox.getText());
            if(!pass.equals(result.getString(3))){
                passwordPBox.clear();
                System.out.println("Password wrong,real pass is "+result.getString(3)+" but got "+pass);
                return;
            }

            if(result.getInt(2)==0)bojaIgraca= ChessPiece.Color.BLACK;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChessRoom.fxml"));
            Parent root = (Parent) fxmlLoader.load();

            ChessRoom controller = fxmlLoader.getController();
            controller.draw(bojaIgraca);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setOnHiding(e->controller.stop());
            stage.show();
            // ((Stage)usernameTBox.getScene().getWindow()).close();
        } catch(Exception e) {
            e.printStackTrace();
        }


    }

    public void refreshClick(ActionEvent actionEvent) {
        refresh();
    }

    public void createClicked(ActionEvent actionEvent) {



        try {


            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateRoom.fxml"));
            Parent root = (Parent) fxmlLoader.load();

            CreateRoom controller = fxmlLoader.getController();
            controller.setId(id);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            //stage.setOnHiding(e->controller.stop());
            stage.show();


            // ((Stage)usernameTBox.getScene().getWindow()).close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        //try {
            /*Properties properties=new Properties();
            properties.setProperty("user","Jasa");
            properties.setProperty("password","1234");
            properties.setProperty("useSSL","false");
            properties.setProperty("serverTimezone","UTC");
            String dburl = "jdbc:mysql://77.78.232.142:3306/chess";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dburl,properties);*/

           /* Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            Statement stmt = conn.createStatement();
            PreparedStatement upit;
            upit=conn.prepareStatement("Select id,white,black From room where (white=? and black=0) or (white=0 and black=?)");

            upit.setInt(1,id);
            upit.setInt(2,id);
            ResultSet result = upit.executeQuery();
            if(result.next()){
                System.out.printf("Vec postoji");
                conn.close();
                return;
            }

             //upit=conn.prepareStatement("INSERT into room values(null,'','',?,default)");
            upit=conn.prepareStatement("INSERT into room values(null,'','',?,0)");
            upit.setInt(1,id);
            upit.executeUpdate();

            upit=conn.prepareStatement("Select Max(id) from room");
            ResultSet rs=upit.executeQuery();
            int maxRoom=0;
            if(rs.next()) maxRoom=rs.getInt(1);
            rooms.add(maxRoom);

            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        refresh();
        enterClicked(actionEvent);
*/




    }



}
