import java.io.*;

/*
    Main Class
    input : directory of jack files
    output : xml files of all files representing parse trees
*/
public class JackAnalyzer {
    private static String getOutputPath(String inputPath){
        int e = inputPath.indexOf(".jack")+1, s = 0;
        return inputPath.substring(0, e).concat(".xml");
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            System.err.println("Expected Input file name or directory");
        }
        String inputPath = args[0];
        File file = new File(inputPath);
        if (file.isFile()){
//            InputStreamReader reader = new InputStreamReader(new FileInputStream(inputPath));
            FileInputStream fileInputStream = new FileInputStream(inputPath);
            PrintWriter writer = new PrintWriter(getOutputPath(inputPath));
            new CompilationEngine(fileInputStream, writer).compileOnlyTokens();
        }else{
            System.out.println("Second phase of implementation .");
        }
    }
}
