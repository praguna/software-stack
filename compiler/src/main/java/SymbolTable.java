import java.util.HashMap;
import java.util.Objects;

enum Kind {
    STATIC,FIELD,ARG,VAR
}

public class SymbolTable {

    HashMap<String,Details> classScope;
    HashMap<Kind, Integer> kindCount;
    HashMap<String,Details> subroutineScope;

    SymbolTable(){
        classScope = new HashMap<>();
        subroutineScope = new HashMap<>();
        kindCount = new HashMap<>();
    }

    void define(String name, String type, Kind kind){
        int count = varCount(kind);
        if(!isClassScope(kind)){
            subroutineScope.put(name, new Details(kind,type,count));
        }else {
            classScope.put(name,new Details(kind,type,count));
        }
        kindCount.put(kind,++count);
    }

    int varCount(Kind type){
        return kindCount.getOrDefault(type,0);
    }

    Kind kindOf(String name){
        return getDetailByName(name).getKind();
    }

    String typeOf(String name){
        return getDetailByName(name).getDataType();
    }
    int indexOf(String name){
        return getDetailByName(name).getIndex();
    }

    private Details getDetailByName(String name){
        Details detail = subroutineScope.get(name);
        if(Objects.isNull(detail)){
            detail =  classScope.get(name); //assumes code is correct
        }
        return detail;
    }

    private boolean isClassScope(Kind kind){
        return kind.equals(Kind.STATIC) || kind.equals(Kind.FIELD);
    }

    private static class Details{
        private int index;
        private Kind kind;
        private String dataType;
        public Details(Kind varType, String dataType, int num){
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
