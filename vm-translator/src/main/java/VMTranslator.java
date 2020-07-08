import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class VMTranslator extends TranslatorUtils{

    public static void main(String[] args) throws Exception {
        if(args.length == 0){
            System.err.println("No input files provided.");
            return;
        }
        String inputPath = args[0];
        File file = new File(inputPath);
        init(inputPath);
        if(file.isFile()) {
            generateFileCode(inputPath);
        }
        else{
           Stream<Path> walk =  Files.walk(Paths.get(inputPath));
           List<String> files = walk.map(Path::toString).filter(x->x.endsWith("vm")).collect(Collectors.toList());
           String boostStrapCodePath = inputPath.concat(File.separator).concat("Sys.vm");
           if(files.contains(boostStrapCodePath))  generateFileCode(boostStrapCodePath); //bootstrap code
           for(String filePath : files){
               if(filePath.endsWith("Sys.vm")) continue;
               generateFileCode(filePath);
           }
        }
        commit();
    }
}
