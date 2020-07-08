import java.io.*;

public class Assembler{

    private static InputStreamReader getInputStreamReader(String inputPath) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(inputPath);
        return new InputStreamReader(fileInputStream);
    }

    private static String outputPath(String path){
        int s =  path.lastIndexOf('\\') + 1;
        int l = path.length() - 3;
        String output_path = path.substring(0,l+1);
        return output_path + path.substring(s, l) + "hack";
    }

    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.out.println("Sorry! File name not provided");
            return;
        }
        String inputPath = args[0];
        Parser parser = new Parser(getInputStreamReader(inputPath));
        Code code = new Code();
        SymbolTable symbolTable = new SymbolTable();

        while (parser.hasMoreCommands()){
            parser.advance();
            CommandType type = parser.getCommandType();
            if(type==CommandType.L_COMMAND)
                symbolTable.addEntry(parser.getSymbol(),parser.getRomAddr());
        }

        String outputPath = outputPath(inputPath);
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)));
        parser.setReader(getInputStreamReader(inputPath));

        while(parser.hasMoreCommands()){
            parser.advance();
            CommandType type=parser.getCommandType();
            if(type == CommandType.A_COMMAND) {
                String decimal = parser.getSymbol();
                out.println(code.address(decimal,symbolTable));
            }
            else if(type == CommandType.C_COMMAND){
                String comp = parser.getComp();
                String dest= parser.getDest();
                String jmp = parser.getJump();
                out.println(code.compAssemble(dest,comp,jmp));
            }
        }
        out.close();
        System.out.println(String.format("Done!! Wrote to %s",outputPath));
    }
}
