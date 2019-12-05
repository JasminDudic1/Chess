package sample;

public abstract class ChessPiece {



    public static enum Color{WHITE,BLACK};

    // public ChessPiece(String pozicija,Color boja){}

    public abstract String getPosition();

    public abstract Color getColor();

    public abstract void move(String position);

    public abstract char getZnak();


    abstract void postaviNa(String pozicija);




}