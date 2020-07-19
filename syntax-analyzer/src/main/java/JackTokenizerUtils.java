import java.util.Map;

class JackTokenizerUtils {
    // map used for html symbolization
    private static Map<String,String> htmlSymbols = Map.ofEntries(
            Map.entry("&","&amp;"),
            Map.entry("<","&lt;"),
            Map.entry(">","&gt;")
    );

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
}
