package sample;

import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ChessRoom implements Initializable {


    public GridPane boardGridPane;
    private Label[][] UIboard=new Label[8][8];
    private Board b;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    b=new Board(boardGridPane);


    }
}
