package Chess;

import java.io.File;
import java.sql.*;
import java.util.Properties;

public  class ConnectionDAO {

    private static ConnectionDAO instance = null;
    private static Connection conn=null;
    private static PreparedStatement maxRoom,maxPast,maxPlayer;

    public static void createConn(){

        int pom=1;

        //region MYSQL
        if(pom==0) {
            try {
                Properties properties = new Properties();
                properties.setProperty("user", "Jasa");
                properties.setProperty("password", "1234");
                properties.setProperty("useSSL", "false");
                properties.setProperty("serverTimezone", "UTC");
                String url = "jdbc:mysql://77.78.232.142:3306/chess";
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(url, properties);
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        //endregion

        //region SQLITE
        if(pom==1) {
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        //endregion


    }

    public static Connection getConn(){
        return conn;
    }


    public static void makeBase(){



        File dbfile=new File("baza.db");
        try {
            dbfile.delete();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        try {
            String setup="DROP TABLE player";
            Statement stm= conn.createStatement();
            try {
                stm.executeUpdate(setup);
            }catch(Exception ex){}

            try {
                setup = "DROP TABLE room";
                stm = conn.createStatement();
                stm.executeUpdate(setup);
            }catch(Exception e){}

          /*  try {
                setup = "DROP TABLE pastgames";
                stm = conn.createStatement();
                stm.executeUpdate(setup);
            }catch(Exception e){}*/

             setup="CREATE TABLE \"player\" (\n" +
                    "\t\"id\"\tINTEGER PRIMARY KEY,\n" +
                    "\t\"username\"\tTEXT NOT NULL,\n" +
                    "\t\"password\"\tTEXT NOT NULL,\n" +
                    "\t\"wins\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"loss\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"draw\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"rating\"\tINTEGER DEFAULT 1000,\n" +
                    "\t\"online\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"challenges\"\tINTEGER DEFAULT 0\n" +
                  //  "\tPRIMARY KEY(\"id\")\n" +
                    ");";
             stm= conn.createStatement();
            stm.execute(setup);
             setup="CREATE TABLE \"room\" (\n" +
                     "\t\"id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                     "\t\"chat\"\tTEXT NOT NULL,\n" +
                     "\t\"moves\"\tTEXT NOT NULL,\n" +
                     "\t\"white\"\tINTEGER NOT NULL,\n" +
                     "\t\"black\"\tINTEGER NOT NULL,\n" +
                     "\t\"roomName\"\tTEXT NOT NULL,\n" +
                     "\t\"password\"\tTEXT NOT NULL\n" +
                     ");";

            stm= conn.createStatement();
            stm.execute(setup);

            /*setup="CREATE TABLE \"pastgames\" (\n" +
                    "\t\"id\"\tINTEGER NOT NULL,\n" +
                    "\t\"white\"\tINTEGER NOT NULL,\n" +
                    "\t\"black\"\tINTEGER NOT NULL,\n" +
                    "\t\"winner\"\tTEXT NOT NULL,\n" +
                    "\t\"moves\"\tTEXT NOT NULL,\n" +
                    "\t\"date\"\tTEXT NOT NULL,\n" +
                    "\t\"roomid\"\tINTEGER NOT NULL,\n" +
                    "\tPRIMARY KEY(\"id\")\n" +
                    ");";

            stm=conn.createStatement();
            stm.execute(setup);*/

            System.out.printf("Napravio");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            maxPlayer=conn.prepareStatement("select max(id) from player");
            maxPast=conn.prepareStatement("select max(id) from pastgames");
            maxRoom=conn.prepareStatement("select max(id) from room");
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

    public static int maxRoomId(){

        int roomid=0;
        try {
            ResultSet rs=maxRoom.executeQuery();
            if(!rs.next())roomid=1;
            else roomid= rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roomid;
    }

    public static int maxPastGamesId(){

        int pastid=0;
        try {
            ResultSet rs=maxPast.executeQuery();
            if(!rs.next())pastid=1;
            else pastid= rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pastid;

    }

    public static int maxPlayerID(){

        int playerid=0;
        try {
            ResultSet rs=maxPlayer.executeQuery();
            if(!rs.next())playerid=1;
            else playerid= rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerid;

    }
}
