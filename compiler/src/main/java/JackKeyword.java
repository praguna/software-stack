enum JackKeyword {
    CLASS,
    METHOD,
    FUNCTION,
    CONSTRUCTOR,
    INT,
    BOOLEAN,
    CHAR,
    VOID,
    VAR,
    STATIC,
    FIELD,
    LET,
    DO,
    IF,
    ELSE,
    WHILE,
    RETURN,
    TRUE,
    FALSE,
    NULL,
    THIS;

    // returns if the current token matches any of the keywords or not
    public static boolean matches(String token){
        for(JackKeyword keyword : JackKeyword.values()){
            if(keyword.name().toLowerCase().equals(token)) return true;
        }
        return false;
    }

    // checks if it is a datatype
    public static boolean isType(String token){
        JackKeyword keyword = JackKeyword.valueOf(token.toUpperCase());
        return keyword.equals(INT) || keyword.equals(BOOLEAN) || keyword.equals(CHAR);
    }

    public static int getKeywordValue(String tokenVal){
        JackKeyword k = JackKeyword.valueOf(tokenVal.toUpperCase());
        return switch (k) {
            case TRUE -> -1;
            case FALSE -> 0;
            case NULL -> 0;
            default -> 0;
        };
    }
}
