package Chess;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class Lobby implements Initializable {

    public String username = "";
    public ListView roomsCBox;
    public PasswordField passwordPBox;
    public Pane backgroundPane;
    public Label usernameLab;
    private TabPane tabsTabPane;
    private int loggedinID = 0;

    private String hash(String s) {

        int hash = 7;
        for (int i = 0; i < s.length(); i++) {
            hash = hash * 31 + s.charAt(i);
        }
        return Integer.toString(hash);
    }

    public void setLoggedinID(int loggedinID) {

        this.loggedinID = loggedinID;
        usernameLab.setText("Logged in as : " + username + " " + loggedinID);
    }

    public void setTabsTabPane(TabPane tabsTabPane) {
        this.tabsTabPane = tabsTabPane;
    }

    public void setUsername(String username) {

        this.username = username;
        usernameLab.setText("Logged in as : " + username);
    }

    public void exitClicked(ActionEvent actionEvent) {
        System.exit(0);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        new Thread(() -> {
            while (true) {

                try {
                    Platform.runLater(() -> refresh());
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        //refresh();


    }

    public void refresh() {


        Object selected = null;
        if (roomsCBox.getSelectionModel().getSelectedItem() != null)
            selected = roomsCBox.getSelectionModel().getSelectedItem();

        roomsCBox.getItems().clear();


        try {

            Connection conn = ConnectionDAO.getConn();
            Statement stmt = conn.createStatement();
            PreparedStatement selectStatus = conn.prepareStatement("Select online from player where id=" + loggedinID);
            ResultSet rs = selectStatus.executeQuery();
            if (!rs.next()) return;
            if (rs.getInt(1) == 0) {
                new Alert(Alert.AlertType.ERROR, ConnectionDAO.getResourcebundle().getString("gotloggedout"));
                Stage s = (Stage) tabsTabPane.getScene().getWindow();
                s.close();
            }
            PreparedStatement selectRooms = conn.prepareStatement("Select id,roomName,length(moves),white,black From room where white!=? and black!=?");
            selectRooms.setInt(1, loggedinID);
            selectRooms.setInt(2, loggedinID);
            ResultSet result = selectRooms.executeQuery();
            while (result.next()) {

                if (result.getInt(4) == 0 && result.getInt(5) == 0) {
                    ConnectionDAO.removeRoom(result.getInt(1));
                    continue;
                }

                if (result.getInt(3) > 1 && (result.getInt(4) == 0 || result.getInt(5) == 0)) continue;

                String s = result.getInt(1) + " : " + result.getString(2);
                selectRooms = conn.prepareStatement("Select white,black,password From room where id=? limit 1");
                selectRooms.setInt(1, Integer.parseInt(s.substring(0, s.indexOf(":") - 1)));

                ResultSet result2 = selectRooms.executeQuery();
                result2.next();

                if (result2.getInt(1) == 0) s += ":White Empty";
                else if (result2.getInt(2) == 0) s += ":Black Empty";
                else s += ":Spectate";

                if (!result2.getString(3).isEmpty()) s += ":password";

                roomsCBox.getItems().add(s);

                result2.close();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (selected != null && roomsCBox.getItems().contains(selected))
            roomsCBox.getSelectionModel().select(selected);


    }

    public void enterClicked(ActionEvent actionEvent) {


        if (roomsCBox.getSelectionModel().getSelectedItem() == null) return;

        ChessPiece.Color bojaIgraca = null;
        String s = roomsCBox.getSelectionModel().getSelectedItem().toString();
        roomsCBox.getSelectionModel().select(null);
        int pom = Integer.parseInt(s.substring(0, s.indexOf(':') - 1));
        String roomName = "";


        Connection conn = ConnectionDAO.getConn();
        PreparedStatement upit = null;
        try {
            upit = conn.prepareStatement("Select white,black,password,roomname from room where id=? ");
            upit.setInt(1, pom);
            ResultSet result = upit.executeQuery();
            result.next();
            roomName = result.getString(4);
            String pass = "";
            if (!passwordPBox.getText().isEmpty()) pass = hash(passwordPBox.getText());
            if (!pass.equals(result.getString(3))) {
                passwordPBox.clear();
                 return;
            }

            if (result.getInt(2) == 0) bojaIgraca = ChessPiece.Color.BLACK;
            else if (result.getInt(1) == 0) bojaIgraca = ChessPiece.Color.WHITE;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {


            if (bojaIgraca == ChessPiece.Color.BLACK)
                upit = conn.prepareStatement("Update room set black=? where id=?");
            else if (bojaIgraca == ChessPiece.Color.WHITE)
                upit = conn.prepareStatement("Update room set white=? where id=?");

            if (bojaIgraca != null) {
                upit.setInt(1, loggedinID);
                upit.setInt(2, pom);
                upit.executeUpdate();
            }

            ChessRoom controller = new ChessRoom();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/ChessRoom.fxml"),ConnectionDAO.getResourcebundle());
            fxmlLoader.setController(controller);
            Parent root = (Parent) fxmlLoader.load();

            controller.setRoomId(pom);
            if (bojaIgraca == null) {
                controller.setRoomId(pom);
            }
            controller.draw(bojaIgraca);

            Tab tab = new Tab(pom + ":" + roomName, root);

            controller.setCurrentTab(tab);

            tabsTabPane.getTabs().add(tab);
            tabsTabPane.getSelectionModel().select(tab);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void createClicked(ActionEvent actionEvent) {


        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/CreateRoom.fxml"),ConnectionDAO.getResourcebundle());
            CreateRoom controller = new CreateRoom();
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();
            controller.setId(loggedinID);

            Tab tab = new Tab("Creating room", root);
            controller.setCurrentTab(tab);
            tabsTabPane.getTabs().add(tab);
            tabsTabPane.getSelectionModel().select(tab);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void profilesClicked(ActionEvent actionEvent) {

        try {

            for (Tab t : tabsTabPane.getTabs())
                if (t.getText() == "Profiles") {
                    tabsTabPane.getSelectionModel().select(t);
                    return;
                }

            ProfilePage controller = new ProfilePage();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/ProfilePage.fxml"),ConnectionDAO.getResourcebundle());
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();

            controller.setPlayer(loggedinID, loggedinID);


            Tab tab = new Tab("Profiles", root);
            controller.setCurrentTab(tab);

            tabsTabPane.getTabs().add(tab);
            tabsTabPane.getSelectionModel().select(tab);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void logoutClicked(ActionEvent actionEvent) throws SQLException {

        Connection conn = ConnectionDAO.getConn();
        PreparedStatement ps = conn.prepareStatement("Update player set online=0 where id=?");
        ps.setInt(1, loggedinID);
        ps.executeUpdate();

        Stage s = (Stage) passwordPBox.getScene().getWindow();
        s.close();
        if (ConnectionDAO.isOnline()) {

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/LoginScreen.fxml"),ConnectionDAO.getResourcebundle());
                LoginScreen controller = new LoginScreen();
                fxmlLoader.setController(controller);
                Parent root = (Parent) fxmlLoader.load();
                s = new Stage();
                s.setTitle("Login");
                s.setScene(new Scene(root));
                s.show();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


    }

    public void exit() {

        ConnectionDAO.logout(loggedinID);
        Stage s = (Stage) tabsTabPane.getScene().getWindow();
        s.close();


    }
}
