public enum JackToken {
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    KEYWORD("keyword"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant");

    private String alias;

    JackToken(String symbol) {
        alias = symbol;
    }

    public String getAlias(){
        return alias;
    }
}
