package asmlib.util.visitor.node;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PostClassVisitor extends ClassNode{
    public static final ConsumeClassNode[] EMPTY_ARRAY = new ConsumeClassNode[0];
    private ConsumeClassNode[] consumers = EMPTY_ARRAY;
    private ClassVisitor finishVisitor;
    @SuppressWarnings("unused")
    public PostClassVisitor(int api){
        super(api);
    }
    @SuppressWarnings("unused")
    public PostClassVisitor consumers(ConsumeClassNode... consumers){
        this.consumers = consumers;
        return this;
    }
    @SuppressWarnings("unused")
    public PostClassVisitor finishVisitor(ClassVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        ClassNode delegate = (ClassNode)getDelegate();
        for(ConsumeClassNode consumer : consumers){
            consumer.consume(delegate);
        }
        if(finishVisitor != null) delegate.accept(finishVisitor);
    }

    public interface ConsumeClassNode{
        void consume(ClassNode classNode);
    }
}
