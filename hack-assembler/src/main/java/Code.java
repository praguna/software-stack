import java.util.Map;

class Code {
    private Map<String,String> destMap;
    private Map<String,String> jumpMap;
    private Map<String,String> compMap;
    private int nextRamAddr;

    Code(){
        destMap = Map.ofEntries(
                Map.entry("M","001"),
                Map.entry("D","010"),
                Map.entry("MD","011"),
                Map.entry("A","100"),
                Map.entry("AM","101"),
                Map.entry("AD","110"),
                Map.entry("AMD","111")
        );
        jumpMap = Map.ofEntries(
                Map.entry("JGT","001"),
                Map.entry("JEQ","010"),
                Map.entry("JGE","011"),
                Map.entry("JLT","100"),
                Map.entry("JNE","101"),
                Map.entry("JLE","110"),
                Map.entry("JMP","111")
        );
        compMap = Map.ofEntries(
                Map.entry("0","101010"),
                Map.entry("1","111111"),
                Map.entry("-1","111010"),
                Map.entry("D","001100"),
                Map.entry("X","110000"),
                Map.entry("!D","001101"),
                Map.entry("!X","110001"),
                Map.entry("-D","001111"),
                Map.entry("-X","110011"),
                Map.entry("D+1","011111"),
                Map.entry("X+1","110111"),
                Map.entry("D-1","001110"),
                Map.entry("X-1","110010"),
                Map.entry("D+X","000010"),
                Map.entry("D-X","010011"),
                Map.entry("X-D","000111"),
                Map.entry("D&X","000000"),
                Map.entry("D|X","010101")
        );
        nextRamAddr=16;
    }
    //TODO:Remove In between white spaces
    private String dest(String mnemonic){
        if(mnemonic==null) return "0".repeat(3);
        return destMap.getOrDefault(mnemonic,"");
    }

    private String comp(String mnemonic){
        if(mnemonic==null) return "0".repeat(7);
        mnemonic = mnemonic.replace("M","X").replace("A","X");
        return compMap.getOrDefault(mnemonic,"");
    }

    private String jump(String mnemonic){
        if(mnemonic==null) return "0".repeat(3);
        return jumpMap.getOrDefault(mnemonic,"");
    }

    String address(String decimal,SymbolTable symbolTable){
       int int_addr = computeAddress(decimal,symbolTable);
       StringBuilder sb = new StringBuilder();
       while(int_addr > 0){
           sb.append(int_addr % 2);
           int_addr>>=1;
       }
       while(sb.length() < 16) {
           sb.append('0');
       }
       return sb.reverse().toString();
    }

    String compAssemble(String dest, String comp, String jump){
        return compPrefix(comp)+comp(comp)+dest(dest)+jump(jump);
    }

    private int computeAddress(String decimal, SymbolTable symbolTable){
        try{
            return Integer.parseInt(decimal);
        }
        catch (NumberFormatException e){
            if(symbolTable.contains(decimal)) {
               return Integer.parseInt(symbolTable.getAddress(decimal));
            }
            symbolTable.addEntry(decimal,nextRamAddr);
            ++nextRamAddr;
            return nextRamAddr-1;
        }
    }

    private String compPrefix(String comp){
        StringBuilder sb = new StringBuilder("111");
        int r = comp.indexOf("M");
        if(r==-1) sb.append('0');
        else sb.append('1');
        return sb.toString();
    }
}
