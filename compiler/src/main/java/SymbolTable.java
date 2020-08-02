import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

enum Kind {
    STATIC,FIELD,ARGUMENT,VAR
}

public class SymbolTable {

    // Scope Tables
    private final HashMap<String, VarDetails> classScope;
    private final HashMap<Kind, Integer> kindCount;
    private final HashMap<String, VarDetails> subroutineScope;
    private final boolean shouldPrint;


    SymbolTable(){
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        kindCount = new HashMap<>();
        shouldPrint = false;
    }

    void resetSubroutine(String name){
        if(shouldPrint) {
            System.out.printf("****************** SUBROUTINE SCOPE %s *******************\n\n", name);
            printEntries(subroutineScope);
        }
        subroutineScope.clear();
        kindCount.clear();
    }

    void reset(){
        subroutineScope.clear();
        classScope.clear();
        kindCount.clear();
    }

    // add entry to symbol table
    void define(String name, String type, Kind kind){
        int count = varCount(kind);
        if(!isClassScope(kind)){
            subroutineScope.put(name, new VarDetails(kind,type,count));
        }else {
            classScope.put(name,new VarDetails(kind,type,count));
        }
        kindCount.put(kind,++count);
    }

    // maintain index of a type
    int varCount(Kind type){
        return kindCount.getOrDefault(type,0);
    }

    // retrieve values
    Kind kindOf(String name){
        VarDetails detail = getDetailByName(name);
        if(Objects.isNull(detail)) return null;
        return getDetailByName(name).getKind();
    }

    String typeOf(String name){
        return getDetailByName(name).getDataType();
    }
    int indexOf(String name){
        return getDetailByName(name).getIndex();
    }

    // search in the table for the variable
    private VarDetails getDetailByName(String name){
        VarDetails detail = subroutineScope.get(name);
        if(Objects.isNull(detail)){
            detail =  classScope.get(name); //assumes code is correct
        }
        return detail;
    }

    // determine if class scope
    private boolean isClassScope(Kind kind){
        return kind.equals(Kind.STATIC) || kind.equals(Kind.FIELD);
    }

    // print the table
    void printClassScope(String className){
        if(!shouldPrint) return;
        System.out.printf("******************* CLASS SCOPE %s **************************\n\n",className);
        printEntries(classScope);
    }

    private void printEntries(HashMap<String, VarDetails> scope){
        System.out.println("------------------------------------");
        System.out.print("| Name | => | Type , Kind , Index | \n");
        System.out.println("------------------------------------");
        if(scope.isEmpty()) System.out.println(" THIS PORTION IS EMPTY ");
        for (Map.Entry<String, VarDetails> entry : scope.entrySet()) {
            String key = entry.getKey();
            VarDetails value = entry.getValue();
            System.out.printf("| %s | => | %s , %s ,%s | \n", key, value.getDataType(), value.getKind().name().toLowerCase(), value.getIndex());
        }
        System.out.println("------------------------------------");
    }


    // model class to store entry details for a variable
    private static class VarDetails {
        private final int index;
        private final Kind kind;
        private final String dataType;
        public VarDetails(Kind varType, String dataType, int num){
            this.dataType = dataType;
            this.kind = varType;
            this.index = num;
        }
        public Kind getKind(){
            return kind;
        }
        public String getDataType(){
            return dataType;
        }
        public int getIndex(){
            return index;
        }
    }
}
