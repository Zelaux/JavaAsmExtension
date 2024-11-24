package asmlib.util.visitor.node;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PostRecordComponentVisitor extends RecordComponentNode{
    public static final ConsumeRecordComponentNode[] EMPTY_ARRAY = new ConsumeRecordComponentNode[0];
    private ConsumeRecordComponentNode[] consumers = EMPTY_ARRAY;
    private ClassVisitor finishVisitor;

    public PostRecordComponentVisitor(int api, String name, String descriptor, String signature){
        super(api, name, descriptor, signature);
    }

    public PostRecordComponentVisitor consumers(ConsumeRecordComponentNode... consumers){
        this.consumers = consumers;
        return this;
    }

    public PostRecordComponentVisitor finishVisitor(ClassVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        for(ConsumeRecordComponentNode consumer : consumers){
            consumer.consume(this);
        }
        if(finishVisitor != null) this.accept(finishVisitor);
    }

    public interface ConsumeRecordComponentNode{
        void consume(RecordComponentNode classNode);
    }
}
