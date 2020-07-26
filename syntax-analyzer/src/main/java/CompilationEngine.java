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
        writeOpen("class");
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
        writeClose("class");
    }

    // compile Declared Variables of class or Method
    void compileClassVarDec() throws Exception {
            writeOpen("classVarDec");
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
            writeClose("classVarDec");
    }

    // The below functions compile a method, function or constructor, parameter list, variable Declaration
    void compileSubroutine() throws Exception {
        writeOpen("subroutineDec");
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
        writeOpen("parameterList");
        if(!getTokenVal().equals(")")) {
            compileParameterList();
        }
        writeClose("parameterList");
        eat(")",Token.SYMBOL);
        compileSubroutineBody();
        writeClose("subroutineDec");
    }

    void compileSubroutineBody() throws Exception {
        writeOpen("subroutineBody");
        eat("{",Token.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("var")) {
            compileVarDec();
            tokenVal = getTokenVal();
        }
        compileStatements();
        eat("}",Token.SYMBOL);
        writeClose("subroutineBody");
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
        writeOpen("varDec");
        eat("var",Token.KEYWORD);
        eatType();
        eatIdentifier();
        String tokenValue = getTokenVal();
        while(tokenValue.equals(",")){
            eat(",",Token.SYMBOL);
            eatIdentifier();
            tokenValue = getTokenVal();
        }
        eat(";",Token.SYMBOL);
        writeClose("varDec");
    }

    void compileStatements() throws Exception {
        writeOpen("statements");
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
        writeClose("statements");
    }

    // Below methods compile statements
    void compileDo() throws Exception {
        writeOpen("doStatement");
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
        writeClose("doStatement");
    }

    void compileLet() throws Exception {
        writeOpen("letStatement");
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
        writeClose("letStatement");
    }

    void compileWhile() throws Exception {
        writeOpen("whileStatement");
        eat("while",Token.KEYWORD);
        eat("(",Token.SYMBOL);
        compileExpression();
        eat(")",Token.SYMBOL);
        eat("{",Token.SYMBOL);
        compileStatements();
        eat("}",Token.SYMBOL);
        writeClose("whileStatement");
    }

    void compileIf() throws Exception {
        writeOpen("ifStatement");
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
        writeClose("ifStatement");
    }

    void compileReturn() throws Exception {
        writeOpen("returnStatement");
        eat("return",Token.KEYWORD);
        if(!getTokenVal().equals(";"))
            compileExpression();
        eat(";",Token.SYMBOL);
        writeClose("returnStatement");
    }

    void compileExpression() throws Exception {
        writeOpen("expression");
        compileTerm();
        String tokenVal = getTokenVal();
        while(JackAnalyzerUtils.isOperator(tokenVal)){
            eat(tokenVal,Token.SYMBOL);
            compileTerm();
            tokenVal =getTokenVal();
        }
        writeClose("expression");
    }

    void compileExpressionList() throws Exception {
        writeOpen("expressionList");
        String tokenVal = getTokenVal();
        // empty arguments
        if(tokenVal.equals(")")){
            writeClose("expressionList");
            return;
        }
        compileExpression();
        tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",",Token.SYMBOL);
            compileExpression();
            tokenVal = getTokenVal();
        }
        writeClose("expressionList");
    }

    // usage of LL(2) parsing
    void compileTerm() throws Exception {
        writeOpen("term");
        String tokenVal = getTokenVal();
        Token tokeType = getTokenType();
        if(tokeType.equals(Token.INT_CONST) || tokeType.equals(Token.STRING_CONST)  || tokeType.equals(Token.KEYWORD)) {
           eat(tokenVal, tokeType);
            writeClose("term");
           return;
        }
        else if(JackAnalyzerUtils.isUnary(tokenVal)){
            eat(tokenVal, Token.SYMBOL);
            compileTerm();
            writeClose("term");
            return;
        }
        else if(tokenVal.equals("(")){
            eat("(", Token.SYMBOL);
            compileExpression();
            eat(")", Token.SYMBOL);
            writeClose("term");
            return;
        }
        eatIdentifier();
        tokenVal = getTokenVal();
        if(tokenVal.equals("[")){
            eat("[", Token.SYMBOL);
            compileExpression();
            eat("]", Token.SYMBOL);
            writeClose("term");
            return;
        }
        // sub routine body checker
        if (tokenVal.equals(".") || tokenVal.equals("(")) {
            if(tokenVal.equals(".")) {
                eat(".", Token.SYMBOL);
                eatIdentifier();
            }
            eat("(", Token.SYMBOL);
            compileExpressionList();
            eat(")", Token.SYMBOL);
        }
        writeClose("term");
    }

    // complete syntax Analyzer process
    void compileSyntaxAnalyzer() throws Exception {
        // read the first token, expected as class
        advance();
        compileClass();
    }

    private Token getTokenType() {
        return jackTokenizer.getTokenType();
    }

    // wrapper to Consume Data Type
    private void eatType() throws Exception {
        String tokenVal = getTokenVal();
        Token tokenType = getTokenType();
        if(tokenType.equals(Token.KEYWORD) && !JackKeyword.isType(tokenVal)){
            throwException("DataType int, char or boolean",tokenVal,tokenVal);
        }
        if(tokenType.equals(Token.IDENTIFIER)) eat(tokenVal ,Token.IDENTIFIER);
        else eat(tokenVal,Token.KEYWORD);
    }

    // wrapper to Consume identifier
    private void eatIdentifier() throws Exception {
        String tokenVal = getTokenVal();
        eat(tokenVal ,Token.IDENTIFIER);
    }

    // consume a token validate and advance
    private void eat(String s, Token token) throws Exception {
        String tokenVal = getTokenVal();
        if(!s.equals(tokenVal)){
            throwException(s,tokenVal,tokenVal);
        }
        if(!getTokenType().equals(token)){
            throwException("<type>"+token.getAlias(),"<type>"+getTokenType().getAlias(),tokenVal);
        }
        if(!jackTokenizer.hasMoreTokens()){
            throw new Exception(String.format("Reached end of file Too Soon @ :: %s",s));
        }
        if(token.equals(Token.SYMBOL))
            writer.printf("<%s> %s </%s>\n",token.getAlias(),JackAnalyzerUtils.getHtml(tokenVal),token.getAlias());
        else
            writer.printf("<%s> %s </%s>\n",token.getAlias(),tokenVal,token.getAlias());
        advance();
    }

    // wrapper to print Exception
    private void throwException(String expected, String got, String at) throws Exception {
        throw new Exception(String.format("Mismatch expected %s got :: %s @ %s",expected,got,at));
    }

    // wrapper to getTokenValue
    private String getTokenVal() {
        return jackTokenizer.getStringVal();
    }

    private void writeOpen(String val){
        writer.printf("<%s>\n",val);
    }

    private void writeClose(String val){
        writer.printf("</%s>\n",val);
    }

    // advance wrapper for Tokenizer
    private void advance() throws Exception {
        jackTokenizer.advance();
        while (jackTokenizer.hasMoreTokens() && getTokenVal()==null) {
            jackTokenizer.advance();
        }
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
