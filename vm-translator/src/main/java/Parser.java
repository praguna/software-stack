import java.io.Reader;
import java.util.*;

enum CommandType{
    Arithmetic("add","sub","or","and","eq","neg","not","lt","gt"),
    Push("push"), Pop("pop"),
    Label("label"),IfGoto("if-goto"),Goto("goto"),
    Function("function"),
    Return("return"), Call("call");
    private List<String> alias;
    CommandType(String... alias){
        this.alias = Arrays.asList(alias);
    }
    private static HashMap<String,CommandType> commandMap;
    static{
        commandMap = new HashMap<>();
        for(CommandType commandType : CommandType.values()) {
           for(String s : commandType.alias)
            commandMap.put(s,commandType);
        }
    }
    public static CommandType getFromAlias(String alias){
        return commandMap.getOrDefault(alias,null);
    }
}

class Parser {
    private Scanner scanner;
    private String curr;
    private String[] currArgs;
    private CommandType currCommandType;
    Parser(Reader reader){
        scanner = new Scanner(reader);
    }

    boolean hasMoreCommands(){
        return scanner.hasNextLine();
    }

    void advance(){
        curr = scanner.nextLine().trim();
        while(hasMoreCommands() &&  curr.length()==0 || curr.startsWith("//"))
            curr = scanner.nextLine();
        removeInlineComment();
        if(!curr.isEmpty()) {
            decompose();
            commandType();
        }
    }

    private void removeInlineComment(){
        int i = curr.indexOf("//");
        if(i > 0) curr = curr.substring(0 , i);
    }

    private void commandType() {
        currCommandType = CommandType.getFromAlias(currArgs[0].trim());
        if(currCommandType==null) throw new AssertionError("Command Type is Unknown.");
    }

    private void decompose(){
        currArgs  =  curr.split(" ",3);
    }

    String arg1(){
        //not for return, arithmetic
        if (currCommandType==CommandType.Return || currCommandType==CommandType.Arithmetic)
            throw new IllegalArgumentException("Should not be this type.");
        return currArgs[1].trim();
    }

    int arg2(){
        //for push, pop, function, call
        if(currCommandType==CommandType.Call || currCommandType==CommandType.Function
                || currCommandType==CommandType.Pop || currCommandType==CommandType.Push)
            return Integer.parseInt(currArgs[2].trim());
        throw new AssertionError("This argument type is not allowed.");
    }

    boolean isValidForArg1(){
        return currCommandType!=CommandType.Arithmetic && currCommandType!=CommandType.Return;
    }

    boolean isValidForArg2(){
        return currCommandType==CommandType.Push ||
                currCommandType==CommandType.Pop ||
                currCommandType==CommandType.Function || currCommandType==CommandType.Call;
    }

    String getCommand(){
        return currArgs[0].trim();
    }

    CommandType getCurrCommandType(){
        return  currCommandType;
    }

    //TODO: Handle other Commands

    String getCurr(){
        return curr;
    }

    void close(){
        scanner.close();
    }
}
