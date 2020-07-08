enum Token{
    Semi(";"),
    Equals("="),
    Plus("+"),
    Minus("-"),
    Or("|"),
    And("&"),
    At("@"),
    EOS("\n"),
    Comment("//"),
    Not("!",true,false),
    UnaryMinus("-",true,false),
    JumpEquals("JEQ",false,true),
    JumpLessThan("JLT",false,true),
    JumpGreaterThan("JGT",false,true),
    JumpNotEquals("JEQ",false,true),
    Jump("JMP",false,true);
    String val;
    private boolean isUnary;
    private boolean isRelational;
    Token(String val){
        this.val = val;
    }
    Token(String val,boolean isUnary,boolean isRelational){
        this.val = val;
        this.isUnary = isUnary;
        if(!isUnary) this.isRelational = isRelational;
    }
    public boolean isRelational(){return isRelational;}
    public boolean isUnary() {
        return isUnary;
    }
}