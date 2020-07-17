import java.io.*;

/*
    Handles tokens of the source code
 */

class JackTokenizer {
    private BufferedInputStream inputStream;
    private int ptr;
    private String currTokenValue;
    private Token currTokenType;
    JackTokenizer(InputStream inputStream){
        this.inputStream = new BufferedInputStream(inputStream);
    }

    // Any remaining tokens left
    boolean hasMoreTokens() {
        return ptr != -1;
    }

    // Advance logic to increment stream reader and also to initialize tokens
    void advance() throws IOException {
        // remove preceding spaces and comment
        skipWhiteSpace();
        if(isComment()){
            removeComment();
        }
        if(isSymbol()){

        }

        System.out.print(getCurrChar());
    }

    private boolean isComment() throws IOException {
        return startsWith("//") || startsWith("/*");
    }

    private boolean isSymbol() throws IOException {
//        if(startsWith("{") || startsWith("}") || startsWith(";") || startsWith("."))
        return false;
    }



    // Return current token type
    Token getTokenType(){
        return  currTokenType;
    }

    private char jumpChar(int i) throws IOException {
        char c = (char)ptr;
        while(i-- > 0){
            c = getNextChar();
        }
        return c;
    }

    private char getCurrChar(){
        return (char)ptr;
    }

    private char getNextChar() throws IOException {
        if(hasMoreTokens()) ptr = inputStream.read();
        return (char)ptr;
    }

    private void skipWhiteSpace() throws IOException {
        while(hasMoreTokens() && Character.isWhitespace(getNextChar()));
        removeComment();
    }

    private void removeComment() throws IOException {
            if(startsWith("//")){
                jumpChar(3);
                while(hasMoreTokens() && (getNextChar()!= '\n'));
                getNextChar();
            }
            if(startsWith("/*")){
                jumpChar(3);
                while (hasMoreTokens()){
                    if(startsWith("*/")){
                        jumpChar(3);
                        break;
                    }
                    getNextChar();
                }
            }
    }

    private boolean startsWith(String s) throws IOException {
        inputStream.mark(s.length()+1);
        int f = ptr;
        for(int i=0; i < s.length(); i++){
            if(f==-1 || (char)f != s.charAt(i)) return false;
            f = inputStream.read();
        }
        inputStream.reset();
        return true;
    }

    // Return the keyword if current token is keyword
    JackKeyword getKeyword(){
        return null;
    }

    // Return the identifier for current token
    String getIdentifier(){
        return currTokenValue;
    }

    // Return IntVal for the current token
    int getIntVal(){
        return Integer.parseInt(currTokenValue);
    }

    // Return StringVal of the current token
    String getStringVal(){
        return null;
    }

    private void setCurrToken(String val, Token type){
        currTokenType = type;
        currTokenValue = val;
    }
}
