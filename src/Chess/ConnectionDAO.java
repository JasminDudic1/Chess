package Chess;

import java.io.File;
import java.sql.*;
import java.util.Properties;

public  class ConnectionDAO {

    private static ConnectionDAO instance = null;
    private Connection conn=null;

    private ConnectionDAO() {

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

    public static ConnectionDAO getInstance(){
        if(instance==null)instance=new ConnectionDAO();
        return instance;

    }

    public Connection getconn(){
        if(conn==null)getInstance();
        return conn;
    }

    public void makeBase(){

        File dbfile=new File("baza.db");
        dbfile.delete();
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:baza.db");
            String setup="CREATE TABLE \"player\" (\n" +
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
            Statement stm= conn.createStatement();
            stm.execute(setup);
             setup="CREATE TABLE \"room\" (\n" +
                    "\t\"id\"\tINTEGER PRIMARY KEY,\n" +
                    "\t\"chat\"\tTEXT NOT NULL,\n" +
                    "\t\"moves\"\tTEXT NOT NULL,\n" +
                    "\t\"white\"\tINTEGER DEFAULT 0,\n" +
                    "\t\"black\"\tINTEGER DEFAULT 0\n" +
                    //"\tPRIMARY KEY(\"id\")\n" +
                    ");";
            stm= conn.createStatement();
            stm.execute(setup);
            System.out.printf("Napravio");
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }



    }

}
