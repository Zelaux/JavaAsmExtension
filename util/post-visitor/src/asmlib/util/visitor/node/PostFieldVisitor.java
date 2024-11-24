package asmlib.util.visitor.node;

import asmlib.util.visitor.node.PostMethodVisitor.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PostFieldVisitor extends FieldNode{
    public static final ConsumeFieldNode[] EMPTY_ARRAY = new ConsumeFieldNode[0];
    private ConsumeFieldNode[] consumers = EMPTY_ARRAY;
    private ClassVisitor finishVisitor;

    public PostFieldVisitor(int api, int access, String name, String descriptor, String signature, Object value){
        super(api, access, name, descriptor, signature, value);
    }

    public PostFieldVisitor consumers(ConsumeFieldNode... consumers){
        this.consumers = consumers;
        return this;
    }

    public PostFieldVisitor finishVisitor(ClassVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        for(ConsumeFieldNode consumer : consumers){
            consumer.consume(this);
        }
        if(finishVisitor != null) this.accept(finishVisitor);
    }

    public interface ConsumeFieldNode{
        void consume(FieldNode classNode);
    }
}
