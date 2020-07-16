import java.io.InputStreamReader;
import java.util.Scanner;

/*
    Handles tokens of the source code
 */

public class JackTokenizer {
    private Scanner scanner;
    private String token;
    JackTokenizer(InputStreamReader reader){
        scanner = new Scanner(reader);
    }

    // Any remaining tokens left
    boolean hasMoreTokens(){
        return scanner.hasNext();
    }

    // Advance logic to increment stream reader
    void advance(){

    }

    // Return current token type
    Token getTokenType(){
        return null;
    }

    // Return the keyword of current token is keyword
    JackKeyword getKeyword(){
        return null;
    }

    // Return the identifier of current token
    String getIdentifier(){
        return null;
    }

    // Return IntVal of the current token
    int getIntVal(){
        return 0;
    }

    // Return StringVal of the current token
    String getStringVal(){
        return null;
    }
}
