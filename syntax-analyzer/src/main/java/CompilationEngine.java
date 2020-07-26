import java.io.*;

/*
    Handles Jack Grammar
    Implementation : Top down Recursion, LL(2) Parser
 */
class CompilationEngine {
    private JackTokenizer jackTokenizer;
    private PrintWriter writer;
    private InputStream inputStream;
    CompilationEngine(FileInputStream inputStream, Writer writer){
        this.writer = (PrintWriter) writer;
        this.jackTokenizer = new JackTokenizer(inputStream);
        this.inputStream = inputStream;
    }

    // compile class NT
    void compileClass() throws Exception {
        eat("class", Token.KEYWORD);
        eatIdentifier();
        eat("{",Token.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("static") || tokenVal.equals("field")){
            compileClassVarDec();
            tokenVal = getTokenVal();
        }
        while (tokenVal.equals("constructor") || tokenVal.equals("function") || tokenVal.equals("method")){
            compileSubroutine();
            tokenVal = getTokenVal();
        }
        eat("}",Token.SYMBOL);
    }

    // compile Declared Variables of class or Method
    void compileClassVarDec() throws Exception {
            String tokenVal = getTokenVal();
            eat(tokenVal, Token.KEYWORD);
            eatType();
            eatIdentifier();
            tokenVal = getTokenVal();
            while(tokenVal.equals(",")) {
                eat(",", Token.SYMBOL);
                eatIdentifier();
                tokenVal = getTokenVal();
            }
            eat(";", getTokenType());
    }

    // The below functions compile a method, function or constructor, parameter list, variable Declaration
    void compileSubroutine() throws Exception {
        String tokenVal = getTokenVal();
        eat(tokenVal, Token.KEYWORD);
        tokenVal = getTokenVal();
        if(tokenVal.equals("void")){
            eat(tokenVal, Token.KEYWORD);
        }else{
            eatType();
        }
        eatIdentifier();
        eat("(",Token.SYMBOL);
        if(!getTokenVal().equals(")")) {
            compileParameterList();
        }
        eat(")",Token.SYMBOL);
        compileSubroutineBody();
    }

    void compileSubroutineBody() throws Exception {
        eat("{",Token.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("var")) {
            compileVarDec();
            tokenVal = getTokenVal();
        }
        compileStatements();
        eat("}",Token.SYMBOL);
    }


