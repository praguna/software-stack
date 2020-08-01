enum Command {
    ADD,
    SUB,
    NEG,
    AND,
    OR,
    NOT,
    LT,
    GT,
    EQ;

    String get(){
        return this.name().toLowerCase();
    }
}
