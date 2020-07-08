class CommandUtils {

    static <T>void ACommand(T address, StringBuilder prev){
        prev.append(Token.At.val).append(getValue(address)).append(Token.EOS.val);
    }

    static <T>void CCommand(T comp, T dest, T jmp, StringBuilder prev){
        String resComp = (comp instanceof Constants)?((Constants) comp).val : (String) comp;
        if(dest!=null) prev.append(getValue(dest)).append(Token.Equals.val);
        if(comp!=null) prev.append(resComp);
        if(jmp!=null) prev.append(Token.Semi.val).append(getValue(jmp));
        if(prev.length() > 0) prev.append(Token.EOS.val);
    }

    static <T>void LCommand(T address, StringBuilder prev){
        String resCommand = (address instanceof Constants)?((Constants) address).val : (String) address;
        prev.append("(").append(resCommand).append(")").append(Token.EOS.val);
    }

    static <T>String getValue(T x){
        return (x instanceof Constants)? ((Constants) x).val : (x instanceof Token)? ((Token) x).val : (String) x;
    }

    static <T>String binaryOp(T x, T opr, T y){
        return getValue(x) + getValue(opr) + getValue(y);
    }

    static String unaryOp(Token opr, Constants x){
        return getValue(opr)+ getValue(x);
    }

}
