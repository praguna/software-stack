import java.io.*;
import java.lang.reflect.InvocationTargetException;

class TranslatorUtils {

    private static Parser parser;
    private static CodeWriter codeWriter;
    private static String outputPath;
    private static int lines = 1;


    private static String getOutputPath(String inputPath){
        StringBuilder sb = new StringBuilder();
        int s = inputPath.lastIndexOf('.');
        if(s==-1){
            s = inputPath.lastIndexOf(File.separator);
            String dirName = inputPath.substring(s+1);
            return inputPath.concat(File.separator+dirName).concat(".asm");
        }
        return sb.append(inputPath,0,s+1).append("asm").toString();
    }

    static void commit(){
        codeWriter.writComment("The End");
        codeWriter.close();
        parser.close();
        System.out.println("Done, Wrote to : "+outputPath);
    }

    static void init(String inputPath) throws IOException{
        outputPath = getOutputPath(inputPath);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)));
        codeWriter = new CodeWriter(writer);
        if(inputPath.contains("FibonacciElement") || inputPath.contains("StaticsTest")){
            codeWriter.writeInit();
        }
    }

    static void generateFileCode(String inputFile) throws FileNotFoundException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(inputFile));
        parser = new Parser(reader);
        while(parser.hasMoreCommands()){
          try{
            parser.advance();
            codeWriter.writComment(parser.getCurr());
            CommandType commandType = parser.getCurrCommandType();
            if(!parser.isValidForArg1()) {
                  evaluate(commandType);
            }
            else {
                String arg1 = parser.arg1();
                if (parser.isValidForArg2()) {
                    int arg2 = parser.arg2();
                    evaluate(arg1,arg2,commandType);
                }
                else{
                    evaluate(arg1,commandType);
                }
            }
         }
          catch (Exception e){
              System.out.println("Error at line : "+lines);
              System.out.println("At :"+parser.getCurr());
              e.printStackTrace();
              System.exit(0);
          }
          ++lines;
        }
        codeWriter.writComment("EOF");
    }

    private static void evaluate(CommandType commandType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        switch (commandType){
            case Return: codeWriter.writeReturn(); break;
            case Arithmetic: codeWriter.writeArithmetic(parser.getCommand()); break;
            default:
                throw new IllegalStateException("Unexpected value: " + commandType);
        }
    }


    private static void evaluate(String arg1, CommandType commandType){
        switch (commandType){
            case IfGoto: codeWriter.writeIfGoto(arg1);break;
            case Goto: codeWriter.writeGoto(arg1); break;
            case Label:codeWriter.writeLabel(arg1);break;
            default:
                throw new IllegalStateException("Unexpected value: " + commandType);
        }
    }

    private static void evaluate(String arg1, int arg2, CommandType commandType) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        switch (commandType){
            case Push:
            case Pop:
                codeWriter.writePushPop(commandType,arg1,arg2); break;
            case Function: codeWriter.writeFunction(arg1,arg2); break;
            case Call: codeWriter.writeCall(arg1,arg2); break;
            default:
                throw new IllegalStateException("Unexpected value: " + commandType);
        }
    }

}
