package asmlib.util.visitor.node;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

public class PostModuleVisitor extends ModuleNode{
    public static final ConsumeModuleNode[] EMPTY_ARRAY = new ConsumeModuleNode[0];
    @NotNull
    private ConsumeModuleNode[] consumers = EMPTY_ARRAY;
    @Nullable
    private ClassVisitor finishVisitor;

    public PostModuleVisitor(int api, String name, int access, String version){
        this(api, name, access, version, null, null, null, null, null);
    }

    public PostModuleVisitor(int api, String name, int access, String version, List<ModuleRequireNode> requires, List<ModuleExportNode> exports, List<ModuleOpenNode> opens, List<String> uses, List<ModuleProvideNode> provides){
        super(api, name, access, version, requires, exports, opens, uses, provides);
    }

    public PostModuleVisitor consumers(ConsumeModuleNode... consumers){
        this.consumers = consumers;
        return this;
    }

    public PostModuleVisitor finishVisitor(ClassVisitor finishVisitor){
        this.finishVisitor = finishVisitor;
        return this;
    }

    @Override
    public void visitEnd(){
        super.visitEnd();
        for(ConsumeModuleNode consumer : consumers){
            consumer.consume(this);
        }
        if(finishVisitor != null) this.accept(finishVisitor);
    }

    public interface ConsumeModuleNode{
        void consume(ModuleNode classNode);
    }
}
