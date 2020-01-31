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
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
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

        if(usernameTBox.getSelectionModel().getSelectedItem()==null ||passwordPBox.getText().isEmpty())return;

        String username=usernameTBox.getSelectionModel().getSelectedItem().toString();
        String password=passwordPBox.getText();

        try {

            //Class.forName("com.mysql.cj.jdbc.Driver");
            //Connection conn = DriverManager.getConnection(url,properties);

            Class.forName("org.sqlite.JDBC");
          // Connection conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            Connection conn=ConnectionDAO.getConn();

            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select * From Player Where username=?");
            upit.setString(1,username);
            ResultSet result = upit.executeQuery();
            if(result.next()==false && !username.isEmpty() && !password.isEmpty()){
                ///upit=conn.prepareStatement("Insert into Player values(null,?,?,default,default,default,default,default,default)");
                upit=conn.prepareStatement("Insert into Player values(null,?,?,0,0,0,1000,0,0)");
                upit.setString(1,username);
                upit.setString(2,hash(password));
                upit.executeUpdate();
                Alert a=new Alert(Alert.AlertType.INFORMATION);
                a.setContentText("Registered, you can now login.");
                a.setTitle("Success");
                a.setHeaderText("");
                a.show();
            }else {
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Already Registered");
                a.setTitle("ERROR");
                a.setHeaderText("");
                a.show();
                passwordPBox.clear();
               // usernameTBox.clear();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void logInClicked(ActionEvent actionEvent) {

        String username=usernameTBox.getSelectionModel().getSelectedItem().toString();
        String password=passwordPBox.getText();

        try {
            System.out.printf("USAO");

            Class.forName("org.sqlite.JDBC");
            Connection conn=ConnectionDAO.getConn();

            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("Select online,id From Player Where username=? and password=?");
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
                return;
            }else if(result.getInt(1)==1){
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Already Logged in");
                a.setTitle("ERROR");
                a.setHeaderText("");
                a.show();
                passwordPBox.clear();
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




                    BufferedWriter writer = new BufferedWriter(new FileWriter("login.txt"));
                    writer.write(usernameTBox.getSelectionModel().getSelectedItem().toString());
                    writer.write("\n");
                    writer.write(passwordPBox.getText());
                    writer.close();

                }

                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                    Parent root = (Parent) fxmlLoader.load();


                    MainMenu controller = fxmlLoader.getController();
                    controller.setUsername(usernameTBox.getSelectionModel().getSelectedItem().toString());
                    controller.setLoggedinID(id);

                    fxmlLoader = new FXMLLoader(getClass().getResource("Tabs.fxml"));
                    Parent tabsRoot=(Parent)fxmlLoader.load();
                    Tabs tabsController=fxmlLoader.getController();
                    tabsController.getTabsPane().getTabs().add(new Tab("Main menu",root));
                    controller.setTabsTabPane(tabsController.getTabsPane());

                    Stage stage = new Stage();
                    stage.setScene(new Scene(tabsRoot));
                    stage.setTitle(username);
                    stage.show();


                } catch(Exception e) {
                    e.printStackTrace();
                }

               // passwordPBox.clear();

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        BackgroundImage bimg=new BackgroundImage(new Image("Backgroundimages/loginImage.jpg",600,520,false,false), BackgroundRepeat.REPEAT,BackgroundRepeat.NO_REPEAT
        , BackgroundPosition.CENTER,BackgroundSize.DEFAULT);

        backgroundPane.setBackground(new Background(bimg));


        Document xmldoc = null;
        try {
            DocumentBuilder docReader
                    = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmldoc = docReader.parse("login.xml");
            NodeList djeca=xmldoc.getElementsByTagName("string");
            System.out.println("Ima "+djeca.getLength());

            XMLDecoder ulaz = new XMLDecoder(new FileInputStream("login.xml"));
            for(int i=0;i<djeca.getLength();i++) {
                savedUsernames.add((String) ulaz.readObject());
                savedPasswords.add((String) ulaz.readObject());
            }


        } catch (Exception e) {
            System.out.println("login.xml nije validan XML dokument");
        }

        usernameTBox.setItems(FXCollections.observableList(savedUsernames));

        usernameTBox.getSelectionModel().selectedItemProperty().addListener((obs, oldKorisnik, newKorisnik) -> {
            if(savedUsernames.contains(newKorisnik))
            passwordPBox.setText(savedPasswords.get(savedUsernames.indexOf(newKorisnik)));

        });


      /*  File file = new File("login.txt");
            if(file.exists()) {

                String str = usernameTBox.getSelectionModel().getSelectedItem().toString()+"\n"+passwordPBox.getText();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("login.txt"));
                    //usernameTBox.setText(reader.readLine());
                    passwordPBox.setText(reader.readLine());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }*/

    }
}
