package Chess;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Board {

    public static int jede = 0;
    private ChessRoom controller = null;
    private PreparedStatement getMoves, setMoves;
    private String castle1 = "", passant1 = "";
    private String selectedPozicija = "";
    private ChessPiece lastMoved = null;
    private int currentBoard = 0;
    private boolean isImport = false, isSpectate = false, picking = false, playing = true, gameReady = false;
    private String moves = "";
    private Label colorLab;
    private int whiteID, blackID, roomId = 0;
    private ChessPiece.Color turnColor = ChessPiece.Color.WHITE;
    private ChessPiece.Color currentPlayer = ChessPiece.Color.WHITE;
    private ChessPiece[][] board = new ChessPiece[2][];
    private Label[][] UIboard = new Label[8][];
    private ChessPiece[][][] allBoard;
    private ArrayList<String> importMoves;

    public Board(GridPane boardGridPane, ChessPiece.Color bojaIgraca) {

        currentPlayer = bojaIgraca;
        playing = true;

        for (int i = 0; i < 8; i++)
            UIboard[i] = new Label[8];

        board[0] = new ChessPiece[16];
        board[1] = new ChessPiece[16];

        //region Pieces

        //region Pawns
        for (int i = 0; i < 8; i++) {
            board[0][8 + i] = new Pawn((char) ('a' + i) + "2", ChessPiece.Color.WHITE);
            board[1][8 + i] = new Pawn((char) ('a' + i) + "7", ChessPiece.Color.BLACK);
        }
        //endregion

        //region Rooks
        board[0][0] = new Rook("a1", ChessPiece.Color.WHITE);
        board[0][7] = new Rook("h1", ChessPiece.Color.WHITE);
        board[1][0] = new Rook("a8", ChessPiece.Color.BLACK);
        board[1][7] = new Rook("h8", ChessPiece.Color.BLACK);
        //endregion

        //region Knight
        board[0][1] = new Knight("b1", ChessPiece.Color.WHITE);
        board[0][6] = new Knight("g1", ChessPiece.Color.WHITE);
        board[1][1] = new Knight("b8", ChessPiece.Color.BLACK);
        board[1][6] = new Knight("g8", ChessPiece.Color.BLACK);
        //endregion

        //region Bishop
        board[0][2] = new Bishop("c1", ChessPiece.Color.WHITE);
        board[0][5] = new Bishop("f1", ChessPiece.Color.WHITE);
        board[1][2] = new Bishop("c8", ChessPiece.Color.BLACK);
        board[1][5] = new Bishop("f8", ChessPiece.Color.BLACK);
        //endregion

        //region Queen
        board[0][3] = new Queen("d1", ChessPiece.Color.WHITE);
        board[1][3] = new Queen("d8", ChessPiece.Color.BLACK);
        //endregion

        //region King
        board[0][4] = new King("e1", ChessPiece.Color.WHITE);
        board[1][4] = new King("e8", ChessPiece.Color.BLACK);
        //endregion

        //endregion

        for (int j = 0; j < 8; j++) {
            Label l1 = new Label();
            char c = (char) ('A' + j);
            l1.setText("" + c);
            labSetup(l1);
            boardGridPane.add(l1, j, 0);
            Label l2 = new Label();
            l2.setText("" + (8 - j));
            labSetup(l2);
            if (bojaIgraca == ChessPiece.Color.WHITE)
                boardGridPane.add(l2, 8, j + 1);
            else boardGridPane.add(l2, 8, 8 - j);
        }

        for (int i = 0; i < 8; i++) {
            UIboard[i] = new Label[8];
            for (int j = 0; j < 8; j++) {
                UIboard[i][j] = new Label();
                UIboard[i][j].onMouseClickedProperty().setValue(this::Clicked);
                UIboard[i][j].setPrefHeight(70);
                UIboard[i][j].setPrefWidth(70);
                UIboard[i][j].setAlignment(Pos.CENTER);
                String pos = "" + (char) ('A' + i) + (j + 1);
                UIboard[i][j].setId(pos);
                if (bojaIgraca == ChessPiece.Color.WHITE) boardGridPane.add(UIboard[i][j], i, 8 - j);
                else boardGridPane.add(UIboard[i][j], i, j + 1);

            }
        }


        refresh();
    }

    public void setController(ChessRoom controller) {
        this.controller = controller;
    }

    private void addCurrentBoard() {

        for (int i = 0; i < 2; i++)
            for (int j = 0; j < 16; j++) {
                if (board[i][j].getZnak() == 'P') allBoard[currentBoard][i][j] = new Pawn(board[i][j]);
                else if (board[i][j].getZnak() == 'Q') allBoard[currentBoard][i][j] = new Queen(board[i][j]);
                else if (board[i][j].getZnak() == 'K') allBoard[currentBoard][i][j] = new King(board[i][j]);
                else if (board[i][j].getZnak() == 'R') allBoard[currentBoard][i][j] = new Rook(board[i][j]);
                else if (board[i][j].getZnak() == 'N') allBoard[currentBoard][i][j] = new Knight(board[i][j]);
                else if (board[i][j].getZnak() == 'B') allBoard[currentBoard][i][j] = new Bishop(board[i][j]);
            }

        currentBoard++;

    }

    void move(String oldPosition, String newPosition) {

        if (!playing) return;

        char newPiece = ' ';
        try {

            if (newPosition.length() == 3) {
                newPiece = newPosition.charAt(2);
                newPosition = newPosition.substring(0, 2);
            }

            ChessPiece c1 = naLokaciji(newPosition);
            ChessPiece c2 = naLokaciji(oldPosition);


            testMove(oldPosition, newPosition);

            //region Castling
            newPosition = newPosition.toLowerCase();
            castle1 = castle1.toLowerCase();

            if (newPosition.equals(castle1)) {


                String rookStr = "xx";

                c2.postaviNa(castle1);

                if (castle1.charAt(0) == 'c') {
                    int pom = castle1.charAt(1) - '0';
                    rookStr = "a" + pom;
                    naLokaciji(rookStr).postaviNa("d" + pom);
                } else if (castle1.charAt(0) == 'g') {
                    int pom = castle1.charAt(1) - '0';
                    rookStr = "h" + pom;
                    naLokaciji(rookStr).postaviNa("f" + pom);
                }
                castle1 = "";


            }
            //endregion

            //region EnPassant
            if (newPosition.equals(passant1)) {

                if (c2.getColor() == ChessPiece.Color.WHITE) {
                    String passant = "" + newPosition.charAt(0);
                    char pom = (char) (newPosition.charAt(1) - 1);
                    passant += pom;
                    c1 = naLokaciji(passant);
                    c1.postaviNa("x");
                    c2.postaviNa(passant1);
                }
                if (c2.getColor() == ChessPiece.Color.BLACK) {
                    String passant = "" + newPosition.charAt(0);
                    char pom = (char) (newPosition.charAt(1) + 1);
                    passant += pom;
                    c1 = naLokaciji(passant);
                    c1.postaviNa("x");
                    c2.postaviNa(passant1);
                }
                passant1 = "";

            }
            //endregion

            newPosition = newPosition.toLowerCase();
            castle1 = castle1.toLowerCase();

            c2.moved();
            lastMoved = c2;

            if (c1 != null)
                c1.postaviNa("X");

            if (newPiece != ' ') {


                for (int i = 0; i < 2; i++)
                    for (int j = 0; j < 16; j++)
                        if (board[i][j] == lastMoved) {
                            //region Change
                            if (newPiece == 'D') board[i][j] = new Queen(newPosition, c2.getColor());
                            else if (newPiece == 'N') board[i][j] = new Knight(newPosition, c2.getColor());
                            else if (newPiece == 'B') board[i][j] = new Bishop(newPosition, c2.getColor());
                            else if (newPiece == 'R') board[i][j] = new Rook(newPosition, c2.getColor());
                            //endregion
                        }

                return;
            }

            if (isImport || isSpectate) return;

            if (c2.getClass() == Pawn.class && (newPosition.charAt(1) == '8' || newPosition.charAt(1) == '1') && newPiece == ' ') {
                moves += " (" + oldPosition.toLowerCase() + "-" + newPosition.toLowerCase();
                picking = true;

            } else if (currentPlayer == turnColor) {
                moves += " (" + oldPosition.toLowerCase() + "-" + newPosition.toLowerCase() + ") ,";
                setMoves.setString(1, moves);
                setMoves.executeUpdate();
                controller.getCurrentTab().setStyle("-fx-background-color: Green;");
                refresh();
                changePlayer();
            }

        } catch (Exception e) {
            throw new IllegalChessMoveException(e.toString());
        }


    }///////////////////////////////////OVDJE SAM

    public void setPlayersIds(int p1, int p2) {

        whiteID = p1;
        blackID = p2;
        if (currentPlayer == ChessPiece.Color.BLACK) return;
        Connection conn = ConnectionDAO.getConn();
        try {
            PreparedStatement ps = conn.prepareStatement("Update room set white=?,black=? where id=?");
            ps.setInt(1, p1);
            ps.setInt(2, p2);
            ps.setInt(3, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public ChessPiece.Color getCurrentPlayer() {
        return currentPlayer;
    }

    public void setColorLab(Label lab) {
        this.colorLab = lab;
        colorLab.setStyle("-fx-background-color: Gray;-fx-border-color: Gray");
    }

    public void setGameReady() {

        gameReady = true;
        colorLab.setStyle("-fx-background-color: WHITE;-fx-border-color: BLACK;-fx-text-fill: BLACK;");
        setupGettingMovesFromDB();

    }

    public void endGame() {
        playing = false;
    }

    private void setupGettingMovesFromDB() {

        try {
            getMoves = ConnectionDAO.getConn().prepareStatement("Select moves,black,white from room where id=?");
            getMoves.setInt(1, roomId);
            setMoves = ConnectionDAO.getConn().prepareStatement("Update room set moves=? where id=? and white>0 and black>0");
            setMoves.setInt(2, roomId);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            while (true) {


                try {
                    if (playing == true) Platform.runLater(() -> checkBaseForNewMoves());
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();


    }

    private void checkBaseForNewMoves() {

        if (turnColor == currentPlayer) return;

        Connection conn = ConnectionDAO.getConn();
        try {

            ResultSet res = getMoves.executeQuery();
            ArrayList<String> temp;
            if (res.next()) {

                if (res.getInt(2) == 0 || res.getInt(3) == 0) {


                    if (currentPlayer == ChessPiece.Color.WHITE) {
                        giveWin(whiteID, blackID);
                        giveLoss(blackID);
                    } else if (currentPlayer == ChessPiece.Color.BLACK) {
                        giveWin(blackID, whiteID);
                        giveLoss(whiteID);
                    }

                    new Alert(Alert.AlertType.INFORMATION, ConnectionDAO.getResourcebundle().getString("opponentleftalert")).show();

                    controller.closeRoom();
                    return;
                }

                String baseMoves = res.getString(1);

                if (baseMoves.isEmpty()) return;
                temp = new ArrayList<String>(Arrays.asList(baseMoves.split(",")));

                if (!baseMoves.equals(moves)) {
                    controller.getCurrentTab().setStyle("-fx-background-color: Red;");
                    String move = temp.get(temp.size() - 1);
                    String oldPosition = move.substring(move.indexOf("(") + 1, move.indexOf("-"));
                    String newPosition = move.substring(move.indexOf("-") + 1, move.indexOf(")"));
                    moves = baseMoves;
                    move(oldPosition, newPosition);
                    refresh();
                    changePlayer();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void setRoomId(int id) {
        this.roomId = id;
    }

    private void labSetup(Label l1) {
        l1.setAlignment(Pos.CENTER);
        l1.setStyle("-fx-background-color: gray;-fx-text-fill: black;");
        l1.setPrefHeight(70);
        l1.setPrefWidth(70);
    }

    boolean isCheck(ChessPiece.Color boja) {

        int bojaInt = 0;
        if (boja == ChessPiece.Color.BLACK) bojaInt = 1;

        ChessPiece kralj = board[bojaInt][4];
        String pozicija = kralj.getPosition();

        for (ChessPiece c : board[1 - bojaInt]) {
            jede = 1;
            if (!praznaPutanja(c.getPosition(), kralj.getPosition()) && c.getClass() != Knight.class)
                continue;//ako nije przna putanja dalje
            String staraPozicija = c.getPosition();
            try {
                c.move(pozicija);//probja pomjerit figuru na poziciju kralja
                c.postaviNa(staraPozicija);//ako moze vrati nazad
                jede = 0;//resetuj i zavrsio
                return true;
            } catch (Exception e) {
                continue;
            }
        }
        jede = 0;
        return false;
    }

    boolean isCheckMate(ChessPiece.Color boja) {

        int bojaIgraca = 0;
        if (boja == ChessPiece.Color.BLACK) bojaIgraca = 1;
        int possibleMoves = 0;

        for (int i = 0; i < 16; i++) {
            if (board[bojaIgraca][i].getPosition() == "X") continue;

            for (char c = 'a'; c <= 'h'; c++)
                for (int j = 1; j <= 8; j++) {
                    jede = 0;
                    String newPosition = "" + c + j;
                    String oldPosition = board[bojaIgraca][i].getPosition();

                    try {
                        testMove(oldPosition, newPosition);
                        board[bojaIgraca][i].postaviNa(oldPosition);

                        return false;

                    } catch (Exception e) {

                    }


                }

        }

        return true;

    }

    private ChessPiece naLokaciji(String lokacija) {
        if (lokacija.length() != 2) return null;
        lokacija = lokacija.toLowerCase();
        for (int i = 0; i <= 1; i++)
            for (int j = 0; j <= 15; j++) {
                if (board[i][j].getPosition().equals(lokacija)) return board[i][j];
            }

        return null;

    }

    private boolean praznaPutanja(String pocetnaPozicija, String krajnjaPozicija) {
        if (pocetnaPozicija.length() != 2 || krajnjaPozicija.length() != 2) return false;
        char iFigure = pocetnaPozicija.charAt(0);
        char jFigure = pocetnaPozicija.charAt(1);

        char iPozicije = krajnjaPozicija.charAt(0);
        char jPozicije = krajnjaPozicija.charAt(1);

        while (!("" + iFigure + jFigure).equals(krajnjaPozicija)) {

            if (iFigure < iPozicije) iFigure++;//pomjeranje po koodrinatama
            else if (iFigure > iPozicije) iFigure--;//ako je u koso obe uvecava/smanjuje
            if (jFigure < jPozicije) jFigure++;//za konja ne gleda jer ne treba
            else if (jFigure > jPozicije) jFigure--;//za topa ne uvecava jednu (zato nije <=/>= )

            if (("" + iFigure + jFigure).equals(krajnjaPozicija)) break;
            ChessPiece testni = naLokaciji("" + iFigure + jFigure);
            if (testni != null) return false;


        }
        return true;

    }

    public void changePlayer() {

        if (turnColor == ChessPiece.Color.WHITE) {
            turnColor = ChessPiece.Color.BLACK;
            colorLab.setStyle("-fx-background-color: BLACK;-fx-border-color: WHITE;-fx-text-fill: WHITE;");

        } else if (turnColor == ChessPiece.Color.BLACK) {
            colorLab.setStyle("-fx-background-color: WHITE;-fx-border-color: BLACK;-fx-text-fill: BLACK;");
            turnColor = ChessPiece.Color.WHITE;
        }

    }

    public void Clicked(MouseEvent mouseEvent) {


        if (turnColor != currentPlayer || !gameReady || isSpectate) return;

        Label label = (Label) mouseEvent.getSource();

        String pozicija = label.getId().substring(0, 2);

        ChessPiece naLok = naLokaciji(pozicija);

        //region Picked
        if (picking == true) {
            char c = ' ';
            int i = 0, j = 0;
            for (i = 0; i < 2; i++)
                for (j = 0; j < 16; j++)
                    if (board[i][j] == lastMoved) {
                        //region Change
                        if (pozicija.equals("D4")) {
                            board[i][j] = new Queen(lastMoved.getPosition(), lastMoved.getColor());
                            picking = false;
                            refresh();
                            c = 'D';
                        } else if (pozicija.equals("E4")) {
                            board[i][j] = new Bishop(lastMoved.getPosition(), lastMoved.getColor());
                            picking = false;
                            refresh();
                            c = 'B';

                        } else if (pozicija.equals("D5")) {
                            board[i][j] = new Knight(lastMoved.getPosition(), lastMoved.getColor());
                            picking = false;
                            refresh();
                            c = 'N';
                        } else if (pozicija.equals("E5")) {
                            board[i][j] = new Rook(lastMoved.getPosition(), lastMoved.getColor());
                            picking = false;
                            refresh();
                            c = 'R';
                        }
                        if (c != ' ') {

                            if (currentPlayer == turnColor) {
                                moves += c + ") ,";
                                try {
                                    setMoves.setString(1, moves);
                                    setMoves.executeUpdate();
                                    controller.getCurrentTab().setStyle("-fx-background-color: Green;");
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                        //endregion
                    }
            changePlayer();
            return;
        }

        //endregion

        if (selectedPozicija == "")
            if (naLok != null)
                if (naLok.getColor() != currentPlayer) return;

        //region Clicked

        if (naLok != null) {//ako nije klikni na nesto


            if (selectedPozicija.equals(pozicija)) {

                selectedPozicija = "";
                refresh();
            } else if (!selectedPozicija.isEmpty()) {

                try {

                    move(selectedPozicija, pozicija);

                } catch (Exception ex) {
                }

                selectedPozicija = "";
                refresh();


            } else {

                refresh();
                label.setStyle("-fx-background-color: green;-fx-text-fill: gray;");

                selectedPozicija = pozicija;
                check(pozicija);
            }


        } else if (selectedPozicija.length() > 0) {

            try {
                move(selectedPozicija, pozicija);
            } catch (Exception ex) {
            }

            selectedPozicija = "";
            refresh();

        } else {
            selectedPozicija = "";
            refresh();
        }

        //endregion


    }

    void testMove(String oldPosition, String newPosition) {

        if (oldPosition == newPosition) throw new IllegalChessMoveException("Ista pozicija");

        ChessPiece.Color boja = naLokaciji(oldPosition).getColor();

        oldPosition = oldPosition.toLowerCase();
        newPosition = newPosition.toLowerCase();
        ChessPiece staraLokacijaFigura = naLokaciji(oldPosition);
        ChessPiece novaLokacijaFigura = naLokaciji(newPosition);

        //region path and eat
        if (novaLokacijaFigura != null)
            if (staraLokacijaFigura.getColor() == novaLokacijaFigura.getColor())
                throw new IllegalArgumentException("Ista boja");

        if (staraLokacijaFigura == null) throw new IllegalArgumentException("Nema sta pomjeriti");

        if (novaLokacijaFigura != null) {
            if (naLokaciji(newPosition).getClass() == King.class)
                throw new IllegalChessMoveException("Kralj se ne moze pojest");
            if (novaLokacijaFigura.getColor() != staraLokacijaFigura.getColor()) jede = 1;
            else jede = 0;
        }

        //if(!isImport)
        if (staraLokacijaFigura.getClass() != Knight.class) {

            if (!praznaPutanja(oldPosition, newPosition))
                throw new IllegalChessMoveException("Nije moguce napraviti taj potez");
        }
        //endregion


        //region Castle

        if (staraLokacijaFigura.getMoves() == 0 && staraLokacijaFigura.getClass() == King.class && isCheck(staraLokacijaFigura.getColor()) == false) {//svi sem kinga vracaju uvijek -1, samo king moze 0 ako nije pomjeren


            int pom = 1;
            if (staraLokacijaFigura.getColor() == ChessPiece.Color.BLACK) pom = 8;

            ChessPiece r1 = naLokaciji("a" + pom);
            ChessPiece r2 = naLokaciji("h" + pom);

            if (newPosition.equals("g" + pom) && r2 != null && r2.getMoves() == 0) {//ako ide king side


                if (praznaPutanja(oldPosition, r2.getPosition())) {//ako su prazni do topa

                    staraLokacijaFigura.postaviNa("f" + pom);
                    if (isCheck(staraLokacijaFigura.getColor())) {
                        staraLokacijaFigura.postaviNa(oldPosition);
                        throw new IllegalChessMoveException("Ne moze kroz sah");
                    }
                    staraLokacijaFigura.postaviNa("g" + pom);
                    if (isCheck(staraLokacijaFigura.getColor())) {
                        staraLokacijaFigura.postaviNa(oldPosition);
                        throw new IllegalChessMoveException("Ne moze kroz sah");
                    }


                    if (isCheck(staraLokacijaFigura.getColor())) {
                        staraLokacijaFigura.postaviNa(oldPosition);
                        throw new IllegalChessMoveException("Error potez");
                    }
                    staraLokacijaFigura.postaviNa(oldPosition);
                    castle1 = "g" + pom;
                    return;

                }

            } else if (newPosition.equals("c" + pom) && r1 != null && r1.getMoves() == 0) {//ako ide queen side


                if (praznaPutanja(oldPosition, r1.getPosition())) {//ako su prazni do topa

                    staraLokacijaFigura.postaviNa("d" + pom);
                    if (isCheck(staraLokacijaFigura.getColor())) {
                        staraLokacijaFigura.postaviNa(oldPosition);
                        throw new IllegalChessMoveException("Ne moze kroz sah");
                    }
                    staraLokacijaFigura.postaviNa("c" + pom);
                    if (isCheck(staraLokacijaFigura.getColor())) {
                        staraLokacijaFigura.postaviNa(oldPosition);
                        throw new IllegalChessMoveException("Ne moze kroz sah");
                    }

                    if (isCheck(staraLokacijaFigura.getColor())) {
                        staraLokacijaFigura.postaviNa(oldPosition);
                        throw new IllegalChessMoveException("Error potez");
                    }
                    staraLokacijaFigura.postaviNa(oldPosition);
                    castle1 = "c" + pom;
                    return;

                }


            }

        }

        //endregion


        //region EnPassant

        if (staraLokacijaFigura.getClass() == Pawn.class) {

            if (staraLokacijaFigura.getColor() == ChessPiece.Color.WHITE && oldPosition.charAt(1) == '5') {
                String passant = "" + newPosition.charAt(0);
                char pom = (char) (newPosition.charAt(1) - 1);
                passant += pom;

                ChessPiece c = naLokaciji(passant);

                if (c == lastMoved && c != staraLokacijaFigura) {
                    jede = 1;
                    try {
                        staraLokacijaFigura.move(newPosition);
                        if (isCheck(staraLokacijaFigura.getColor())) {
                            staraLokacijaFigura.postaviNa(oldPosition);
                            throw new IllegalChessMoveException("Stvara sah");
                        }
                    } catch (Exception e) {
                        jede = 0;
                        throw new IllegalChessMoveException(e.toString());
                    }

                    jede = 0;
                    passant1 = newPosition;
                    return;
                }

            } else if (staraLokacijaFigura.getColor() == ChessPiece.Color.BLACK && oldPosition.charAt(1) == '4') {
                String passant = "" + newPosition.charAt(0);
                char pom = (char) (newPosition.charAt(1) + 1);
                passant += pom;

                ChessPiece c = naLokaciji(passant);
                if (c == lastMoved && c != staraLokacijaFigura) {
                    jede = 1;
                    try {
                        staraLokacijaFigura.move(newPosition);
                        if (isCheck(staraLokacijaFigura.getColor())) {
                            staraLokacijaFigura.postaviNa(oldPosition);
                            throw new IllegalChessMoveException("Stvara sah");
                        }
                    } catch (Exception e) {
                        jede = 0;
                        throw new IllegalChessMoveException("Passant error");
                    }

                    jede = 0;
                    passant1 = newPosition;
                    return;
                }


            }

        }


        //endregion



        staraLokacijaFigura.move(newPosition);
        if (novaLokacijaFigura != null) novaLokacijaFigura.postaviNa("X");
        int jedeTemp = jede;
        if (isCheck(boja)) {
            staraLokacijaFigura.postaviNa(oldPosition);
            if (novaLokacijaFigura != null) novaLokacijaFigura.postaviNa(newPosition);
            jede = 0;
            throw new IllegalChessMoveException("U sahu je ");
        }

        if (novaLokacijaFigura != null) novaLokacijaFigura.postaviNa(newPosition);

        jede = 0;
    }

    public void refresh() {


        if (!playing) return;

        //region ResetLabels
        for (int i = 0; i < 8; i++) {

            for (int j = 0; j < 8; j++) {
                UIboard[i][j].setGraphic(null);
                if ((i + j) % 2 == 0) {
                    UIboard[i][j].setStyle("-fx-background-color: DARKGOLDENROD;-fx-text-fill: gray;");
                } else UIboard[i][j].setStyle("-fx-background-color: LIGHTGOLDENRODYELLOW;-fx-text-fill: gray;");
            }

        }
        //endregion

        //region WhenPicking
        if (picking == true) {

            ImageView pomView = new Queen("D4", lastMoved.getColor()).getIcon();
            pomView.setFitHeight(30);
            pomView.setFitWidth(30);
            UIboard[3][3].setStyle("-fx-background-color: yellow;-fx-text-fill: gray;");
            UIboard[3][3].setGraphic(pomView);

            pomView = new Knight("E4", lastMoved.getColor()).getIcon();
            pomView.setFitHeight(30);
            pomView.setFitWidth(30);
            UIboard[3][4].setStyle("-fx-background-color: yellow;-fx-text-fill: gray;");
            UIboard[3][4].setGraphic(pomView);

            pomView = new Bishop("D5", lastMoved.getColor()).getIcon();
            pomView.setFitHeight(30);
            pomView.setFitWidth(30);
            UIboard[4][3].setStyle("-fx-background-color: yellow;-fx-text-fill: gray;");
            UIboard[4][3].setGraphic(pomView);

            pomView = new Rook("E5", lastMoved.getColor()).getIcon();
            pomView.setFitHeight(30);
            pomView.setFitWidth(30);
            UIboard[4][4].setStyle("-fx-background-color: yellow;-fx-text-fill: gray;");
            UIboard[4][4].setGraphic(pomView);


            return;
        }
        //endregion

        for (int i = 0; i < 8; i++) {

            for (int j = 0; j < 8; j++) {

                char pom = 'a';
                pom += i;
                String pozicija = "" + pom + (j + 1);

                ChessPiece naLok = naLokaciji(pozicija);

                UIboard[i][j].setGraphic(null);

                if (naLok != null) {
                    char boja = 'w';
                    if (naLok.getColor() == ChessPiece.Color.BLACK) boja = 'b';
                    UIboard[i][j].setGraphic(null);
                    ImageView pomView = naLok.getIcon();
                    pomView.setFitHeight(30);
                    pomView.setFitWidth(30);

                    UIboard[i][j].setGraphic(pomView);
                } else UIboard[i][j].setText("");

                if ((i + j) % 2 == 0) {
                    UIboard[i][j].setStyle("-fx-background-color: DARKGOLDENROD;-fx-text-fill: gray;");
                } else UIboard[i][j].setStyle("-fx-background-color: LIGHTGOLDENRODYELLOW;-fx-text-fill: gray;");


            }

        }


        //region CheckMate

        if (isImport || isSpectate) return;

        if (isCheckMate(ChessPiece.Color.WHITE)) {
            int status = 0;

            Alert a = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.YES, ButtonType.NO);
            if (isCheck(ChessPiece.Color.WHITE)) {
                if (currentPlayer == ChessPiece.Color.BLACK) {
                    a.setContentText(ConnectionDAO.getResourcebundle().getString("wonchekmatealert"));
                    status = 1;
                } else {
                    a.setContentText(ConnectionDAO.getResourcebundle().getString("lostcheckmate"));
                    status = -1;
                }
            } else {

                if ((currentPlayer == turnColor && currentPlayer != ChessPiece.Color.WHITE) ||
                        (currentPlayer != turnColor && currentPlayer == ChessPiece.Color.WHITE)) {
                    a.setContentText(ConnectionDAO.getResourcebundle().getString("whitestalematealert"));
                    status = 0;
                }
            }

            gameEnd(status, a);


        }

        if (isCheckMate(ChessPiece.Color.BLACK)) {
            int status = 0;

            Alert a = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.YES, ButtonType.NO);
            if (isCheck(ChessPiece.Color.BLACK)) {
                if (currentPlayer == ChessPiece.Color.WHITE) {
                    a.setContentText(ConnectionDAO.getResourcebundle().getString("wonchekmatealert"));
                    status = 1;
                } else {
                    a.setContentText(ConnectionDAO.getResourcebundle().getString("lostcheckmate"));
                    status = -1;
                }
            } else {
                if ((currentPlayer == turnColor && currentPlayer != ChessPiece.Color.BLACK) ||
                        (currentPlayer != turnColor && currentPlayer == ChessPiece.Color.BLACK)) {
                    a.setContentText(ConnectionDAO.getResourcebundle().getString("blackstalematealert"));
                    status = 0;
                }
            }

            gameEnd(status, a);

        }


        //endregion

    }

    public void check(String position) {

        ChessPiece naLok = naLokaciji(position);
        ChessPiece.Color boja = ChessPiece.Color.WHITE;

        if (naLok.getColor() == ChessPiece.Color.WHITE) boja = ChessPiece.Color.BLACK;

        for (char c = 'a'; c <= 'h'; c++) {

            for (int j = 1; j <= 8; j++) {
                String newPosition = "" + c + j;

                try {
                    jede = 0;
                    testMove(position, newPosition);
                    if (isCheck(boja)) {
                        UIboard[c - 'a'][j - 1].setStyle("-fx-background-color: red;-fx-text-fill: gray;");
                        naLok.postaviNa(position);
                        continue;
                    }

                    naLok.postaviNa(position);
                    ChessPiece cp = naLokaciji(newPosition);


                    if (cp != null) UIboard[c - 'a'][j - 1].setStyle("-fx-background-color: pink;-fx-text-fill: gray;");

                    else if ((j + c) % 2 == 0)
                        UIboard[c - 'a'][j - 1].setStyle("-fx-background-color: royalblue;-fx-text-fill: gray;");
                    else UIboard[c - 'a'][j - 1].setStyle("-fx-background-color: lightblue;-fx-text-fill: gray;");


                } catch (Exception e) {

                }


            }

        }
        jede = 0;
    }

    private void gameEnd(int status, Alert al) {

        if (!playing || isSpectate || isImport) return;


        playing = false;
        boolean save = false;

        Optional<ButtonType> resultAl = al.showAndWait();
        if (resultAl.get() == ButtonType.YES) save = true;

        Connection conn = ConnectionDAO.getConn();

        if (save) {

            try {
                PreparedStatement upit = conn.prepareStatement("Select * from pastgames where roomid=? limit 1");
                upit.setInt(1, roomId);
                ResultSet rs = upit.executeQuery();
                if (!rs.next()) {

                    upit = conn.prepareStatement("Insert into pastgames values(?,?,?,?,?,?,?)");

                    upit.setInt(1, ConnectionDAO.maxPastGamesId() + 1);

                    upit.setInt(2, whiteID);
                    upit.setInt(3, blackID);

                    if ((status == 1 && currentPlayer == ChessPiece.Color.WHITE) || (status == -1 && currentPlayer == ChessPiece.Color.BLACK)) {
                        upit.setInt(4, whiteID);
                    } else if ((status == -1 && currentPlayer == ChessPiece.Color.WHITE) || (status == 1 && currentPlayer == ChessPiece.Color.BLACK)) {
                        upit.setInt(4, blackID);
                    } else upit.setInt(4, 0);

                    upit.setString(5, moves);
                    upit.setString(6, String.valueOf(java.time.LocalDate.now()));
                    upit.setInt(7, roomId);

                    upit.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }


        try {

            PreparedStatement upit;
            int playerID = 0;
            if (currentPlayer == ChessPiece.Color.WHITE) playerID = whiteID;
            else playerID = blackID;

            if (status == 1) {
                if (playerID == whiteID) giveWin(whiteID, blackID);
                else giveWin(blackID, whiteID);
            } else if (status == -1) giveLoss(playerID);
            else giveDraw(playerID);

            if (status == 1) {
                upit = conn.prepareStatement("Update pastgames set winner=? where roomid=?");
                upit.setInt(1, playerID);
                upit.setInt(2, roomId);
                upit.executeUpdate();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
        a.setContentText("Do you want to rematch?");

        Optional<ButtonType> result = a.showAndWait();
        if (result.get() == ButtonType.YES) {
            controller.rematch();
        } else controller.leaveRoom();

    }

    private void giveWin(int winnerID, int loserID) {

        try {
            PreparedStatement winStatment = ConnectionDAO.getConn().prepareStatement("Update player set wins=wins+1 where id=?");
            winStatment.setInt(1, winnerID);
            winStatment.executeUpdate();

            PreparedStatement getLoserRating = ConnectionDAO.getConn().prepareStatement("Select rating from player where id=" + loserID);
            ResultSet rs = getLoserRating.executeQuery();
            if (!rs.next()) return;
            int lRating = rs.getInt(1);

            PreparedStatement ratingStatment = ConnectionDAO.getConn().prepareStatement("Update player" +
                    " set rating=rating+0.03*" + lRating + " where id=? ");
            ratingStatment.setInt(1, winnerID);
            ratingStatment.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void giveLoss(int loserID) {

        try {
            PreparedStatement lossStatment = ConnectionDAO.getConn().prepareStatement("Update player set loss=loss+1 where id=?");
            lossStatment.setInt(1, loserID);
            lossStatment.executeUpdate();

            PreparedStatement ratingStatment = ConnectionDAO.getConn().prepareStatement("Update player set rating=rating*0.97 where id=? ");
            ratingStatment.setInt(1, loserID);
            ratingStatment.executeUpdate();


        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private void giveDraw(int playerID) {
        try {
            PreparedStatement upit = ConnectionDAO.getConn().prepareStatement("Update player set draw=draw+1 where id=?");
            upit.setInt(1, playerID);
            upit.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void importGame(String importedMoves) {

        isImport = true;

        currentBoard = 0;

        importMoves = new ArrayList<String>(Arrays.asList(importedMoves.split(",")));
        allBoard = new ChessPiece[importMoves.size() + 1][2][16];

        for (String s : importMoves) {
            addCurrentBoard();
            String oldPosition = s.substring(s.indexOf("(") + 1, s.indexOf("-"));
            String newPosition = s.substring(s.indexOf("-") + 1, s.indexOf(")"));

            move(oldPosition, newPosition);
        }
        addCurrentBoard();


        currentBoard = 0;
        board = allBoard[currentBoard];
        refresh();

    }

    public void spectateGame() {

        isSpectate = true;

        String importedMoves = "";

        PreparedStatement getMoves = null;
        try {
            getMoves = ConnectionDAO.getConn().prepareStatement("Select moves from room where id=?");
            getMoves.setInt(1, roomId);
            ResultSet rs = getMoves.executeQuery();
            rs.next();
            importedMoves = rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (importedMoves.isEmpty()) return;


        importMoves = new ArrayList<String>(Arrays.asList(importedMoves.split(",")));
        for (String s : importMoves) {
            String oldPosition = s.substring(s.indexOf("(") + 1, s.indexOf("-"));
            String newPosition = s.substring(s.indexOf("-") + 1, s.indexOf(")"));
            move(oldPosition, newPosition);
            changePlayer();
        }
        moves = importedMoves;
        refresh();

    }

    public void next() {

        if (currentBoard == allBoard.length - 1) return;
        currentBoard++;
        board = allBoard[currentBoard];
        refresh();
    }

    public void previous() {
        if (currentBoard == 0) return;

        currentBoard--;
        board = allBoard[currentBoard];
        refresh();
    }

    public void first() {

        currentBoard = 0;
        board = allBoard[currentBoard];
        refresh();
    }

    public void last() {

        currentBoard = allBoard.length - 1;

        board = allBoard[currentBoard];
        refresh();
    }


}