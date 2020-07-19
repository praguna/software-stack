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
    void compileClassCarDec(){

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
