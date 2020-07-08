import java.util.Arrays;
import java.util.List;
/* Memory Segment Pop uses GPR1 , Function Return uses GPR2 and GPR3 for EndFrame and Return Address respectively.*/
class ReturnAndCallExpressionUtils extends AssemblyArithmeticWriter{
    private static List<Constants> returnAddressOrder = Arrays.asList(Constants.That,Constants.This,Constants.Argument,Constants.Local);

    private static void initializeReturnAddrAndEndFrame(StringBuilder sb) {
        CommandUtils.ACommand(Constants.Local,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.GPR2,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
        restoreToAddressFromEndFrame(5,Constants.GPR3,sb);
    }

    private static void restoreToAddressFromEndFrame(int index, Constants toaddress, StringBuilder sb){
        CommandUtils.ACommand(Integer.toString(index),sb);
        CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.GPR2,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.Data),Constants.Data,null,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        CommandUtils.ACommand(toaddress,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    private static void storeNewResult(StringBuilder sb){
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.One),Constants.Memory,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.Argument,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    private static void restoreMemorySegments(StringBuilder sb) {
        for (int i = 1; i <= 4 ; i++) {
            restoreToAddressFromEndFrame(i, returnAddressOrder.get(i-1),sb);
        }
    }

    private static void goToReturnAddress(StringBuilder sb){
        CommandUtils.ACommand(Constants.GPR3,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        jump(sb);
    }

    private static void jump(StringBuilder sb){
        CommandUtils.CCommand(Constants.Zero,null,Token.Jump,sb);
    }

    private static void restoreStackPointer(StringBuilder sb){
        CommandUtils.ACommand(Constants.Argument,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Plus,Constants.One),Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    static void executeReturn(StringBuilder sb) {
        initializeReturnAddrAndEndFrame(sb);
        storeNewResult(sb);
        restoreStackPointer(sb);
        restoreMemorySegments(sb);
        goToReturnAddress(sb);
    }

    private static void saveAddressState(String returnAddress, StringBuilder sb){
        CommandUtils.ACommand(returnAddress,sb);
        CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        PushPopUtils.pushFromDataRegister(sb);
        for (int i = 3; i >=0 ; i--) {
            Constants val = returnAddressOrder.get(i);
            CommandUtils.ACommand(val,sb);
            CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
            PushPopUtils.pushFromDataRegister(sb);
        }
    }

    private static void setNewArgs(int nArgs, StringBuilder sb){
        CommandUtils.ACommand(Integer.toString(5),sb);
        CommandUtils.CCommand(Constants.Addr,Constants.Data,null,sb);
        CommandUtils.ACommand(Integer.toString(nArgs),sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Addr,Token.Plus,Constants.Data),Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.Data),Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.Argument,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    private static void setNewLocalAddress(StringBuilder sb){
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Data,null,sb);
        CommandUtils.ACommand(Constants.Local,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
    }

    static void executeCall(String funcName,int callTimes,int numArgs, StringBuilder sb){
        String returnAddress = funcName.concat("$").concat("return.").concat(Integer.toString(callTimes));
        saveAddressState(returnAddress,sb);
        setNewArgs(numArgs,sb);
        setNewLocalAddress(sb);
        CommandUtils.ACommand(funcName,sb);
        jump(sb);
        CommandUtils.LCommand(returnAddress,sb);
    }

}
