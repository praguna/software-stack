import java.util.Map;

class JackTokenizerUtils {
    private static Map<String,String> htmlSymbols = Map.ofEntries(
            Map.entry("&","&amp;"),
            Map.entry("<","&lt;"),
            Map.entry(">","&gt;")
    );

    static String getHtml(String symbol){
        return htmlSymbols.getOrDefault(symbol,symbol);
    }

    static String getOpenTag(String token){
        return "<"+ token +">";
    }

    static String getCloseTag(String token){
        return "</"+token+">";
    }
}
