package Chess;

public class IllegalChessMoveException extends IllegalArgumentException {
    String message="";

    public IllegalChessMoveException(String message){
        this.message=message;
    }

    public String toString(){
        return this.message;
    }

}