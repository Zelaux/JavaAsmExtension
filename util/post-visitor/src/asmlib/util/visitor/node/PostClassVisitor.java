package asmlib.util.visitor.node;

import asmlib.util.visitor.node.PostFieldVisitor.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PostClassVisitor extends ClassNode{
    public static final ConsumeClassNode[] EMPTY_ARRAY = new ConsumeClassNode[0];
    private ConsumeClassNode[] consumers = EMPTY_ARRAY;
    private ClassVisitor finishVisitor;

    public PostClassVisitor(int api){
        super(api);
    }

    public PostClassVisitor consumers(ConsumeClassNode... consumers){
        this.consumers = consumers;
        return this;
    }

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
