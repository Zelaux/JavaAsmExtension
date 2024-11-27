package asmlib.util.visitor.node;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class PostTypeAnnotationVisitor extends TypeAnnotationNode{
    public static final ConsumeTypeAnnotationNode[] EMPTY_ARRAY = new ConsumeTypeAnnotationNode[0];
    @NotNull
    private ConsumeTypeAnnotationNode[] consumers = EMPTY_ARRAY;
    @Nullable
    private AnnotationVisitor finishVisitor;

    public PostTypeAnnotationVisitor(int api, int typeRef, TypePath typePath, String descriptor){
        super(api, typeRef, typePath, descriptor);
    }

    public PostTypeAnnotationVisitor consumers(ConsumeTypeAnnotationNode... consumers){
        this.consumers = consumers;
        return this;
    }

    public PostTypeAnnotationVisitor finishVisitor(AnnotationVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        for(ConsumeTypeAnnotationNode consumer : consumers){
            consumer.consume(this);
        }
        if(finishVisitor != null) this.accept(finishVisitor);
    }

    public interface ConsumeTypeAnnotationNode{
        void consume(TypeAnnotationNode classNode);
    }
}
