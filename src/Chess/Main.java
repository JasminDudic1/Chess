package Chess;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Stage stg;
    @Override
    public void start(Stage primaryStage) throws Exception{

        MainMenu controller=new MainMenu();
        FXMLLoader fxmlLoader=new FXMLLoader(getClass().getClassLoader().getResource("fxml/MainMenu.fxml"), ConnectionDAO.getResourcebundle());
        fxmlLoader.setController(controller);

        Parent root = fxmlLoader.load();

        primaryStage.setTitle("MainMenu");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
