import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
    Main Class
    input : directory of jack files
    output : xml files of all files representing parse trees
*/
public class JackCompiler {
    private static String getOutputPath(String inputPath){
        int e = inputPath.indexOf(".jack");
        return inputPath.substring(0, e).concat(".vm");
    }

    private static void generateFileCode(String inputPath, SymbolTable symbolTable) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(inputPath);
        String of = getOutputPath(inputPath);
        System.out.println("Compiling Jack file@::"+inputPath);
        PrintWriter writer = new PrintWriter(of);
        CompilationEngine compilationEngine = new CompilationEngine(symbolTable,fileInputStream, writer);
        compilationEngine.compileSyntaxAnalyzer();
        compilationEngine.close(of);
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            System.err.println("Expected Input file name or directory");
            return;
        }
        String inputPath = args[0];
        File file = new File(inputPath);
        SymbolTable symbolTable = new SymbolTable();
        if (file.isFile()){
            generateFileCode(inputPath, symbolTable);
        }else if(file.isDirectory()){
            Stream<Path> walk =  Files.walk(Paths.get(inputPath));
            List<String> files = walk.map(Path::toString).filter(x->x.endsWith("jack")).collect(Collectors.toList());
            for(String filePath : files){
                JackCompiler.generateFileCode(filePath, symbolTable);
            }
         }
        }
    }
