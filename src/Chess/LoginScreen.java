package Chess;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginScreen implements Initializable {


    public Button registerBtn;
    public Button logInBtn;
    public PasswordField passwordPBox;
    public CheckBox rememberMeCB;
    public Pane backgroundPane;
    public ComboBox usernameTBox;
    private ArrayList<String> savedUsernames=new ArrayList<>();
    private ArrayList<String> savedPasswords=new ArrayList<>();
    private int id=0;

    private String hash(String s){

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void registerClicked(ActionEvent actionEvent) {

        if(usernameTBox.getSelectionModel().getSelectedItem()==null)return;

        if(usernameTBox.getSelectionModel().getSelectedItem().toString().length()<5 || passwordPBox.getText().length()<5 ||
                usernameTBox.getSelectionModel().getSelectedItem().toString().length()>15 || passwordPBox.getText().length()>15 ){
            new Alert(Alert.AlertType.ERROR,"Username and password must be between 5 and 15 characters").show();
            return;
        }

        String username=usernameTBox.getSelectionModel().getSelectedItem().toString();
        String password=passwordPBox.getText();

        try {


            Class.forName("org.sqlite.JDBC");
            Connection conn=ConnectionDAO.getConn();

            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select * From Player Where username=?");
            upit.setString(1,username);
            ResultSet result = upit.executeQuery();

            if(result.next()==false && !username.isEmpty() && !password.isEmpty()){
            if(ConnectionDAO.isOnline()) upit=conn.prepareStatement("Insert into Player values(0,?,?,0,0,0,1000,0)");
            else upit=conn.prepareStatement("Insert into Player values(null,?,?,0,0,0,1000,0)");

                upit.setString(1,username);
                upit.setString(2,hash(password));

                upit.executeUpdate();
                Alert a=new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Registered, you can now login.");
                a.setTitle("Success");
                a.setHeaderText("");
                a.show();
            }else {
                new Alert(Alert.AlertType.ERROR,"That username is already registered").show();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void logInClicked(ActionEvent actionEvent) {

        if(usernameTBox.getSelectionModel().getSelectedItem()==null)return;

        String username=usernameTBox.getSelectionModel().getSelectedItem().toString();
        String password=passwordPBox.getText();

        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn=ConnectionDAO.getConn();

            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select online,id From Player Where username=? and password=?");
            upit.setString(1,username);
            upit.setString(2,hash(password));
            ResultSet result = upit.executeQuery();
            if(result.next()==false){
                new Alert(Alert.AlertType.ERROR,"Wrong username and/or password.").show();
                passwordPBox.clear();
                return;
            }else if(result.getInt(1)==1){

                Alert a=new Alert(Alert.AlertType.ERROR,"Account is already logged in, do you wish to logout?",ButtonType.YES,ButtonType.NO);
                Optional answer=a.showAndWait();
                if(answer.get()==ButtonType.YES){
                    ConnectionDAO.logout(result.getInt(2));
                    new Alert(Alert.AlertType.CONFIRMATION,"Try logging in now",ButtonType.OK).show();
                    return;
                }

                return;
            }
            else{
                Alert a=new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Logged in!");
                a.setTitle("Logged in!");
                a.setHeaderText("");
                a.showAndWait();
                id=result.getInt(2);
                upit=conn.prepareStatement("Update player set online=1 where id=?");
                upit.setInt(1,result.getInt(2));
                upit.executeUpdate();
                if(rememberMeCB.isSelected()) {

                    if(!savedUsernames.contains(usernameTBox.getSelectionModel().getSelectedItem().toString())) {
                        savedUsernames.add(usernameTBox.getSelectionModel().getSelectedItem().toString());
                        savedPasswords.add(passwordPBox.getText());
                    }
                        XMLEncoder izlaz = new XMLEncoder(new FileOutputStream("login.xml"));
                    for(int i=0;i<savedUsernames.size();i++){
                        izlaz.writeObject(savedUsernames.get(i));
                        izlaz.writeObject(savedPasswords.get(i));
                    }
                        izlaz.close();

                }

                try {

                    Lobby controller =new Lobby();
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/Lobby.fxml"),ConnectionDAO.getResourcebundle());
                    fxmlLoader.setController(controller);
                    Parent root = fxmlLoader.load();

                    controller.setUsername(usernameTBox.getSelectionModel().getSelectedItem().toString());
                    controller.setLoggedinID(id);

                    Tabs tabsController=new Tabs();
                    fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/Tabs.fxml"),ConnectionDAO.getResourcebundle());
                    fxmlLoader.setController(tabsController);
                    Parent tabsRoot=(Parent)fxmlLoader.load();

                    tabsController.getTabsPane().getTabs().add(new Tab("Lobbies",root));
                    controller.setTabsTabPane(tabsController.getTabsPane());

                    Stage stage = new Stage();
                    stage.setOnHiding((e)-> {
                        controller.exit();
                    });
                    stage.setScene(new Scene(tabsRoot));
                    stage.setTitle(username);
                    stage.show();

                    if(ConnectionDAO.isOnline()) ((Stage) usernameTBox.getScene().getWindow()).close();

                } catch(Exception e) {
                    e.printStackTrace();
                }

            }

        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        BackgroundImage bimg=new BackgroundImage(new Image("backgroundimages/loginBackground.jpg",600,520,false,false), BackgroundRepeat.REPEAT,BackgroundRepeat.NO_REPEAT
        , BackgroundPosition.CENTER,BackgroundSize.DEFAULT);

        backgroundPane.setBackground(new Background(bimg));


        Document xmldoc = null;
        try {
            DocumentBuilder docReader
                    = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmldoc = docReader.parse("login.xml");
            NodeList djeca=xmldoc.getElementsByTagName("string");

            XMLDecoder ulaz = new XMLDecoder(new FileInputStream("login.xml"));
            for(int i=0;i<djeca.getLength()/2;i++) {
                savedUsernames.add((String) ulaz.readObject());
                savedPasswords.add((String) ulaz.readObject());
            }


        } catch (Exception e) { }

        usernameTBox.setItems(FXCollections.observableList(savedUsernames));

        usernameTBox.getSelectionModel().selectedItemProperty().addListener((obs, oldKorisnik, newKorisnik) -> {
            if(savedUsernames.contains(newKorisnik))
            passwordPBox.setText(savedPasswords.get(savedUsernames.indexOf(newKorisnik)));

        });


    }
}
