import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

class CodeWriter{
    private PrintWriter out;
    private HashMap<String,Integer> numberOfCalls;
    static HashMap<String, List<Integer>> staticMap;
    static Stack<String> functionStack;
    CodeWriter(PrintWriter writer){
        out = writer;
        numberOfCalls = new HashMap<>();
        staticMap = new HashMap<>();
        functionStack = new Stack<>();
        functionStack.push("global");
        staticMap.put("global",new ArrayList<>());
    }

    void writeInit() {
        StringBuilder sb = new StringBuilder();
        writComment("Initializer");
        CommandUtils.ACommand(Integer.toString(261),sb);
        CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
        out.println(sb);
    }

    void writComment(String comment){
        out.println(Token.Comment.val+comment);
    }

    void writeArithmetic(String command) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
       String expr =  (String) AssemblyArithmeticWriter.class.getDeclaredMethod(command).invoke(this);
       out.println(expr);
    }

    void writePushPop(CommandType commandType,String segment,int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String expr =  (String) CodeWriter.class.getDeclaredMethod(commandType.toString(),int.class,String.class).invoke(this,index,segment);
        out.println(expr);
    }

    void writeLabel(String label){
        StringBuilder sb = new StringBuilder();
        CommandUtils.LCommand(label,sb);
        out.println(sb);
    }

    void writeGoto(String command){
        StringBuilder sb = new StringBuilder();
        CommandUtils.ACommand(command,sb);
        CommandUtils.CCommand(Constants.Zero,null,Token.Jump,sb);
        out.println(sb);
    }

    void writeIfGoto(String command){
        StringBuilder sb = new StringBuilder();
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.One),Constants.Memory,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        CommandUtils.ACommand(command,sb);
        CommandUtils.CCommand(Constants.Data,null,Token.JumpGreaterThan,sb);
        CommandUtils.CCommand(CommandUtils.unaryOp(Token.Not,Constants.Data),Constants.Data,null,sb);
        CommandUtils.ACommand(command,sb);
        CommandUtils.CCommand(Constants.Data,null,Token.JumpEquals,sb);
        out.println(sb);
    }

    private String getScopeName(String name){
        if(name.contains(".")) return name.substring(0,name.indexOf('.'));
        return name;
    }

    void writeFunction(String name,int numVars) throws NoSuchFieldException, IllegalAccessException {

        String scope = getScopeName(name);
        functionStack.push(scope);
        if(!staticMap.containsKey(scope)) staticMap.put(scope, new ArrayList<>());
        writeLabel(name);
        for (int i = 0; i < numVars; i++)
           out.println(Push(0, "constant"));
    }

    void writeCall(String name, int numArgs){
        StringBuilder sb = new StringBuilder();
        Integer count = numberOfCalls.getOrDefault(name,0);
        numberOfCalls.put(name, count+1);
        ReturnAndCallExpressionUtils.executeCall(name,count,numArgs,sb);
        out.println(sb);
    }

    void writeReturn(){
        functionStack.pop();
        StringBuilder sb = new StringBuilder();
        ReturnAndCallExpressionUtils.executeReturn(sb);
        out.println(sb);
    }

    private String Push(int index,String segment) throws NoSuchFieldException, IllegalAccessException {
        return PushPopUtils.Push(index,segment);
    }

    private String Pop(int index,String segment) throws NoSuchFieldException, IllegalAccessException {
        return PushPopUtils.Pop(index,segment);
    }

    void close() {
        out.close();
    }
}
