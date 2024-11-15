package asmlib.util;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

public class ConstantInsn{
    public static void visitInt(MethodVisitor visitor, int idx){
        if(idx == -1){
            visitor.visitInsn(ICONST_M1);
        }else if(idx <= 5){
            visitor.visitInsn(ICONST_0 + idx);
        }else{
            visitor.visitIntInsn(BIPUSH, idx);
        }
    }

    public static void visitLong(MethodVisitor visitor, long value){
        if(value == 0){
            visitor.visitInsn(LCONST_0);
        }else if(value == 1){
            visitor.visitInsn(LCONST_1);
        }else{
            visitor.visitLdcInsn(value);
        }
    }

    public static void visitDouble(MethodVisitor visitor, double v){
        if(v == 0){
            visitor.visitInsn(DCONST_0);
        }else if(v == 1){
            visitor.visitInsn(DCONST_1);
        }else{
            visitor.visitLdcInsn(v);
        }
    }

    public static AbstractInsnNode intNode(int value){
        if(value == -1) return new InsnNode(ICONST_M1);
        if(value <= 5) return new InsnNode(ICONST_0 + value);
        return new IntInsnNode(BIPUSH, value);
    }

}
