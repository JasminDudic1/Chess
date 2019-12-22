package Chess;

import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class ChessRoom implements Initializable {

    public GridPane boardGridPane;
    private Board b;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    b=new Board(boardGridPane);


    }
}
