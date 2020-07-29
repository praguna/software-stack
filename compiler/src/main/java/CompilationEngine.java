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
        eat("class", JackToken.KEYWORD);
        eatIdentifier();
        eat("{", JackToken.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("static") || tokenVal.equals("field")){
            compileClassVarDec();
            tokenVal = getTokenVal();
        }
        while (tokenVal.equals("constructor") || tokenVal.equals("function") || tokenVal.equals("method")){
            compileSubroutine();
            tokenVal = getTokenVal();
        }
        eat("}", JackToken.SYMBOL);
        writeClose("class");
    }

    // compile Declared Variables of class or Method
    void compileClassVarDec() throws Exception {
            writeOpen("classVarDec");
            String tokenVal = getTokenVal();
            eat(tokenVal, JackToken.KEYWORD);
            eatType();
            eatIdentifier();
            tokenVal = getTokenVal();
            while(tokenVal.equals(",")) {
                eat(",", JackToken.SYMBOL);
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
        eat(tokenVal, JackToken.KEYWORD);
        tokenVal = getTokenVal();
        if(tokenVal.equals("void")){
            eat(tokenVal, JackToken.KEYWORD);
        }else{
            eatType();
        }
        eatIdentifier();
        eat("(", JackToken.SYMBOL);
        writeOpen("parameterList");
        if(!getTokenVal().equals(")")) {
            compileParameterList();
        }
        writeClose("parameterList");
        eat(")", JackToken.SYMBOL);
        compileSubroutineBody();
        writeClose("subroutineDec");
    }

    void compileSubroutineBody() throws Exception {
        writeOpen("subroutineBody");
        eat("{", JackToken.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("var")) {
            compileVarDec();
            tokenVal = getTokenVal();
        }
        compileStatements();
        eat("}", JackToken.SYMBOL);
        writeClose("subroutineBody");
    }


    void compileParameterList() throws Exception {
        eatType();
        eatIdentifier();
        String tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",", JackToken.SYMBOL);
            eatType();
            eatIdentifier();
            tokenVal = getTokenVal();
        }
    }

    void compileVarDec() throws Exception {
        writeOpen("varDec");
        eat("var", JackToken.KEYWORD);
        eatType();
        eatIdentifier();
        String tokenValue = getTokenVal();
        while(tokenValue.equals(",")){
            eat(",", JackToken.SYMBOL);
            eatIdentifier();
            tokenValue = getTokenVal();
        }
        eat(";", JackToken.SYMBOL);
        writeClose("varDec");
    }

    void compileStatements() throws Exception {
        writeOpen("statements");
        String tokenVal = getTokenVal();
        if(!JackCompilerUtils.isStatement(tokenVal)){
            throwException("Statement Type",tokenVal,tokenVal);
        }
        while(JackCompilerUtils.isStatement(tokenVal)){
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
        eat("do", JackToken.KEYWORD);
        eatIdentifier();
        String tokenVal = getTokenVal();
        if(tokenVal.equals(".")) {
            eat(".", JackToken.SYMBOL);
            eatIdentifier();
        }
        eat("(", JackToken.SYMBOL);
        compileExpressionList();
        eat(")", JackToken.SYMBOL);
        eat(";", JackToken.SYMBOL);
        writeClose("doStatement");
    }

    void compileLet() throws Exception {
        writeOpen("letStatement");
        eat("let", JackToken.KEYWORD);
        eatIdentifier();
        String tokenVal = getTokenVal();
        if(tokenVal.equals("[")){
            eat("[", JackToken.SYMBOL);
            compileExpression();
            eat("]", JackToken.SYMBOL);
        }
        eat("=", JackToken.SYMBOL);
        compileExpression();
        eat(";", JackToken.SYMBOL);
        writeClose("letStatement");
    }

    void compileWhile() throws Exception {
        writeOpen("whileStatement");
        eat("while", JackToken.KEYWORD);
        eat("(", JackToken.SYMBOL);
        compileExpression();
        eat(")", JackToken.SYMBOL);
        eat("{", JackToken.SYMBOL);
        compileStatements();
        eat("}", JackToken.SYMBOL);
        writeClose("whileStatement");
    }

    void compileIf() throws Exception {
        writeOpen("ifStatement");
        eat("if", JackToken.KEYWORD);
        eat("(", JackToken.SYMBOL);
        compileExpression();
        eat(")", JackToken.SYMBOL);
        eat("{", JackToken.SYMBOL);
        compileStatements();
        eat("}", JackToken.SYMBOL);
        String tokenVal =  getTokenVal();
        if(tokenVal.equals("else")){
            eat("else", JackToken.KEYWORD);
            eat("{", JackToken.SYMBOL);
            compileStatements();
            eat("}", JackToken.SYMBOL);
        }
        writeClose("ifStatement");
    }

    void compileReturn() throws Exception {
        writeOpen("returnStatement");
        eat("return", JackToken.KEYWORD);
        if(!getTokenVal().equals(";"))
            compileExpression();
        eat(";", JackToken.SYMBOL);
        writeClose("returnStatement");
    }

    void compileExpression() throws Exception {
        writeOpen("expression");
        compileTerm();
        String tokenVal = getTokenVal();
        while(JackCompilerUtils.isOperator(tokenVal)){
            eat(tokenVal, JackToken.SYMBOL);
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
            eat(",", JackToken.SYMBOL);
            compileExpression();
            tokenVal = getTokenVal();
        }
        writeClose("expressionList");
    }

    // usage of LL(2) parsing
    void compileTerm() throws Exception {
        writeOpen("term");
        String tokenVal = getTokenVal();
        JackToken tokeType = getTokenType();
        if(tokeType.equals(JackToken.INT_CONST) || tokeType.equals(JackToken.STRING_CONST)  || tokeType.equals(JackToken.KEYWORD)) {
           eat(tokenVal, tokeType);
            writeClose("term");
           return;
        }
        else if(JackCompilerUtils.isUnary(tokenVal)){
            eat(tokenVal, JackToken.SYMBOL);
            compileTerm();
            writeClose("term");
            return;
        }
        else if(tokenVal.equals("(")){
            eat("(", JackToken.SYMBOL);
            compileExpression();
            eat(")", JackToken.SYMBOL);
            writeClose("term");
            return;
        }
        eatIdentifier();
        tokenVal = getTokenVal();
        if(tokenVal.equals("[")){
            eat("[", JackToken.SYMBOL);
            compileExpression();
            eat("]", JackToken.SYMBOL);
            writeClose("term");
            return;
        }
        // sub routine body checker
        if (tokenVal.equals(".") || tokenVal.equals("(")) {
            if(tokenVal.equals(".")) {
                eat(".", JackToken.SYMBOL);
                eatIdentifier();
            }
            eat("(", JackToken.SYMBOL);
            compileExpressionList();
            eat(")", JackToken.SYMBOL);
        }
        writeClose("term");
    }

    // complete syntax Analyzer process
    void compileSyntaxAnalyzer() throws Exception {
        // read the first token, expected as class
        advance();
        compileClass();
    }

    private JackToken getTokenType() {
        return jackTokenizer.getTokenType();
    }

    // wrapper to Consume Data Type
    private void eatType() throws Exception {
        String tokenVal = getTokenVal();
        JackToken tokenType = getTokenType();
        if(tokenType.equals(JackToken.KEYWORD) && !JackKeyword.isType(tokenVal)){
            throwException("DataType int, char or boolean",tokenVal,tokenVal);
        }
        if(tokenType.equals(JackToken.IDENTIFIER)) eat(tokenVal , JackToken.IDENTIFIER);
        else eat(tokenVal, JackToken.KEYWORD);
    }

    // wrapper to Consume identifier
    private void eatIdentifier() throws Exception {
        String tokenVal = getTokenVal();
        eat(tokenVal , JackToken.IDENTIFIER);
    }

    // consume a token validate and advance
    private void eat(String s, JackToken token) throws Exception {
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
        if(token.equals(JackToken.SYMBOL))
            writer.printf("<%s> %s </%s>\n",token.getAlias(), JackCompilerUtils.getHtml(tokenVal),token.getAlias());
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

    // close all streams and print the end message
    void close(String of) throws IOException {
        System.out.println(String.format("Done writing to Output File @ : %s  :)",of));
        writer.close();
        jackTokenizer.close();
        inputStream.close();
    }
}
