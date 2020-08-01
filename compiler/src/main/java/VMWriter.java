import java.io.PrintWriter;

public class VMWriter {

    private PrintWriter writer;

    VMWriter(PrintWriter writer){
        this.writer = writer;
    }

    void writePush(Segment segment, int index){
        this.writer.printf("push %s %d\n",segment.get(),index);
    }

    void writePop(Segment segment, int index){
        this.writer.printf("pop %s %d\n",segment.get(), index);
    }

    void writeArithmetic(Command command){
        this.writer.println(command.get());
    }

    void writeLabel(String label){
        this.writer.printf("label %s\n",label);
    }

    void writeGoto(String label){
        this.writer.printf("goto %s\n",label);
    }

    void writeIf(String label){
        this.writer.printf("if-goto %s\n",label);
    }

    void writeCall(String name, int nArgs){
        this.writer.printf("call %s %d",name,nArgs);
    }

    void writeFunction(String name, int nLocals){
        this.writer.printf("function %s %d",name,nLocals);
    }

    void writeReturn(){
        this.writer.println("return");
    }

    void close(){
        writer.close();
    }
}
