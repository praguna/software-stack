enum Segment {
    CONST("constant"),
    ARG("argument"),
    LOCAL("local"),
    STATIC("static"),
    THIS("this"),
    THAT("that"),
    TEMP("temp"),
    PTR("pointer");

    private final String alias;
    Segment(String alias) {
        this.alias = alias;
    }

    String get(){
        return alias;
    }

}
