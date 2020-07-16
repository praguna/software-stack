import java.io.InputStreamReader;
import java.io.Writer;

/*
    Handles Jack Grammar
 */
public class CompilationEngine {
    private JackTokenizer jackTokenizer;
    private Writer writer;
    CompilationEngine(InputStreamReader inputStreamReader, Writer writer){
        this.writer = writer;
        this.jackTokenizer = new JackTokenizer(inputStreamReader);

    }

    // complete compilation process
    void compile(){

    }

    // creates a file of the output of Tokenization
    void compileOnlyTokens(){
        while(jackTokenizer.hasMoreTokens()){

            jackTokenizer.advance();
        }
    }
}
