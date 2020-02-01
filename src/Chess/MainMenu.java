package Chess;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainMenu implements Initializable {

    
    public PasswordField passwordBox;
    public TextField usernameBox;
    public TextField servernameBox;
    public TextField urlBox;
    public ComboBox serversCBox;
    private ArrayList<String> serverNames=new ArrayList<>();
    private ArrayList<String> usernames = new ArrayList<>();
    private ArrayList<String> passwords = new ArrayList<>();
    private ArrayList<String> urls=new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Document xmldoc = null;
        try {
            DocumentBuilder docReader
                    = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            xmldoc = docReader.parse("databases.xml");
            NodeList djeca = xmldoc.getElementsByTagName("string");

            XMLDecoder ulaz = new XMLDecoder(new FileInputStream("databases.xml"));
            for (int i = 0; i < djeca.getLength() / 4; i++) {
                serverNames.add((String) ulaz.readObject());
                usernames.add((String) ulaz.readObject());
                passwords.add((String) ulaz.readObject());
                urls.add((String) ulaz.readObject());
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        refresh();

        serversCBox.getSelectionModel().selectedItemProperty().addListener((obs, oldKorisnik, newKorisnik) -> {

            if(newKorisnik==null)return;

            int index=serversCBox.getSelectionModel().getSelectedIndex();
            usernameBox.setText(usernames.get(index));
            passwordBox.setText(passwords.get(index));
            servernameBox.setText(serverNames.get(index));
            urlBox.setText(urls.get(index));

        });

    }


    public void saveClicked(ActionEvent actionEvent) {

        int indeks=serverNames.size()-1;
        serverNames.set(indeks,servernameBox.getText());
        usernames.set(indeks,usernameBox.getText());
        passwords.set(indeks,passwordBox.getText());
        urls.set(indeks,urlBox.getText());
        refresh();

        napuniXML();

    }

    private void napuniXML(){

        XMLEncoder izlaz = null;
        try {
            izlaz = new XMLEncoder(new FileOutputStream("databases.xml"));
            for(int i=0;i<urls.size();i++){
                izlaz.writeObject(serverNames.get(i));
                izlaz.writeObject(usernames.get(i));
                izlaz.writeObject(passwords.get(i));
                izlaz.writeObject(urls.get(i));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        izlaz.close();


    }

    public void exitClicked(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void newClicked(ActionEvent actionEvent) {

        serverNames.add("Base "+serverNames.size());
        usernames.add("Username "+ usernames.size());
        passwords.add("password");
        urls.add("Type database URL here");
        refresh();

    }

    private void refresh() {
        serversCBox.setItems(FXCollections.observableList(serverNames));
        usernameBox.clear();
        passwordBox.clear();
        servernameBox.clear();
        urlBox.clear();
        serversCBox.getSelectionModel().clearSelection();
    }

    public void offlineClicked(ActionEvent actionEvent) {

        ConnectionDAO.createOffline();
        gotoLogin(false);



    }

    private void gotoLogin(boolean online){

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/LoginScreen.fxml"),ConnectionDAO.getResourcebundle());
            LoginScreen controller=new LoginScreen();
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();
            Stage s=new Stage();
            s.setTitle("Login");

            s.setScene(new Scene(root));
            if(online==false) s.setOnHiding((e)-> Platform.exit());
            s.show();
            Stage currentStage= (Stage) usernameBox.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteClicked(ActionEvent actionEvent) {

        if(serversCBox.getSelectionModel().getSelectedItem()==null)return;
        int indeks=serversCBox.getSelectionModel().getSelectedIndex();
        serverNames.remove(indeks);
        usernames.remove(indeks);
        passwords.remove(indeks);
        urls.remove(indeks);
        napuniXML();
        refresh();

    }

    public void enterClicked(ActionEvent actionEvent) {

        if(serversCBox.getSelectionModel().getSelectedItem()==null)return;
        ConnectionDAO.createOnline(usernameBox.getText(),passwordBox.getText(),urlBox.getText());
        gotoLogin(true);


    }

    public void changeClicked(ActionEvent actionEvent) {

        if(ConnectionDAO.getResourcebundle().getLocale().toString().equals("en_US"))ConnectionDAO.setResourcebundle("Translation_bs");
        else ConnectionDAO.setResourcebundle("Translation_en_US");

        napuniXML();

        Stage s= (Stage) passwordBox.getScene().getWindow();
        s.close();

        try {


        FXMLLoader fxmlLoader=new FXMLLoader(getClass().getClassLoader().getResource("fxml/MainMenu.fxml"), ConnectionDAO.getResourcebundle());
        fxmlLoader.setController(new MainMenu());

        Parent root = fxmlLoader.load();
        s=new Stage();
        s.setScene(new Scene(root));
        s.show();

        } catch (IOException e) {
            e.printStackTrace();
        }




    }



}
