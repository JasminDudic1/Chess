package sample;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import sample.ChessPiece;
import sample.IllegalChessMoveException;

public class Knight extends ChessPiece {

    String pozicija;
    Color boja;
    char znak='N';

    Image iconImgW=new Image("Icons/WhiteKnight.png");
    Image iconImgB=new Image("Icons/BlackKnight.png");

    Knight(String pozicija,Color boja){
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
        char slovo=position.charAt(0);
        slovo=Character.toLowerCase(slovo);
        int broj=position.charAt(1)-48;
        if(slovo<'a' || slovo>'h' || broj<1 || broj>8 || pozicija.length()>2) throw new IllegalArgumentException("Potez nije ispravan");

        int pomakI=Math.abs(this.pozicija.charAt(0)-slovo);
        int pomakJ=Math.abs(this.pozicija.charAt(1)-broj-48);
        if(pomakI*pomakJ!=2) throw new IllegalChessMoveException("Potez nije ispravan");//mora se pomjerit 1:2,2:1, -1:2, -2:1, -1:-2,-2:-1 tj i*j==2

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

    public ImageView getIcon(){

        if(boja==Color.WHITE)
            return new ImageView(iconImgW);

        return new ImageView(iconImgB);

    }
}