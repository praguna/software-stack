import java.io.*;
import java.util.List;

/*
    Handles Jack Grammar
    Implementation : Top down Recursion, LL(2) Parser
 */
class CompilationEngine {
    private JackTokenizer jackTokenizer;
    private PrintWriter writer;
    private InputStream inputStream;
    CompilationEngine(FileInputStream inputStream, Writer writer){
        this.writer = (PrintWriter) writer;
        this.jackTokenizer = new JackTokenizer(inputStream);
        this.inputStream = inputStream;
    }

    // compile class NT
    void compileClass(){

    }

    // compile Declared Variables of class
    void compileClassCarDec() throws Exception {
        String tokenVal = getTokenVal();
        if(tokenVal.equals("static") || tokenVal.equals("field")){
            eat(tokenVal, Token.KEYWORD);
            eatType();
            eatIdentifier();
            tokenVal = getTokenVal();
            while(tokenVal.equals(",")) {
                eat(",", Token.SYMBOL);
                eatIdentifier();
                tokenVal = getTokenVal();
            }
            eat(";", getTokenType());
        }
    }

    // The below functions compile a method, function or constructor, parameter list, variable Declaration
    void compileSubroutine(){

    }


    void compileParameterList(){

    }

    void compileVarDec(){

    }

    // Below methods compile statements
    void compileDo(){

    }

    void compileLet(){

    }

    void compileWhile(){

    }

    void compileIf(){

    }

    void compileReturn(){

    }

    void compileExpression(){

    }

    // usage of LL(2) parsing
    void compileTerm(){

    }

    // complete syntax Analyzer process
    void compileSyntaxAnalyzer(){

    }

    private Token getTokenType() {
        return jackTokenizer.getTokenType();
    }

    private void eatType() throws Exception {
        String tokenVal = getTokenVal();
        Token tokenType = getTokenType();
        if(tokenType.equals(Token.KEYWORD) && !JackKeyword.isType(tokenVal)){
            throwException("DataType int, char or boolean",tokenVal,tokenVal);
        }
        if(tokenType.equals(Token.IDENTIFIER)) eat(tokenVal ,Token.IDENTIFIER);
        else eat(tokenVal,Token.KEYWORD);
    }

    private void eatIdentifier() throws Exception {
        String tokenVal = getTokenVal();
        eat(tokenVal ,Token.IDENTIFIER);
    }

    private void eat(String s, Token token) throws Exception {
        String tokenVal = getTokenVal();
        if(!s.equals(tokenVal)){
            throwException(s,tokenVal,tokenVal);
        }
        if(!getTokenType().equals(token)){
            throwException(token.getAlias(),getTokenType().getAlias(),tokenVal);
        }
        if(!jackTokenizer.hasMoreTokens()){
            throw new Exception(String.format("Reached end of file Too Soon @ :: %s",s));
        }
        jackTokenizer.advance();
    }

    private void throwException(String expected, String got, String at) throws Exception {
        throw new Exception(String.format("Mismatch expected %s got :: %s @ %s",expected,got,at));
    }


    private String getTokenVal() {
        return jackTokenizer.getStringVal();
    }

    // creates a file of the output of Tokenization, this is used in unit testing for correct token generation
    void compileOnlyTokens() throws Exception {
        writer.println("<tokens>");
        while(jackTokenizer.hasMoreTokens()){
            if(jackTokenizer.getStringVal()!=null && jackTokenizer.getTokenType()!=null){
                String entry = jackTokenizer.getStringVal();
                if(jackTokenizer.getTokenType().equals(Token.SYMBOL)){
                    entry = JackTokenizerUtils.getHtml(entry);
                }
                String val = JackTokenizerUtils.getOpenTag(jackTokenizer.getTokenType().getAlias()) + " "+entry+" "
                        + JackTokenizerUtils.getCloseTag(jackTokenizer.getTokenType().getAlias());
                writer.println(val);

            }
            jackTokenizer.advance();
        }
        writer.println("</tokens>");
    }

    // close all streams and print the end message
    void close(String of) throws IOException {
        System.out.println(String.format("Done writing to Output File @ : %s  :)",of));
        writer.close();
        jackTokenizer.close();
        inputStream.close();
    }
}
