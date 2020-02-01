package Chess;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Stage stg;
    @Override
    public void start(Stage primaryStage) throws Exception{


        ConnectionDAO.createConn();
        ConnectionDAO.makeBase();

        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/LoginScreen.fxml"));

        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnHiding((e)-> Platform.exit());

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
