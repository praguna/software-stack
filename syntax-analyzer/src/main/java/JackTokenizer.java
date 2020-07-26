import java.io.*;

/*
    Handles tokens of the source code
    Implementation : String matching from inputStream
 */

class JackTokenizer {
    private BufferedInputStream inputStream;
    private int ptr; // code point in the stream
    // These two variables are null, null if token is absent or skipped in advance, otherwise contain token value and token type
    private String currTokenValue;
    private Token currTokenType;
    // initialize the stream
    JackTokenizer(InputStream inputStream){
        this.inputStream = new BufferedInputStream(inputStream);
    }

    // Any remaining tokens left
    boolean hasMoreTokens() {
        return ptr != -1;
    }

    // Advance logic to increment stream reader and also to initialize tokens
    void advance() throws Exception {
        int x;
        String v;
        setCurrToken(null,null);
        if(ptr==0){
            getNextChar();
        }
        // remove preceding spaces and comment
        skipWhiteSpace();
        if(isComment()){
            removeComment();
            return;
        }
        else if(isSymbol()){
            currTokenValue = String.valueOf(getCurrChar());
            currTokenType = Token.SYMBOL;
            getNextChar();
            return;
        }
        else if((x=isIntegerConst())!=-1){
            currTokenType = Token.INT_CONST;
            currTokenValue = String.valueOf(x);
            jumpChar(String.valueOf(x).length());
            return;
        }
        else if(isStringConst()){
            currTokenType = Token.STRING_CONST;
            currTokenValue = collectStringConst();
            return;
        }
        else if((v = isKeyword()).length() > 0){
              currTokenType = Token.KEYWORD;
              currTokenValue = v;
              jumpChar(v.length());
            return;
        }
        else if(isIdentifier()){
             currTokenType = Token.IDENTIFIER;
             currTokenValue = collectIdentifier();
            return;
        }
        if(ptr!=-1) {
            throw new Exception(String.format("Symbol %s not found", peek(1)));
        }
    }

    // returns the value of identifier
    private String collectIdentifier() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (hasMoreTokens() && (Character.isLetter(ptr) || Character.isDigit(ptr) || getCurrChar()=='_')){
            sb.append(getCurrChar());
            getNextChar();
        }
        return sb.toString();
    }

    // check if it is an identifier
    private boolean isIdentifier() {
        return Character.isLetter(ptr) ||  getCurrChar()=='_';
    }

    // check if it is a keyword
    private String isKeyword() throws IOException {
        inputStream.mark(11);
        int f = ptr, i = 0;
        StringBuilder sb = new StringBuilder();
        if(Character.isDigit(ptr)) return sb.toString();
        while((f!=-1) && i < 11){
            sb.append((char)f);
            if(JackKeyword.matches(sb.toString())) {
                inputStream.reset();
                return sb.toString();
            }
            f = inputStream.read();
            ++i;
        }
        inputStream.reset();
        return "";
    }

    // returns the value of String const
    private String collectStringConst() throws IOException {
        getNextChar();
        StringBuilder sb = new StringBuilder();
        while(hasMoreTokens()){
            if((char)ptr == '\"') break;
            sb.append(getCurrChar());
            getNextChar();
        }
        jumpChar(1);
        return sb.toString();
    }

    // check if it is a comment
    private boolean isComment() throws IOException {
        return startsWith("//") || startsWith("/*");
    }

    // check if it is a symbol
    private boolean isSymbol() throws IOException {
        return isBraces() || isOperator() || startsWith(";") || startsWith(".") || startsWith(",") || startsWith("~");
    }

    // check if it is an integer constant and also return the integer value
    private int isIntegerConst()throws IOException{
        inputStream.mark(6);
        int f = ptr;
        StringBuilder sb = new StringBuilder();
        while((f!=-1) && (Character.isDigit((char)f))){
            sb.append((char)f);
            f = inputStream.read();
        }
        inputStream.reset();
        if(sb.length() > 0) return Integer.parseInt(sb.toString());
        return -1;
    }

    // check if it is a string const
    private boolean isStringConst() throws IOException{
        return startsWith("\"");
    }

    // check if it is braces
    private boolean isBraces() throws IOException{
        return startsWith("{") || startsWith("}") ||  startsWith("(") || startsWith(")") || startsWith("[") || startsWith("]");
    }

    // check if it is an operator
    private boolean isOperator() throws IOException{
        return startsWith("|") || startsWith("&") || startsWith("/") || startsWith("*") || startsWith("+") || startsWith("-") || startsWith("<")
                || startsWith(">") || startsWith("=");
    }


    // Return current token type
    Token getTokenType(){
        return  currTokenType;
    }

    // move to i characters forward in the stream and return it, starting from next character
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

    // return the next character of te stream
    private char getNextChar() throws IOException {
        if(hasMoreTokens()) ptr = inputStream.read();
        return (char)ptr;
    }

    // ignore whitespace
    private void skipWhiteSpace() throws IOException {
        while(hasMoreTokens() && Character.isWhitespace(getCurrChar())){
            getNextChar();
        }
    }

    // ignore comment of two types
    private void removeComment() throws IOException {
            if(startsWith("//")){
                jumpChar(2);
                while(hasMoreTokens() && (getCurrChar()!= '\n')){
                    getNextChar();
                }
                getNextChar();
            }
            if(startsWith("/*")){
                jumpChar(2);
                while (hasMoreTokens()){
                    if(startsWith("*/")){
                        jumpChar(2);
                        break;
                    }
                    getNextChar();
                }
            }
    }

    // does the next stream of characters start with s ?
    private boolean startsWith(String s) throws IOException {
        return peek(s.length()).startsWith(s);
    }

    // returns a string of next (x-1) characters and the present
    private String peek(int x) throws IOException{
        StringBuilder sb = new StringBuilder();
        inputStream.mark(x);
        int f = ptr;
        while(x-- > 0 && !(f==-1)){
            sb.append((char)f);
            f = inputStream.read();
        }
        inputStream.reset();
        return sb.toString();
    }

    // Return the keyword if current token is keyword
    JackKeyword getKeyword(){
        return JackKeyword.valueOf(currTokenValue.toUpperCase());
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
        return currTokenValue;
    }

    // Return Symbol value of current token
    char getSymbol(){
        return getCurrChar();
    }

    // set token value and token type
    private void setCurrToken(String val, Token type){
        currTokenType = type;
        currTokenValue = val;
    }

    // close the read stream
    void close() throws IOException {
        inputStream.close();
    }

}
