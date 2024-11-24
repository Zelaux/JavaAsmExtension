package asmlib.util.visitor.node;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PostMethodVisitor extends MethodNode{
    public static final ConsumeMethodNode[] EMPTY_ARRAY = new ConsumeMethodNode[0];
    @NotNull
    private ConsumeMethodNode[] consumers = EMPTY_ARRAY;
    @Nullable
    private ClassVisitor finishVisitor;

    public PostMethodVisitor(int api, int access, String name, String descriptor, String signature, String[] exceptions){
        super(api, access, name, descriptor, signature, exceptions);
    }

    public PostMethodVisitor consumers(ConsumeMethodNode... consumers){
        this.consumers = consumers;
        return this;
    }

    public PostMethodVisitor finishVisitor(ClassVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        for(ConsumeMethodNode consumer : consumers){
            consumer.consume(this);
        }
        if(finishVisitor != null) this.accept(finishVisitor);
    }

    public interface ConsumeMethodNode{
        void consume(MethodNode classNode);
    }
}
