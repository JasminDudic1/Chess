package sample;

import java.util.Scanner;


public class Program {

    public static void main(String[] args) {
        Board tabla=new Board();
        ChessPiece.Color bojaIgraca=ChessPiece.Color.WHITE;
        boolean cool=false;


       while(1==1){

            if(bojaIgraca== ChessPiece.Color.WHITE)
                System.out.print("White move:");
            else System.out.print("Black move:");

            Scanner scanner = new Scanner(System. in);
            String potez = scanner. nextLine();
            if(potez.toLowerCase().equals("cool")){
                System.out.println("Cool activated");
                cool=true;
                continue;
            }

            try {
                if(potez.length()==2)
                    tabla.move(Pawn.class, bojaIgraca, potez);
                else switch (potez.charAt(0)) {
                    case 'N':
                        tabla.move(Knight.class, bojaIgraca, potez.substring(1));
                        break;
                    case 'B':
                        tabla.move(Bishop.class, bojaIgraca, potez.substring(1));
                        break;
                    case 'R':
                        tabla.move(Rook.class, bojaIgraca, potez.substring(1));
                        break;
                    case 'Q':
                        tabla.move(Queen.class, bojaIgraca, potez.substring(1));
                        break;
                    case 'K':
                        tabla.move(King.class, bojaIgraca, potez.substring(1));
                        break;
                    default:
                        System.out.println("Potez nije ispravno upisan");
                        continue;

                }
            }catch(Exception e){
                System.out.println(e.toString());
                continue;
            }
            if(bojaIgraca== ChessPiece.Color.WHITE)
                bojaIgraca= ChessPiece.Color.BLACK;
            else bojaIgraca= ChessPiece.Color.WHITE;

            if(cool==true)tabla.ispisi();
            if(tabla.isCheck(bojaIgraca)==true) System.out.println("Check");

        }





    }

}