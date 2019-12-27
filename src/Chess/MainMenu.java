package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    private TabPane tabsTabPane;
    private int id=0;


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

    public void setTabsTabPane(TabPane tabsTabPane) {
        this.tabsTabPane = tabsTabPane;
    }

    public void setUsername(String username)
    {
        System.out.println("Called");

        this.username = username;
        usernameLab.setText("Logged in as : "+username);
    }

    public void exitClicked(ActionEvent actionEvent)
    {
        System.exit(0);
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


    public void refresh(){

        boolean isShowing=roomsCBox.isShowing();
        Object selected=null;
        if(roomsCBox.getSelectionModel().getSelectedItem()!=null)
            selected=roomsCBox.getSelectionModel().getSelectedItem();

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
            PreparedStatement upit=conn.prepareStatement("Select id,roomName From room where white!=? and black!=? and length(moves)=0");
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

        if(selected!=null && roomsCBox.getItems().contains(selected))
            roomsCBox.getSelectionModel().select(selected);

            //roomsCBox.getSelectionModel().selectLast();
           if(isShowing) roomsCBox.show();

    }

    public void enterClicked(ActionEvent actionEvent) {


        if(roomsCBox.getSelectionModel().getSelectedItem()==null)return;

        ChessPiece.Color bojaIgraca= ChessPiece.Color.WHITE;
        String s=roomsCBox.getSelectionModel().getSelectedItem().toString();
        roomsCBox.getSelectionModel().select(null);
        int pom=Integer.parseInt(s.substring(0,s.indexOf(':')-1));
        String roomName="";

        Connection conn=ConnectionDAO.getConn();
        PreparedStatement upit= null;
        try {
            upit = conn.prepareStatement("Select white,black,password,roomname from room where id=? ");
            upit.setInt(1,pom);
            ResultSet result=upit.executeQuery();
            result.next();
            roomName=result.getString(4);
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

            upit.close();
            if(bojaIgraca== ChessPiece.Color.BLACK)
            upit=conn.prepareStatement("Update room set black=? where id=?");
            else upit=conn.prepareStatement("Update room set white=? where id=?");

            upit.setInt(1,id);
            upit.setInt(2,pom);
            upit.executeUpdate();

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChessRoom.fxml"));
            Parent root = (Parent) fxmlLoader.load();

            ChessRoom controller = fxmlLoader.getController();
            controller.setRoomId(pom);
            controller.draw(bojaIgraca);


            Tab tab=new Tab(pom+":"+roomName,root);
            tab.setOnClosed(e->controller.closeRoom());

            controller.setCurrentTab(tab);

            tabsTabPane.getTabs().add(tab);

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
            Parent root =  fxmlLoader.load();

            CreateRoom controller = fxmlLoader.getController();
            controller.setId(id);

            //Stage stage = new Stage();
            //stage.setScene(new Scene(root));
            Tab tab=new Tab("Creating room",root);
            controller.setCurrentTab(tab);
            tabsTabPane.getTabs().add(tab);

           // ((Stage)tabsTabPane.getScene().getWindow()).setScene(null);

            //stage.setOnHiding(e->controller.stop());

           // stage.show();




        } catch(Exception e) {
            e.printStackTrace();
        }






    }



}
