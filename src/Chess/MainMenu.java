package Chess;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
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
    private int id=0;
    private ArrayList<Integer> rooms=new ArrayList<Integer>();

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
    refresh();


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
            Connection conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("update player set online=0 where username=?");
            upit.setString(1,username);
            upit.executeUpdate();
            for(int i:rooms){
                upit=conn.prepareStatement("delete from room where id=? and (white!=0 or black!=0)");
                upit.setInt(1,i);
                upit.executeUpdate();
            }
            conn.close();
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
            Connection conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select id,white,black From room");
            ResultSet result = upit.executeQuery();
            // if(result.next()==false) System.out.println("Nema");
            while(result.next()){

                String s=result.getInt(2)+" : ";
                System.out.println("Uzeo je "+result.getInt(1));
                upit=conn.prepareStatement("Select username From player where id=?");
                upit.setInt(1,result.getInt(2));
                ResultSet result2 = upit.executeQuery();
                if(result2.next())
                    s+="White="+result2.getString(1);
                else s+="White=/";

                upit=conn.prepareStatement("Select username From player  where id=?");
                upit.setInt(1,result.getInt(3));
                result2 = upit.executeQuery();
                if(result2.next())
                    s+="Black="+result2.getString(1);
                else s+="Black=/";
                roomsCBox.getItems().add(s);

                result2.close();
            }
            conn.close();

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


        if(rooms.contains(pom)){
            bojaIgraca= ChessPiece.Color.WHITE;
        }else bojaIgraca= ChessPiece.Color.BLACK;




        try {
            //conn.close();
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
            /*Properties properties=new Properties();
            properties.setProperty("user","Jasa");
            properties.setProperty("password","1234");
            properties.setProperty("useSSL","false");
            properties.setProperty("serverTimezone","UTC");
            String dburl = "jdbc:mysql://77.78.232.142:3306/chess";
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(dburl,properties);*/

            Class.forName("org.sqlite.JDBC");
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



    }
}
