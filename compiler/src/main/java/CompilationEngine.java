import java.io.*;
import java.util.Objects;

/*
    Handles Jack Grammar
    Implementation : Top down Recursion, LL(2) Parser
 */
class CompilationEngine {
    private final JackTokenizer jackTokenizer;
    private final PrintWriter writer;
    private final SymbolTable symbolTable;
    private String currType;
    private Kind currkind;
    private String currSubroutine;
    private String currClassName;
    private final boolean stopXmlWrites;
    CompilationEngine(FileInputStream inputStream, PrintWriter writer){
        this.writer = writer;
        this.jackTokenizer = new JackTokenizer(inputStream);
        this.symbolTable = new SymbolTable();
        this.stopXmlWrites = true;
    }

    // compile class NT
    void compileClass() throws Exception {
        writeOpen("class");
        eat("class", JackToken.KEYWORD);
        currClassName = getTokenVal();
        eatIdentifier();
        eat("{", JackToken.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("static") || tokenVal.equals("field")){
            setCurrKind(tokenVal);
            compileClassVarDec();
            tokenVal = getTokenVal();
        }
        setCurrKind(null);
        while (tokenVal.equals("constructor") || tokenVal.equals("function") || tokenVal.equals("method")){
            if(tokenVal.equals("method")) {
                symbolTable.define("this",currClassName,Kind.ARGUMENT);
            }
            compileSubroutine();
            tokenVal = getTokenVal();
        }
        eat("}", JackToken.SYMBOL);
        if(jackTokenizer.hasMoreTokens()) throwException(null, getTokenVal() ,"last point");
        writeClose("class");
    }



    // compile Declared Variables of class or Method
    void compileClassVarDec() throws Exception {
            writeOpen("classVarDec");
            String tokenVal = getTokenVal();
            eat(tokenVal, JackToken.KEYWORD);
            setCurrType(getTokenVal());
            eatType();
            eatIdentifier();
            tokenVal = getTokenVal();
            while(tokenVal.equals(",")) {
                eat(",", JackToken.SYMBOL);
                eatIdentifier();
                tokenVal = getTokenVal();
            }
            eat(";", getTokenType());
            setCurrType(null);
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
        setCurrSubroutine(getTokenVal());
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
        symbolTable.resetSubroutine(currSubroutine);
        setCurrSubroutine(null);
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
        setCurrKind("argument");
        setCurrType(getTokenVal());
        eatType();
        eatIdentifier();
        String tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",", JackToken.SYMBOL);
            setCurrType(getTokenVal());
            eatType();
            eatIdentifier();
            setCurrType(null);
            tokenVal = getTokenVal();
        }
        setCurrType(null);
        setCurrKind(null);
    }

    void compileVarDec() throws Exception {
        writeOpen("varDec");
        setCurrKind("var");
        eat("var", JackToken.KEYWORD);
        setCurrType(getTokenVal());
        eatType();
        eatIdentifier();
        String tokenValue = getTokenVal();
        while(tokenValue.equals(",")){
            eat(",", JackToken.SYMBOL);
            eatIdentifier();
            tokenValue = getTokenVal();
        }
        eat(";", JackToken.SYMBOL);
        setCurrType(null);
        setCurrKind(null);
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
        if(Objects.nonNull(currkind) && Objects.nonNull(currType)) {
            symbolTable.define(tokenVal, currType, currkind);
        }
    }

    // consume a token validate and advance (heart of the tokenizer)
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
        writeToken(token,tokenVal);
        advance();
    }

    // wrapper to print Exception
    private void throwException(String expected, String got, String at) throws Exception {
        throw new Exception(String.format("Mismatch expected %s got :: %s, in line :: <%s>, token :: %s",expected,got,jackTokenizer.getLocation(),at));
    }


    private void setCurrType(String type){
        this.currType = type;
    }

    private void setCurrKind(String val) {
        if(val==null) this.currkind = null;
        else this.currkind = Kind.valueOf(val.toUpperCase());
    }

    private void setCurrSubroutine(String val){
        this.currSubroutine = val;
    }

    // wrapper to getTokenValue
    private String getTokenVal() {
        return jackTokenizer.getStringVal();
    }

    private void writeOpen(String val){
        if(stopXmlWrites) return;
        writer.printf("<%s>\n",val);
    }

    private void writeToken(JackToken token, String tokenVal){
        if(stopXmlWrites) return;
        writer.printf("<%s> %s </%s>\n",token.getAlias(), JackCompilerUtils.getHtml(tokenVal),token.getAlias());
    }

    private void writeClose(String val){
        if(stopXmlWrites) return;
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
        if(!stopXmlWrites) {
            System.out.printf("Done writing to Output File @ : %s  :)%n", of);
            writer.close();
        }
        jackTokenizer.close();
        symbolTable.printClassScope();
    }
}
