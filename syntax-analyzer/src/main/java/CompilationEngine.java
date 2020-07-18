import java.io.*;
import java.util.List;

/*
    Handles Jack Grammar
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

    // complete compilation process
    void compile(){

    }

    // creates a file of the output of Tokenization
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

    void close(String of) throws IOException {
        System.out.println(String.format("Done writing to Output File @ : %s  :)",of));
        writer.close();
        jackTokenizer.close();
        inputStream.close();
    }
}
