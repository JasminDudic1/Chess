package Chess;

import javafx.scene.control.Alert;
import net.sf.jasperreports.engine.JRException;

import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class ConnectionDAO {

    private static ConnectionDAO instance = null;
    private static Connection conn = null;
    private static PreparedStatement maxRoom, maxPast, maxPlayer;
    private static ResourceBundle resourcebundle=ResourceBundle.getBundle("Translation");

    public static ResourceBundle getResourcebundle() {
        return resourcebundle;
    }

    public static void setResourcebundle(String s) {
        resourcebundle=ResourceBundle.getBundle(s);
    }

    public static boolean isOnline() {
        return online;
    }

    private static boolean online =false;

    public static void createOffline() {

        online =false;
        //region SQLITE
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            testBaseOffline();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }



        try {
            maxPlayer = conn.prepareStatement("select max(id) from player");
            maxPast = conn.prepareStatement("select max(id) from pastgames");
            maxRoom = conn.prepareStatement("select max(id) from room");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //endregion

    }

    private static void testBaseOffline(){

        try {
            PreparedStatement ps = conn.prepareStatement("Select * from player");
            ps.executeQuery();
            ps = conn.prepareStatement("Select * from room");
            ps.executeQuery();
            ps = conn.prepareStatement("Select * from pastgames");
            ps.executeQuery();
        } catch (SQLException e) {
            makeNewBaseOffline();
        }

    }

    private static void makeNewBaseOffline() {

        try {
            String setup = "";
            Statement stm;



            try{
                PreparedStatement ps=conn.prepareStatement("Drop table if exists player");
                ps.executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            try{
                PreparedStatement ps=conn.prepareStatement("Drop table if exists room");
                ps.executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            try{
                PreparedStatement ps=conn.prepareStatement("Drop table if exists pastgames");
                ps.executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            setup = "CREATE TABLE \"player\" (\n" +
                    "\t\"id\"\tINTEGER PRIMARY KEY,\n" +
                    "\t\"username\"\tTEXT NOT NULL,\n" +
                    "\t\"password\"\tTEXT NOT NULL,\n" +
                    "\t\"wins\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"loss\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"draw\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"rating\"\tINTEGER DEFAULT 1000,\n" +
                    "\t\"online\"\tINTEGER DEFAULT 0\n" +
                    ");";
            stm = conn.createStatement();
            stm.execute(setup);

            setup = "CREATE TABLE \"room\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                    "\t\"moves\"\tTEXT NOT NULL,\n" +
                    "\t\"white\"\tINTEGER NOT NULL,\n" +
                    "\t\"black\"\tINTEGER NOT NULL,\n" +
                    "\t\"roomName\"\tTEXT NOT NULL,\n" +
                    "\t\"password\"\tTEXT NOT NULL\n" +
                    ");";

            stm = conn.createStatement();
            stm.execute(setup);

            setup = "CREATE TABLE \"pastgames\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL,\n" +
                    "\t\"white\"\tINTEGER NOT NULL,\n" +
                    "\t\"black\"\tINTEGER NOT NULL,\n" +
                    "\t\"winner\"\tTEXT NOT NULL,\n" +
                    "\t\"moves\"\tTEXT NOT NULL,\n" +
                    "\t\"date\"\tTEXT NOT NULL,\n" +
                    "\t\"roomid\"\tINTEGER NOT NULL,\n" +
                    "\tPRIMARY KEY(\"id\")\n" +
                    ");";

            stm = conn.createStatement();
            stm.execute(setup);


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static void createOnline(String user, String pass, String url) {

        online =true;
        try {
            Properties properties = new Properties();
            properties.setProperty("user", user);
            properties.setProperty("password", pass);
            properties.setProperty("useSSL", "false");
            properties.setProperty("serverTimezone", "UTC");
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, properties);
            testBaseOnline();
        } catch (ClassNotFoundException | SQLException e) {
            (new Alert(Alert.AlertType.ERROR,"Error connecting to that database")).show();
            e.printStackTrace();
        }
        try {
            maxPlayer = conn.prepareStatement("select max(id) from player");
            maxPast = conn.prepareStatement("select max(id) from pastgames");
            maxRoom = conn.prepareStatement("select max(id) from room");
        } catch (SQLException e) {
            e.printStackTrace();
        }




    }

    private static void testBaseOnline(){

        try {
            PreparedStatement ps = conn.prepareStatement("Select * from player");
            ps.executeQuery();
            ps = conn.prepareStatement("Select * from room");
            ps.executeQuery();
            ps = conn.prepareStatement("Select * from pastgames");
            ps.executeQuery();
        } catch (SQLException e) { makeNewBaseOnline(); }

    }

    private static void makeNewBaseOnline() {

        try {
            String setup = "";
            Statement stm;



            try{
                PreparedStatement ps=conn.prepareStatement("Drop table if exists player");
                ps.executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            try{
                PreparedStatement ps=conn.prepareStatement("Drop table if exists room");
                ps.executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            try{
                PreparedStatement ps=conn.prepareStatement("Drop table if exists pastgames");
                ps.executeUpdate();
            }catch(Exception ex){
                ex.printStackTrace();
            }

            setup = "CREATE TABLE player (" +
                    "id int AUTO_INCREMENT PRIMARY KEY ," +
                    "username varchar(255) ," +
                    "password varchar(255) ," +
                    "wins int ," +
                    "loss int ," +
                    "draw int ," +
                    "rating int ," +
                    "online int " +
                    ");";
            stm = conn.createStatement();
            stm.execute(setup);

            setup = "CREATE TABLE room ( "+
                    "id int AUTO_INCREMENT PRIMARY KEY," +
                    "moves text," +
                    "white int ," +
                    "black int ," +
                    "roomName varchar(255) ," +
                    "password varchar(255)" +
                    ");";

            stm = conn.createStatement();
            stm.execute(setup);

            setup = "CREATE TABLE pastgames (" +
                    "id integer AUTO_INCREMENT PRIMARY KEY ," +
                    "white int ," +
                    "black int ," +
                    "winner int ," +
                    "moves text ," +
                    "date text ," +
                    "roomid int " +
                    ");";

            stm = conn.createStatement();
            stm.execute(setup);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static Connection getConn() {
        return conn;
    }

    public static int maxPastGamesId() {

        int pastid = 0;
        try {
            ResultSet rs = maxPast.executeQuery();
            if (!rs.next()) pastid = 1;
            else pastid = rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pastid;

    }

    public static void logout(int playerID) {

        try {
            PreparedStatement logoutWhite = conn.prepareStatement("update room set white=0 where white=?");
            logoutWhite.setInt(1, playerID);
            PreparedStatement logoutBlack = conn.prepareStatement("update room set black=0 where black=?");
            logoutBlack.setInt(1, playerID);
            PreparedStatement logout = conn.prepareStatement("Update player set online=0 where id=?");
            logout.setInt(1, playerID);
            logoutWhite.executeUpdate();
            logoutBlack.executeUpdate();
            logout.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void removeRoom(int roomID) {

        try {
            PreparedStatement removeRoom = conn.prepareStatement("Delete from room where id=" + roomID);
            removeRoom.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void resetPlayer(int id){


        try {
            PreparedStatement resetPlayer=conn.prepareStatement("Update player set wins=0, loss=0, draw=0, rating=700 where id=?");
            resetPlayer.setInt(1,id);
            resetPlayer.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public static void generateReport(){

        try {
            new ChessReport().showReport(conn);
        } catch ( JRException e1) {
            e1.printStackTrace();
        }

    }


}
