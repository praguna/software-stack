import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

class JackAnalyzerUtils {
    // map used for html symbolization
    private static Map<String,String> htmlSymbols = Map.ofEntries(
            Map.entry("&","&amp;"),
            Map.entry("<","&lt;"),
            Map.entry(">","&gt;")
    );

    private static HashSet<String> operators = new HashSet<>(Arrays.asList(
        "+","-","*","/","&","|","<",">","~","="
    ));

    private static HashSet<String> statementPrefix = new HashSet<>(Arrays.asList(
       "do", "let", "while", "if", "return"
    ));

    // method that returns html equivalent of the current symbol
    static String getHtml(String symbol){
        return htmlSymbols.getOrDefault(symbol,symbol);
    }

    // method that returns a token value in html open tag
    static String getOpenTag(String token){
        return "<"+ token +">";
    }

    // method that returns a token value in html close tag
    static String getCloseTag(String token){
        return "</"+token+">";
    }

    // method that validates whether a token is operation
    static boolean isOperator(String tokenVal) {
        return operators.contains(tokenVal);
    }

    public static boolean isStatement(String tokenVal) {
        return statementPrefix.contains(tokenVal);
    }

    public static boolean isUnary(String tokenVal) {
        return tokenVal.equals("~") || tokenVal.equals("-");
    }
}
