import java.io.*;

/*
    Handles Jack Grammar
 */
class CompilationEngine {
    private JackTokenizer jackTokenizer;
    private PrintWriter writer;
    CompilationEngine(FileInputStream inputStream, Writer writer){
        this.writer = (PrintWriter) writer;
        this.jackTokenizer = new JackTokenizer(inputStream);

    }

    // complete compilation process
    void compile(){

    }

    // creates a file of the output of Tokenization
    void compileOnlyTokens() throws IOException {
        System.out.println("<tokens>");
        while(jackTokenizer.hasMoreTokens()){
            jackTokenizer.advance();
        }
        System.out.println("<\\tokens>");
    }

    String getCurrToken(){
        return "";
    }
}
