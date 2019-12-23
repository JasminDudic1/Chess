package Chess;

import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ChessRoom implements Initializable {

    public GridPane boardGridPane;
    private Board b;
    private int roomId;

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        //draw(ChessPiece.Color.BLACK);

    }

    public void draw(ChessPiece.Color bojaIgraca){
        b=new Board(boardGridPane, bojaIgraca);
    }

    public void stop(){

        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:baza.db");

            Statement stmt = conn.createStatement();
            PreparedStatement upit=conn.prepareStatement("select id from room where id=? and (white=0 or black=0)");
            upit.setInt(1,roomId);
            ResultSet result = upit.executeQuery();

            if(result.next()){
                upit=conn.prepareStatement("delete from room where id=? ");
                upit.setInt(1,roomId);
                upit.executeUpdate();
            }

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


    }

}
