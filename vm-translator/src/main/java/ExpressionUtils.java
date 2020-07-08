class ExpressionUtils{

    private static int numRelationalLines = 0;

    static String buildArithmeticExpr(Token opr){
        StringBuilder sb = new StringBuilder();
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.One), Constants.Memory,null, sb);
        CommandUtils.CCommand(Constants.Memory ,Constants.Addr,null, sb);
        if(opr.isUnary()) buildArithmeticUnaryEvaluation(opr,sb);
        else buildArithmeticBinaryEvaluation(opr,sb);
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Plus,Constants.One), Constants.Memory,null, sb);
        return sb.toString();
    }

    static private void buildArithmeticUnaryEvaluation(Token opr,StringBuilder sb){
        CommandUtils.CCommand(CommandUtils.unaryOp(opr,Constants.Memory),Constants.Memory,null,sb);
    }

    static private void buildArithmeticBinaryEvaluation(Token opr,StringBuilder sb){
        CommandUtils.CCommand(Constants.Memory ,Constants.Data,null, sb);
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.One), Constants.Memory,null, sb);
        CommandUtils.CCommand(Constants.Memory ,Constants.Addr,null, sb);
        if(opr.isRelational()) buildArithmeticRelationalEvaluation(opr,sb);
        else CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,opr,Constants.Data), Constants.Memory,null, sb);
    }

    static private void buildArithmeticRelationalEvaluation(Token opr, StringBuilder sb){
        String label1 = Constants.RelationalLabel.val+"T"+numRelationalLines;
        String label2 = Constants.RelationalLabel.val+numRelationalLines;
        CommandUtils.CCommand(CommandUtils.binaryOp(Constants.Memory,Token.Minus,Constants.Data),Constants.Data,null,sb);
        CommandUtils.ACommand(label1,sb);
        CommandUtils.CCommand(Constants.Data,null,opr,sb);
        CommandUtils.CCommand(Constants.Zero,Constants.Data,null,sb);
        CommandUtils.ACommand(label2,sb);
        CommandUtils.CCommand(Constants.Zero,null,Token.Jump,sb);
        CommandUtils.LCommand(label1,sb);
        CommandUtils.CCommand(Constants.MinusOne,Constants.Data,null,sb);
        CommandUtils.LCommand(label2,sb);
        CommandUtils.ACommand(Constants.SP,sb);
        CommandUtils.CCommand(Constants.Memory,Constants.Addr,null,sb);
        CommandUtils.CCommand(Constants.Data,Constants.Memory,null,sb);
        ++numRelationalLines;
    }


    static int getNumRelationalLines(){
        return numRelationalLines;
    }
}
