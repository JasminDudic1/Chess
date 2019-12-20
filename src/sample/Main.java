package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Stage stg;
    @Override
    public void start(Stage primaryStage) throws Exception{
        this.stg=primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("ChessRoom.fxml"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
