abstract class AssemblyArithmeticWriter {
    static String add(){  return ExpressionUtils.buildArithmeticExpr(Token.Plus); }

    static String sub(){ return ExpressionUtils.buildArithmeticExpr(Token.Minus); }

    static  String or(){  return ExpressionUtils.buildArithmeticExpr(Token.Or); }

    static String and(){   return ExpressionUtils.buildArithmeticExpr(Token.And);  }

    static String not(){  return  ExpressionUtils.buildArithmeticExpr(Token.Not); }

    static String neg(){  return  ExpressionUtils.buildArithmeticExpr(Token.UnaryMinus); }

    static String eq(){ return ExpressionUtils.buildArithmeticExpr(Token.JumpEquals); }

    static String lt(){ return ExpressionUtils.buildArithmeticExpr(Token.JumpLessThan); }

    static String gt(){ return ExpressionUtils.buildArithmeticExpr(Token.JumpGreaterThan); }
}
