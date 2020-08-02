import java.io.*;
import java.util.Objects;

/*
    Handles Jack Grammar
    Implementation : Top down Recursion, LL(2) Parser
 */
class CompilationEngine {
    private final JackTokenizer jackTokenizer;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;
    private String currType;
    private Kind currkind;
    private String currSubroutine;
    private String currSubroutineType;
    private String currClassName;
    CompilationEngine(FileInputStream inputStream, PrintWriter writer){
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
        setCurrSubroutineType(tokenVal);
        if(tokenVal.equals("void")){
            eat(tokenVal, JackToken.KEYWORD);
        }else{
            eatType();
        }
        setCurrSubroutine(JackCompilerUtils.getVMName(currClassName,getTokenVal()));
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
        int nLocal = 0;
        while(tokenVal.equals("var")) {
            nLocal+=compileVarDec();
            tokenVal = getTokenVal();
        }
        vmWriter.writeFunction(currSubroutine, nLocal);
        compileStatements();
        vmWriter.writeReturn();
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
            tokenVal = getTokenVal();
        }
        setCurrType(null);
        setCurrKind(null);
    }

    int compileVarDec() throws Exception {
        int varCount = 1;
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
            ++varCount;
        }
        eat(";", JackToken.SYMBOL);
        setCurrType(null);
        setCurrKind(null);
        return varCount;
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
        String funcName = getTokenVal();
        eatIdentifier();
        String tokenVal = getTokenVal();
        if(tokenVal.equals(".")) {
            eat(".", JackToken.SYMBOL);
            funcName=JackCompilerUtils.getVMName(funcName,getTokenVal());
            eatIdentifier();
        }else{
            funcName = JackCompilerUtils.getVMName(currClassName,funcName);
        }
        eat("(", JackToken.SYMBOL);
        int nArgs = compileExpressionList();
        eat(")", JackToken.SYMBOL);
        eat(";", JackToken.SYMBOL);
        vmWriter.writeCall(funcName,nArgs);
        vmWriter.writePop(Segment.TEMP,0);
    }

    void compileLet() throws Exception {
        eat("let", JackToken.KEYWORD);
        String varName = getTokenVal();
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
        writeStore(varName);
    }

    void compileWhile() throws Exception {
        int index = symbolTable.getWhileIndex();
        vmWriter.writeLabel(String.format("%s.while_start_%d",currClassName,index));
        eat("while", JackToken.KEYWORD);
        eat("(", JackToken.SYMBOL);
        compileExpression();
        eat(")", JackToken.SYMBOL);
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(String.format("%s.while_end_%d",currClassName,index));
        eat("{", JackToken.SYMBOL);
        compileStatements();
        eat("}", JackToken.SYMBOL);
        vmWriter.writeGoto(String.format("%s.while_start_%d",currClassName,index));
        vmWriter.writeLabel(String.format("%s.while_end_%d",currClassName,index));
    }

    void compileIf() throws Exception {
        int index = symbolTable.getIfIndex();
        vmWriter.writeLabel(String.format("%s.if_stmt_%d",currClassName,index));
        eat("if", JackToken.KEYWORD);
        eat("(", JackToken.SYMBOL);
        compileExpression();
        eat(")", JackToken.SYMBOL);
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(String.format("%s.if_end_%d",currClassName,index));
        eat("{", JackToken.SYMBOL);
        compileStatements();
        eat("}", JackToken.SYMBOL);
        String tokenVal =  getTokenVal();
        if(tokenVal.equals("else")){
            vmWriter.writeGoto(String.format("%s.if_else_end_%d",currClassName,index));
            vmWriter.writeLabel(String.format("%s.if_end_%d",currClassName,index));
            eat("else", JackToken.KEYWORD);
            eat("{", JackToken.SYMBOL);
            compileStatements();
            eat("}", JackToken.SYMBOL);
            vmWriter.writeLabel(String.format("%s.if_else_end_%d",currClassName,index));
            return;
        }
        vmWriter.writeLabel(String.format("%s.if_end_%d",currClassName,index));

    }

    void compileReturn() throws Exception {
        eat("return", JackToken.KEYWORD);
        if(!getTokenVal().equals(";")) {
            compileExpression();
        }else{
            if(!currSubroutineType.equals("void"))
                throwException(String.format("Subroutine %s is expected to return type %s",currSubroutine,currSubroutineType));
        }
        eat(";", JackToken.SYMBOL);
    }

    void compileExpression() throws Exception {
        compileTerm();
        String tokenVal = getTokenVal();
        while(JackCompilerUtils.isOperator(tokenVal)){
            eat(tokenVal, JackToken.SYMBOL);
            compileTerm();
            writeOperation(tokenVal);
            tokenVal =getTokenVal();
        }
    }

    // returns number of expressions in expressions list for do statements
    int compileExpressionList() throws Exception {
        int nArgs = 0;
        String tokenVal = getTokenVal();
        // empty arguments
        if(tokenVal.equals(")")){
            return nArgs;
        }
        compileExpression();
        ++nArgs;
        tokenVal = getTokenVal();
        while(tokenVal.equals(",")){
            eat(",", JackToken.SYMBOL);
            compileExpression();
            tokenVal = getTokenVal();
            ++nArgs;
        }
        return nArgs;
    }

    // usage of LL(2) parsing
    void compileTerm() throws Exception {
        String tokenVal = getTokenVal();
        JackToken tokenType = getTokenType();
        if(JackCompilerUtils.isConstant(tokenType)) {
            writeValues();
            eat(tokenVal, tokenType);
            return;
        }
        else if(JackCompilerUtils.isUnary(tokenVal)){
            eat(tokenVal, JackToken.SYMBOL);
            compileTerm();
            writeOperation("u-");
            return;
        }
        else if(tokenVal.equals("(")){
            eat("(", JackToken.SYMBOL);
            compileExpression();
            eat(")", JackToken.SYMBOL);
            return;
        }
        String idf = getTokenVal();
        eatIdentifier();
        tokenVal = getTokenVal();
        // arrays
        if(tokenVal.equals("[")){
            eat("[", JackToken.SYMBOL);
            compileExpression();
            eat("]", JackToken.SYMBOL);
            return;
        }
        // sub routine call
        if (tokenVal.equals(".") || tokenVal.equals("(")) {
            if(tokenVal.equals(".")) {
                eat(".", JackToken.SYMBOL);
                idf= JackCompilerUtils.getVMName(idf,getTokenVal());
                eatIdentifier();
            }else{
                idf = JackCompilerUtils.getVMName(currClassName,idf);
            }
            eat("(", JackToken.SYMBOL);
            int nArgs = compileExpressionList();
            eat(")", JackToken.SYMBOL);
            vmWriter.writeCall(idf,nArgs);
            return;
        }
        writePut(idf);
    }

    // complete syntax Analyzer process
    void compileSyntaxAnalyzer() throws Exception {
        // read the first token, expected as class
        advance();
        compileClass();
    }

    // compile operators
    private void writeOperation(String tokenVal) {
        if(tokenVal.equals("*"))
            vmWriter.writeCall("Math.multiply" , 2);
        else if(tokenVal.equals("/"))
            vmWriter.writeCall("Math.divide" , 2);
        else
            vmWriter.writeArithmetic(JackCompilerUtils.getCommand(tokenVal));
    }

    //wrapper for pop operation into a segment
    private void writeStore(String varName) throws Exception {
        Segment segment = JackCompilerUtils.getSegment(lookup(varName));
        vmWriter.writePop(Objects.requireNonNull(segment),symbolTable.indexOf(varName));
    }

    // wrapper for push operation into stack
    private void writePut(String varName) throws Exception{
        Segment segment = JackCompilerUtils.getSegment(lookup((varName)));
        vmWriter.writePush(Objects.requireNonNull(segment),symbolTable.indexOf(varName));
    }

    //look up variable in the symbol table
    private Kind lookup(String varName) throws Exception {
        Kind kind = symbolTable.kindOf(varName);
        if(Objects.isNull(kind)) throwException(String.format("Could not find %s ", varName));
        return kind;
    }

    // push constant values of term
    private void writeValues() {
        JackToken tokenType = getTokenType();
        if (tokenType == JackToken.INT_CONST) {
            vmWriter.writePush(Segment.CONST, jackTokenizer.getIntVal());
        }else if(tokenType == JackToken.KEYWORD){
            int value =JackKeyword.getKeywordValue(getTokenVal());
            vmWriter.writePush(Segment.CONST,value);
        }
    }

    // returns current token type
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
            if(Objects.nonNull(symbolTable.kindOf(tokenVal))) throwException(String.format("Symbol is a duplicate %s ", tokenVal));
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

    //wrapper for exception
    private void throwException(String message) throws Exception {
        throw new Exception(String.format("Error :: %s, in line :: <%s>,",message,jackTokenizer.getLocation()));
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

    private void setCurrSubroutineType(String type) {
        this.currSubroutineType = type;
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
        symbolTable.reset();
        System.out.printf("Done writing to Output File @ : %s  :)%n", of);
    }
}
