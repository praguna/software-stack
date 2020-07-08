import java.io.Reader;
import java.util.Scanner;
enum CommandType{
    A_COMMAND("A") , C_COMMAND("C"), L_COMMAND("L");
    private String type;
    private CommandType(String type){
        this.type = type;
    }
    public String getType(){
        return type;
    }
}

class Parser {
    private Scanner sc;
    private String curr;
    private CommandType commandType;
    private int romAddr = 0;
    Parser(Reader reader){
        this.sc = new Scanner(reader);
    }

    void setReader(Reader reader){
        this.sc.close();
        this.sc = new Scanner(reader);
    }

    boolean hasMoreCommands(){
        return sc.hasNextLine();
    }

    void advance(){
        curr = sc.nextLine();
        while(hasMoreCommands() && curr==null || curr.length()==0 || curr.startsWith("//")){
            curr = sc.nextLine();
        }
        removeCurrCommentAndSpace();
    }

    private void removeCurrCommentAndSpace(){
        curr=curr.replaceAll("\\s","");
        int l = curr.indexOf("//");
        if(l==-1) return;
        curr = curr.substring(0,l);
    }

    CommandType getCommandType(){
        //TODO: Implement using regular expression matching, covering all cases
        if(curr.startsWith("(")) {
            commandType = CommandType.L_COMMAND;
        }
        else if(curr.startsWith("@")) {
            commandType = CommandType.A_COMMAND;
            ++romAddr;
        }
        else {
            commandType = CommandType.C_COMMAND;
            ++romAddr;
        }
        return commandType;
    }

    int getRomAddr(){
        return romAddr;
    }

    String getSymbol(){
        if(commandType == CommandType.L_COMMAND)
            return curr.substring(1,curr.length()-1).trim();
        if(commandType == CommandType.A_COMMAND)
            return curr.substring(1).trim();
        throw typeCError("Only Type A and L are allowed");
    }

    String getDest(){
        if(commandType != CommandType.C_COMMAND)
            throw typeCError("Only Type C is Allowed");
        int e = curr.indexOf("=");
        if(e == -1) return null;
        return curr.substring(0,e).trim();
    }

    String getComp(){
        if(commandType != CommandType.C_COMMAND)
            throw typeCError("Only Type C is Allowed");
        int s = curr.indexOf("=")+1, e;
        if((e=curr.indexOf(";"))==-1)
            e=curr.length();
        return curr.substring(s,e).trim();
    }

    String getJump(){
        if(commandType != CommandType.C_COMMAND)
            throw typeCError("Only Type C is Allowed");
        int s = curr.indexOf(";");
        if(s==-1) return null;
        return curr.substring(s+1).trim();
    }

    String getCurr(){
        return curr;
    }

    private IllegalArgumentException typeCError(String cmd){
        throw new IllegalArgumentException(cmd);
    }

}
