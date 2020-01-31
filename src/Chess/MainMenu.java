package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class MainMenu implements Initializable {

    public String username="";
    public ListView roomsCBox;
    public PasswordField passwordPBox;
    public Pane backgroundPane;
    private TabPane tabsTabPane;
    private int loggedinID =0;


    private String hash(String s){

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void setLoggedinID(int loggedinID) {

        this.loggedinID = loggedinID;
        usernameLab.setText("Logged in as : "+username+" "+ loggedinID);
    }

    public Label usernameLab;

    public void setTabsTabPane(TabPane tabsTabPane) {
        this.tabsTabPane = tabsTabPane;
    }

    public void setUsername(String username) {
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

        BackgroundImage bimg=new BackgroundImage(new Image("Backgroundimages/menuGif.gif",300,300,true,false), BackgroundRepeat.REPEAT,BackgroundRepeat.NO_REPEAT
                , BackgroundPosition.CENTER, BackgroundSize.DEFAULT);

        backgroundPane.setBackground(new Background(bimg));

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

        //boolean isShowing=roomsCBox.isShowing();
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

            Connection conn=ConnectionDAO.getConn();
            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select id,roomName,length(moves) From room where white!=? and black!=?");
            upit.setInt(1, loggedinID);
            upit.setInt(2, loggedinID);
            ResultSet result = upit.executeQuery();
            while(result.next()){

                if(result.getInt(3)>1 && (result.getInt(1)==0 || result.getInt(2)==0))continue;

                String s=result.getInt(1)+" : "+result.getString(2);
                upit=conn.prepareStatement("Select white,black,password From room where id=? limit 1");
                upit.setInt(1,Integer.parseInt(s.substring(0,s.indexOf(":")-1)));

                ResultSet result2=upit.executeQuery();
                result2.next();

                if(result2.getInt(1)==0)s+=":White Empty";
                else if(result2.getInt(2)==0)s+=":Black Empty";
                else s+=":Spectate";

                if(!result2.getString(3).isEmpty()) s+=":password";

                roomsCBox.getItems().add(s);

                result2.close();
            }



        } catch ( SQLException e) {
            e.printStackTrace();
        }

        if(selected!=null && roomsCBox.getItems().contains(selected))
            roomsCBox.getSelectionModel().select(selected);


    }

    public void enterClicked(ActionEvent actionEvent) {


        if(roomsCBox.getSelectionModel().getSelectedItem()==null)return;

        ChessPiece.Color bojaIgraca=null;
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
            else if(result.getInt(1)==0)bojaIgraca=ChessPiece.Color.WHITE;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {


            if(bojaIgraca== ChessPiece.Color.BLACK)
            upit=conn.prepareStatement("Update room set black=? where id=?");
            else if(bojaIgraca==ChessPiece.Color.WHITE) upit=conn.prepareStatement("Update room set white=? where id=?");

            if(bojaIgraca!=null) {
                upit.setInt(1, loggedinID);
                upit.setInt(2, pom);
                upit.executeUpdate();
            }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ChessRoom.fxml"));
            Parent root = (Parent) fxmlLoader.load();

            ChessRoom controller = fxmlLoader.getController();
            controller.setRoomId(pom);
            controller.draw(bojaIgraca);


            Tab tab=new Tab(pom+":"+roomName,root);
            tab.setOnClosed(e->controller.closeRoom());

            controller.setCurrentTab(tab);

            tabsTabPane.getTabs().add(tab);
            tabsTabPane.getSelectionModel().select(tab);

            // ((Stage)usernameTBox.getScene().getWindow()).close();
        } catch(Exception e) {
            e.printStackTrace();
        }


    }

    public void createClicked(ActionEvent actionEvent) {


        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateRoom.fxml"));
            Parent root =  fxmlLoader.load();

            CreateRoom controller = fxmlLoader.getController();
            controller.setId(loggedinID);

            //Stage stage = new Stage();
            //stage.setScene(new Scene(root));
            Tab tab=new Tab("Creating room",root);
            controller.setCurrentTab(tab);
            tabsTabPane.getTabs().add(tab);
            tabsTabPane.getSelectionModel().select(tab);

           // ((Stage)tabsTabPane.getScene().getWindow()).setScene(null);

            //stage.setOnHiding(e->controller.stop());

           // stage.show();




        } catch(Exception e) {
            e.printStackTrace();
        }




    }

    public void profilesClicked(ActionEvent actionEvent) {

        try {

            for(Tab t:tabsTabPane.getTabs())
                if(t.getText()=="Profiles"){
                    tabsTabPane.getSelectionModel().select(t);
                    return;
                }

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ProfilePage.fxml"));
            Parent root = (Parent) fxmlLoader.load();

            ProfilePage controller = fxmlLoader.getController();
            controller.setPlayer(loggedinID, loggedinID);


            Tab tab=new Tab("Profiles",root);
            controller.setCurrentTab(tab);

            tabsTabPane.getTabs().add(tab);
            tabsTabPane.getSelectionModel().select(tab);

            // ((Stage)usernameTBox.getScene().getWindow()).close();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void logoutClicked(ActionEvent actionEvent) throws SQLException {

        Connection conn=ConnectionDAO.getConn();
        PreparedStatement ps=conn.prepareStatement("Update player set online=0 where id=?");
        ps.setInt(1, loggedinID);
        ps.executeUpdate();

        Stage s= (Stage) passwordPBox.getScene().getWindow();
        s.close();


    }
}
