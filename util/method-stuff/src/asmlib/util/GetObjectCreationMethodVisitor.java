package asmlib.util;

import lombok.*;
import lombok.experimental.*;
import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

public class GetObjectCreationMethodVisitor extends MethodVisitor{


    public static final String INIT = "<init>";
    public static boolean throwException = true;
    public final List<ObjectCreationRange> rootRanges = new ArrayList<>();
    public final MethodNode methodNode;
    public final List<ObjectCreationRange> allRanges = new ArrayList<>();
    private List<ObjectCreationRange> ranges = rootRanges;
    @Nullable
    private ObjectCreationRange current = null;

    public GetObjectCreationMethodVisitor(int api){
        super(api);
        this.mv = (methodNode = new MethodNode());
    }

    public GetObjectCreationMethodVisitor(int api, MethodNode infoNode){
        super(api, infoNode);
        methodNode = infoNode;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface){
        int i = curIdx();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        if(opcode == INVOKESPECIAL && name.equals(INIT)){
            if(current == null){
                if(methodNode.name != null && methodNode.name.equals(INIT)) return;//Call super in constructor
                if(throwException) throw new RuntimeException("Has no opened object creation\n" + owner + "." + name + descriptor + "\n");
                return;
            }
            if(!current.type.equals(owner)){
                if(throwException) throw new RuntimeException(String.format("Types '%s' and '%s' did not match", current.type, opcode));
                return;
            }
            current.endIndex = i;
            current = current.parent;
            if(current == null){
                ranges = rootRanges;
            }else{
                ranges = current.innerObjects;
            }
        }
    }

    @Override
    public void visitTypeInsn(int opcode, String type){
        if(opcode == NEW){
            ranges.add(current = new ObjectCreationRange(current, type, curIdx()));
            allRanges.add(current);
            ranges = current.innerObjects;
        }
        super.visitTypeInsn(opcode, type);
    }

    private int curIdx(){
        return methodNode.instructions.size();
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC)
    public static class ObjectCreationRange{
        @Nullable
        final ObjectCreationRange parent;
        @NotNull
        final String type;
        @NotNull
        final int startIndex;
        @NotNull
        List<ObjectCreationRange> innerObjects = new ArrayList<>();
        int endIndex = -1;

    }
}
