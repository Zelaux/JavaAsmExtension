package asmlib.util.visitor.node;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class PostAnnotationVisitor extends AnnotationNode{
    public static final ConsumeAnnotationNode[] EMPTY_ARRAY = new ConsumeAnnotationNode[0];
    @NotNull
    private ConsumeAnnotationNode[] consumers = EMPTY_ARRAY;
    @Nullable
    private AnnotationVisitor finishVisitor;

    public PostAnnotationVisitor(int api, String descriptor){
        super(api, descriptor);
    }

    public PostAnnotationVisitor consumers(ConsumeAnnotationNode... consumers){
        this.consumers = consumers;
        return this;
    }

    public PostAnnotationVisitor finishVisitor(AnnotationVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        for(ConsumeAnnotationNode consumer : consumers){
            consumer.consume(this);
        }
        if(finishVisitor != null) this.accept(finishVisitor);
    }

    public interface ConsumeAnnotationNode{
        void consume(AnnotationNode classNode);
    }
}
