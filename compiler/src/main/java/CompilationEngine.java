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
    private final VMWriter vmWriter;
    private String currType;
    private Kind currkind;
    private String currSubroutine;
    private String currClassName;
    CompilationEngine(FileInputStream inputStream, PrintWriter writer){
        this.writer = writer;
        this.vmWriter = new VMWriter(writer);
        this.jackTokenizer = new JackTokenizer(inputStream);
        this.symbolTable = new SymbolTable();
    }

    // compile class NT
    void compileClass() throws Exception {
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
    }



    // compile Declared Variables of class or Method
    void compileClassVarDec() throws Exception {
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
    }

    // The below functions compile a method, function or constructor, parameter list, variable Declaration
    void compileSubroutine() throws Exception {
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
        if(!getTokenVal().equals(")")) {
            compileParameterList();
        }
        eat(")", JackToken.SYMBOL);
        compileSubroutineBody();
        symbolTable.resetSubroutine(currSubroutine);
        setCurrSubroutine(null);
    }

    void compileSubroutineBody() throws Exception {
        eat("{", JackToken.SYMBOL);
        String tokenVal = getTokenVal();
        while(tokenVal.equals("var")) {
            compileVarDec();
            tokenVal = getTokenVal();
        }
        compileStatements();
        eat("}", JackToken.SYMBOL);
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
    }

    void compileStatements() throws Exception {
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
    }

    // Below methods compile statements
    void compileDo() throws Exception {
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
    }

    void compileLet() throws Exception {
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
    }

    void compileWhile() throws Exception {
        eat("while", JackToken.KEYWORD);
        eat("(", JackToken.SYMBOL);
        compileExpression();
        eat(")", JackToken.SYMBOL);
        eat("{", JackToken.SYMBOL);
        compileStatements();
        eat("}", JackToken.SYMBOL);
    }

    void compileIf() throws Exception {
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
    }

    void compileReturn() throws Exception {
        eat("return", JackToken.KEYWORD);
        if(!getTokenVal().equals(";"))
            compileExpression();
        eat(";", JackToken.SYMBOL);
    }

    void compileExpression() throws Exception {
        compileTerm();
        String tokenVal = getTokenVal();
        while(JackCompilerUtils.isOperator(tokenVal)){
            eat(tokenVal, JackToken.SYMBOL);
            compileTerm();
            tokenVal =getTokenVal();
        }
    }

    void compileExpressionList() throws Exception {
        String tokenVal = getTokenVal();
        // empty arguments
        if(tokenVal.equals(")")){
            return;
        }
        compileExpression();
        tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",", JackToken.SYMBOL);
            compileExpression();
            tokenVal = getTokenVal();
        }
    }

    // usage of LL(2) parsing
    void compileTerm() throws Exception {
        String tokenVal = getTokenVal();
        JackToken tokeType = getTokenType();
        if(tokeType.equals(JackToken.INT_CONST) || tokeType.equals(JackToken.STRING_CONST)  || tokeType.equals(JackToken.KEYWORD)) {
           eat(tokenVal, tokeType);
           return;
        }
        else if(JackCompilerUtils.isUnary(tokenVal)){
            eat(tokenVal, JackToken.SYMBOL);
            compileTerm();
            return;
        }
        else if(tokenVal.equals("(")){
            eat("(", JackToken.SYMBOL);
            compileExpression();
            eat(")", JackToken.SYMBOL);
            return;
        }
        eatIdentifier();
        tokenVal = getTokenVal();
        if(tokenVal.equals("[")){
            eat("[", JackToken.SYMBOL);
            compileExpression();
            eat("]", JackToken.SYMBOL);
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
        advance();
    }

    // wrapper to print Exception
    private void throwException(String expected, String got, String at) throws Exception {
        throw new Exception(String.format("Mismatch expected %s got :: %s, in line :: <%s>, token :: %s",expected,got,jackTokenizer.getLocation(),at));
    }

    // Setters for some useful variables
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

    // advance wrapper for Tokenizer
    private void advance() throws Exception {
        jackTokenizer.advance();
        while (jackTokenizer.hasMoreTokens() && getTokenVal()==null) {
            jackTokenizer.advance();
        }
    }

    // close all streams and print the end message
    void close(String of) throws IOException {
        vmWriter.close();
        jackTokenizer.close();
        symbolTable.printClassScope(currClassName);
        System.out.printf("Done writing to Output File @ : %s  :)%n", of);
    }
}
