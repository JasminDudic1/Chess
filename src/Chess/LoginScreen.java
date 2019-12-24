package Chess;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class LoginScreen implements Initializable {


    public Button RegisterBtn;
    public Button LogInBtn;
    public TextField usernameTBox;
    public PasswordField passwordPBox;
    public CheckBox rememberMeCB;
    private int id=0;

    private String hash(String s){

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash*31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void registerClicked(ActionEvent actionEvent) {

     /*   Properties properties=new Properties();
        properties.setProperty("user","Jasa");
        properties.setProperty("password","1234");
        properties.setProperty("useSSL","false");
        properties.setProperty("serverTimezone","UTC");
        String url = "jdbc:mysql://77.78.232.142:3306/chess";*/
        String username=usernameTBox.getText();
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
                usernameTBox.clear();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void logInClicked(ActionEvent actionEvent) {

        String username=usernameTBox.getText();
        String password=passwordPBox.getText();

        try {
            System.out.printf("USAO");
            //Class.forName("com.mysql.cj.jdbc.Driver");
           // Connection conn = DriverManager.getConnection(url,properties);

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
            }else if(result.getInt(1)==1){
                Alert a=new Alert(Alert.AlertType.ERROR);
                a.setContentText("Already Logged in");
                a.setTitle("ERROR");
                a.setHeaderText("");
                a.show();
                passwordPBox.clear();
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
                //MainMenu.username=username;
                if(rememberMeCB.isSelected()) {

                    BufferedWriter writer = new BufferedWriter(new FileWriter("login.txt"));
                    writer.write(usernameTBox.getText());
                    writer.write("\n");
                    writer.write(passwordPBox.getText());
                    writer.close();

                }



                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainMenu.fxml"));
                    Parent root = (Parent) fxmlLoader.load();

                    MainMenu controller = fxmlLoader.getController();
                    controller.setUsername(usernameTBox.getText());
                    controller.setId(id);

                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setOnHiding(e->controller.stop());
                    stage.show();



                   // ((Stage)usernameTBox.getScene().getWindow()).close();
                } catch(Exception e) {
                    e.printStackTrace();
                }

                passwordPBox.clear();
                usernameTBox.clear();
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


        File file = new File("login.txt");
            if(file.exists()) {

                String str = usernameTBox.getText()+"\n"+passwordPBox.getText();
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("login.txt"));
                    usernameTBox.setText(reader.readLine());
                    passwordPBox.setText(reader.readLine());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

    }
}
