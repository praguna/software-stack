enum Constants{
    SP("R0"),
    Addr("A"),
    Data("D"),
    Memory("M"),
    One("1"),
    Zero("0"),
    MinusOne("-1"),
    Local("R1"),
    Argument("R2"),
    This("R3"),
    That("R4"),
    Temp("R5"),
    Static("R16"),
    Pointer("R3"),
    GPR1("R13"),
    GPR2("R14"),
    GPR3("R15"),
    RelationalLabel("REL");
    String val;
    Constants(String val){
        this.val = val;
    }
}