import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class JackCompilerUtils {
    // map used for html symbolization
    private static final Map<String,String> htmlSymbols = Map.ofEntries(
            Map.entry("&","&amp;"),
            Map.entry("<","&lt;"),
            Map.entry(">","&gt;")
    );

    private static final HashSet<String> operators = new HashSet<>(Arrays.asList(
        "+","-","*","/","&","|","<",">","~","="
    ));

    private static final Map<String, Command> operatorCommandMap= Map.ofEntries(
      Map.entry("+",Command.ADD),
      Map.entry("-",Command.SUB),
      Map.entry("&",Command.AND),
      Map.entry("<",Command.LT),
      Map.entry(">",Command.GT),
      Map.entry("u~",Command.NOT), // unary type modification
      Map.entry("=",Command.EQ),
      Map.entry("u-",Command.NEG), // unary type modification
      Map.entry("|",Command.OR)
    );

    private static final HashSet<String> statementPrefix = new HashSet<>(Arrays.asList(
       "do", "let", "while", "if", "return"
    ));

    // Below methods validates whether a token is operation , statement or unary
    static boolean isOperator(String tokenVal) {
        return operators.contains(tokenVal);
    }

    static boolean isStatement(String tokenVal) {
        return statementPrefix.contains(tokenVal);
    }

    static boolean isUnary(String tokenVal) {
        return tokenVal.equals("~") || tokenVal.equals("-");
    }

    // VMname generated with class namespace attached
    static String getVMName(String className, String idf){
        return className+"."+idf;
    }

    static Command getCommand(String opr){
        return operatorCommandMap.getOrDefault(opr,null);
    }

    // get segment for the extracted variable from symbol table
    static Segment getSegment(Kind kind){
        switch (kind){
            case VAR -> {
                return Segment.LOCAL;
            }
            case STATIC -> {
                return Segment.STATIC;
            }
            case ARGUMENT -> {
                return Segment.ARG;
            }
            case FIELD -> {
                return Segment.THIS;
            }
        }
        return null;
    }

    // constant value type checker
    static boolean isConstant(JackToken tokenType){
        return tokenType.equals(JackToken.INT_CONST) || tokenType.equals(JackToken.STRING_CONST)  || tokenType.equals(JackToken.KEYWORD);
    }
}
