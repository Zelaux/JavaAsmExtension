package asmlib.method.lombok;

import asmlib.annotations.*;
import asmlib.method.gen.*;
import asmlib.util.*;
import asmlib.util.visitor.node.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;

class AddMethodByteCodeVisitor extends ClassVisitor{
    public static final String STR_ARGS_PARAM_NAME = "sargs";
    static final int accessMask = ACC_ABSTRACT | ACC_NATIVE;

    public AddMethodByteCodeVisitor(ClassWriter writer){
        super(ASM9, writer);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
        if((access & accessMask) == 0) return super.visitMethod(access, name, descriptor, signature, exceptions);
        return new PostMethodVisitor(api, access, name, descriptor, signature, exceptions)
            .consumers(it -> {
                AnnotationNode rawNode = NodeUtil.find(it.visibleAnnotations, ByteCode.class);
                if(rawNode == null) return;
                AnnotationArgumentMap asmListNode = new AnnotationArgumentMap(rawNode);
                //noinspection unchecked
                List<AnnotationArgumentMap> opcodeList = asmListNode.getList("value");
                InstructionContext context = new InstructionContext();
                for(AnnotationArgumentMap asmItem : opcodeList){
                    int opcode = asmItem.get("value");
                    //noinspection MagicConstant
                    MethodMeta meta = ByteCodeMapping.getMethodMeta(opcode);
//                    String opcodeName = ByteCodeMapping.name(opcode);
                    Object[] args;
                    if(asmItem.has(STR_ARGS_PARAM_NAME)){
                        List<String> o = asmItem.get(STR_ARGS_PARAM_NAME);
                        args = meta.transformStringArgs(o, context);
                    }else if(asmItem.has("args")){
                        //TODO add support for asmlib.annotations.Arg
                        throw null;
                    }else{
                        args = new Object[0];
                    }
                    meta.invoke(it, opcode, args);
                }
                it.visibleAnnotations.remove(rawNode);
                it.access -= it.access & accessMask;
//                    rawNode.values
            })
            .finishVisitor(this.cv);
    }
}
