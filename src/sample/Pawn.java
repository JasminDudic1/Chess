package sample;

import javafx.scene.image.Image;
import sample.Board;
import sample.ChessPiece;
import sample.IllegalChessMoveException;

import javafx.scene.image.ImageView;


public class Pawn extends ChessPiece {

    String pozicija;
    Color boja;
    int mogucPomak=2;
    char znak='P';
    Image iconImgW=new Image("Icons/WhitePawn.png");
    Image iconImgB=new Image("Icons/BlackPawn.png");
    int moves=0;

    Pawn(String pozicija,Color boja)
    {
        if(pozicija.length()!=2)throw new IllegalArgumentException("Van ploce");
        char slovo=pozicija.charAt(0);
        slovo=Character.toLowerCase(slovo);
        int broj=pozicija.charAt(1)-48;
        if(slovo<'a' || slovo>'h' || broj<1 || broj>8 || pozicija.length()>2) throw new IllegalArgumentException("Van ploce");
        this.pozicija=pozicija.toLowerCase();
        this.boja=boja;
    }


    @Override
    public String getPosition() {
        return pozicija;
    }

    @Override
    public Color getColor() {
        return boja;
    }

    @Override
    public void move(String position) {

        if(position.length()!=2)throw new IllegalArgumentException("Van ploce");
        int smjer=1;
        if(this.boja==Color.BLACK)smjer=-1;

        char slovo=position.charAt(0);
        slovo=Character.toLowerCase(slovo);
        int broj=position.charAt(1)-48;

        if((boja==Color.WHITE && pozicija.charAt(1)=='2') ||(boja==Color.BLACK && pozicija.charAt(1)=='7'))mogucPomak=2;
        else mogucPomak=1;


        if(slovo<'a' || slovo>'h' || broj<1 || broj>8 || pozicija.length()>2) throw new IllegalArgumentException("Van ploce");

        int pomakI=Math.abs(this.pozicija.charAt(0)-slovo);
        int pomakJ=(broj-this.pozicija.charAt(1)+48)*smjer;


        if((Board.jede==1 && (pomakI!=1 || pomakJ!=1)) ||(Board.jede==0 && (pomakI!=0 || pomakJ>this.mogucPomak) ) || pomakJ<0) {
            throw new IllegalChessMoveException(pomakI+"|"+pomakJ);//ako je vise od moguceg (prvi put 2) il ako je vise od 1 mjesto u stranu
        }


        this.pozicija=position;

    }

    @Override
    public char getZnak() {
        return znak;
    }

    @Override
    void postaviNa(String pozicija) {
        this.pozicija=pozicija.toLowerCase();
    }

    @Override
    int getMoves() {
        return moves;
    }

    @Override
    void moved() {
    moves++;
    }

    public ImageView getIcon(){

    if(boja==Color.WHITE)
        return new ImageView(iconImgW);

        return new ImageView(iconImgB);

    }
}