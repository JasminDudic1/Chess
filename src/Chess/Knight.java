package Chess;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Knight extends ChessPiece {

    String pozicija;
    Color boja;
    char znak='N';

    static Image iconImgW=new Image("icons/WhiteKnight.png", 100, 150, false, false);
    static Image iconImgB=new Image("icons/BlackKnight.png", 100, 150, false, false);

    Knight(String pozicija,Color boja){
        if(pozicija.length()!=2)throw new IllegalArgumentException("Van ploce");
        char slovo=pozicija.charAt(0);
        slovo=Character.toLowerCase(slovo);
        int broj=pozicija.charAt(1)-48;
        if(slovo<'a' || slovo>'h' || broj<1 || broj>8 || pozicija.length()>2) throw new IllegalArgumentException("Van ploce");
        this.pozicija=pozicija.toLowerCase();
        this.boja=boja;
    }

    public Knight (ChessPiece b){
        this.pozicija=b.getPosition();
        this.boja=b.getColor();
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

    @Override
    int getMoves() {
        return -1;
    }

    @Override
    void moved() {

    }

    public ImageView getIcon(){

        if(boja==Color.WHITE)
            return new ImageView(iconImgW);

        return new ImageView(iconImgB);

    }
}