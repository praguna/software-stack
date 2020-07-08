import java.lang.reflect.Field;
import java.util.List;

/* Memory Segment Pop uses GPR1 , Function Return uses GPR2 and GPR3 for EndFrame and Return Address respectively.*/
class PushPopUtils {
    private static int numStaticVars=0;
    private static <T>void fetchIntoDataRegister(T address, int index, boolean isPointer, StringBuilder sb){
        CommandUtils.ACommand(Integer.toString(index),sb);
        CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        CommandUtils.ACommand(CommandUtils.getValue(address),sb);
        Constants arg1 = (isPointer)?Constants.Memory:Constants.Addr;
        CommandUtils.CCommand(CommandUtils.binaryOp(arg1,Token.Plus,Constants.Data),Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
    }

   private static  <T>void storeLocationIntoGPR(T address, int index, boolean isPointer, StringBuilder sb){
        CommandUtils.ACommand(Integer.toString(index),sb);
        CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        CommandUtils.ACommand(CommandUtils.getValue(address),sb);
        Constants arg1 = (isPointer)?Constants.Memory:Constants.Addr;
        CommandUtils.CCommand(CommandUtils.binaryOp(arg1,Token.Plus,Constants.Data),Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.GPR1,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    private static void loadDataFromStackAndStore(StringBuilder sb){
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.One),Constants.Memory,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.GPR1,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    static void pushFromDataRegister(StringBuilder load){
        CommandUtils.ACommand(Constants.SP,load);
        CommandUtils.CCommand(Constants.Memory ,Constants.Addr,null, load);
        CommandUtils.CCommand(Constants.Data ,Constants.Memory,null, load);
        CommandUtils.ACommand(Constants.SP,load);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Plus,Constants.One),Constants.Memory,null,load);
    }

    static void pushFromGPR(Constants GPR,StringBuilder sb){
        CommandUtils.ACommand(GPR,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        pushFromDataRegister(sb);
    }

    static void popIntoGPR(Constants GPR, StringBuilder sb) {
        CommandUtils.ACommand(Constants.SP, sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory, Token.Minus, Constants.One), Constants.Memory, null, sb);
        CommandUtils.CCommand(Constants.Memory, Constants.Addr, null, sb);
        CommandUtils.CCommand(Constants.Memory, Constants.Addr, null, sb);
        CommandUtils.CCommand(Constants.Memory, Constants.Data, null, sb);
        CommandUtils.ACommand(GPR, sb);
        CommandUtils.CCommand(Constants.Data, Constants.Memory, null, sb);
    }


    private static Constants processSegment(String segment) throws NoSuchFieldException, IllegalAccessException {
        String name = segment.substring(0,1).toUpperCase()+segment.substring(1);
        Field field = Constants.class.getField(name);
        return (Constants) field.get(null);
    }

    private static int getStaticIndex(String segment,int index) throws NoSuchFieldException, IllegalAccessException {
        if(processSegment(segment).equals(Constants.Static)){
            String scope = CodeWriter.functionStack.peek();
            List<Integer> list =  CodeWriter.staticMap.get(scope);
            try{
                return list.get(index);
            }
            catch (Exception ignored){}
        }
        return -1;
    }

    static String Push(int index,String segment) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder sb  = new StringBuilder();
        if(segment.equalsIgnoreCase("constant")){
            CommandUtils.ACommand(Integer.toString(index),sb);
            CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        }
        else {
            int new_index = getStaticIndex(segment, index);
            if (new_index != -1) index = new_index;
            PushPopUtils.fetchIntoDataRegister(processSegment(segment), index, isPointer(segment), sb);
        }
        PushPopUtils.pushFromDataRegister(sb);
        return sb.toString();
    }

    static String Pop(int index,String segment) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        if(processSegment(segment).equals(Constants.Static)){
            int new_index = getStaticIndex(segment,index);
            if(new_index==-1){
                if(index < numStaticVars) index = numStaticVars;
            }
            else index = new_index;
            int finalIndex = index;
            CodeWriter.staticMap.computeIfPresent(CodeWriter.functionStack.peek(),(k, v)->{
                v.add(finalIndex);
                return v;
            });
            ++numStaticVars;
        }
        PushPopUtils.storeLocationIntoGPR(processSegment(segment),index,isPointer(segment),sb);
        PushPopUtils.loadDataFromStackAndStore(sb);
        return sb.toString();
    }

    private static boolean isPointer(String segment){
        return !(segment.equalsIgnoreCase("temp") || segment.equalsIgnoreCase("static") ||
                segment.equalsIgnoreCase("pointer"));
    }

}