    void compileParameterList() throws Exception {
        eatType();
        eatIdentifier();
        String tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",",Token.SYMBOL);
            eatType();
            eatIdentifier();
            tokenVal = getTokenVal();
        }
    }

    void compileVarDec() throws Exception {
        eat("var",Token.KEYWORD);
        compileClassVarDec();
    }

    void compileStatements() throws Exception {
        String tokenVal = getTokenVal();
        if(!JackAnalyzerUtils.isStatement(tokenVal)){
            throwException("Statement Type",tokenVal,tokenVal);
        }
        while(JackAnalyzerUtils.isStatement(tokenVal)){
            switch (tokenVal) {
                case "do" -> compileDo();
                case "let" -> compileLet();
                case "while" -> compileWhile();
                case "if" -> compileIf();
                case "return" -> compileReturn();
            }
            tokenVal = getTokenVal();
        }
    }

    // Below methods compile statements
    void compileDo() throws Exception {
        eat("do", Token.KEYWORD);
        eatIdentifier();
        String tokenVal = getTokenVal();
        if(tokenVal.equals(".")) {
            eat(".", Token.SYMBOL);
            eatIdentifier();
        }
        eat("(", Token.SYMBOL);
        compileExpressionList();
        eat(")",Token.SYMBOL);
        eat(";",Token.SYMBOL);
    }

    void compileLet() throws Exception {
        eat("let",Token.KEYWORD);
        eatIdentifier();
        String tokenVal = getTokenVal();
        if(tokenVal.equals("[")){
            eat("[",Token.SYMBOL);
            compileExpression();
            eat("]",Token.SYMBOL);
        }
        eat("=",Token.SYMBOL);
        compileExpression();
        eat(";",Token.SYMBOL);
    }

    void compileWhile() throws Exception {
        eat("while",Token.KEYWORD);
        eat("(",Token.SYMBOL);
        compileExpression();
        eat(")",Token.SYMBOL);
        eat("{",Token.SYMBOL);
        compileStatements();
        eat("}",Token.SYMBOL);
    }

    void compileIf() throws Exception {
        eat("if",Token.KEYWORD);
        eat("(",Token.SYMBOL);
        compileExpression();
        eat(")",Token.SYMBOL);
        eat("{",Token.SYMBOL);
        compileStatements();
        eat("}",Token.SYMBOL);
        String tokenVal =  getTokenVal();
        if(tokenVal.equals("else")){
            eat("else",Token.KEYWORD);
            eat("{",Token.SYMBOL);
            compileStatements();
            eat("}",Token.SYMBOL);
        }
    }

    void compileReturn() throws Exception {
        eat("return",Token.KEYWORD);
        compileExpression();
        eat(";",Token.SYMBOL);
    }

    void compileExpression() throws Exception {
        compileTerm();
        String tokenVal = getTokenVal();
        while(JackAnalyzerUtils.isOperator(tokenVal)){
            compileTerm();
            tokenVal =getTokenVal();
        }
    }

    void compileExpressionList() throws Exception {
        compileExpression();
        String tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",",Token.SYMBOL);
            compileExpression();
        }
    }

    // usage of LL(2) parsing
    void compileTerm() throws Exception {
        String tokenVal = getTokenVal();
        Token tokeType = getTokenType();
        if(tokeType.equals(Token.INT_CONST) || tokeType.equals(Token.STRING_CONST)  || tokeType.equals(Token.KEYWORD)) {
           eat(tokenVal, tokeType);
           return;
        }
        else if(JackAnalyzerUtils.isUnary(tokenVal)){
            eat(tokenVal, Token.SYMBOL);
            return;
        }
        else if(tokenVal.equals("(")){
            eat("(", Token.SYMBOL);
            compileExpression();
            eat(")", Token.SYMBOL);
            return;
        }
        eatIdentifier();
        tokenVal = getTokenVal();
        if(tokenVal.equals("[")){
            eat("[", Token.SYMBOL);
            compileExpression();
            eat("]", Token.SYMBOL);
            return;
        }
        // sub routine body checker
        if(tokenVal.equals(".")) {
            eat(".", Token.SYMBOL);
            eatIdentifier();
        }
        eat("(", Token.SYMBOL);
        compileExpressionList();
        eat(")",Token.SYMBOL);
    }

    // complete syntax Analyzer process
    void compileSyntaxAnalyzer() throws Exception {
        compileClass();
    }

    private Token getTokenType() {
        return jackTokenizer.getTokenType();
    }

    private void eatType() throws Exception {
        String tokenVal = getTokenVal();
        Token tokenType = getTokenType();
        if(tokenType.equals(Token.KEYWORD) && !JackKeyword.isType(tokenVal)){
            throwException("DataType int, char or boolean",tokenVal,tokenVal);
        }
        if(tokenType.equals(Token.IDENTIFIER)) eat(tokenVal ,Token.IDENTIFIER);
        else eat(tokenVal,Token.KEYWORD);
    }

    private void eatIdentifier() throws Exception {
        String tokenVal = getTokenVal();
        eat(tokenVal ,Token.IDENTIFIER);
    }

    private void eat(String s, Token token) throws Exception {
        String tokenVal = getTokenVal();
        if(!s.equals(tokenVal)){
            throwException(s,tokenVal,tokenVal);
        }
        if(!getTokenType().equals(token)){
            throwException(token.getAlias(),getTokenType().getAlias(),tokenVal);
        }
        if(!jackTokenizer.hasMoreTokens()){
            throw new Exception(String.format("Reached end of file Too Soon @ :: %s",s));
        }
        jackTokenizer.advance();
    }

    private void throwException(String expected, String got, String at) throws Exception {
        throw new Exception(String.format("Mismatch expected %s got :: %s @ %s",expected,got,at));
    }


    private String getTokenVal() {
        return jackTokenizer.getStringVal();
    }

    // creates a file of the output of Tokenization, this is used in unit testing for correct token generation
    void compileOnlyTokens() throws Exception {
        writer.println("<tokens>");
        while(jackTokenizer.hasMoreTokens()){
            if(jackTokenizer.getStringVal()!=null && jackTokenizer.getTokenType()!=null){
                String entry = jackTokenizer.getStringVal();
                if(jackTokenizer.getTokenType().equals(Token.SYMBOL)){
                    entry = JackAnalyzerUtils.getHtml(entry);
                }
                String val = JackAnalyzerUtils.getOpenTag(jackTokenizer.getTokenType().getAlias()) + " "+entry+" "
                        + JackAnalyzerUtils.getCloseTag(jackTokenizer.getTokenType().getAlias());
                writer.println(val);

            }
            jackTokenizer.advance();
        }
        writer.println("</tokens>");
    }

    // close all streams and print the end message
    void close(String of) throws IOException {
        System.out.println(String.format("Done writing to Output File @ : %s  :)",of));
        writer.close();
        jackTokenizer.close();
        inputStream.close();
    }
}
