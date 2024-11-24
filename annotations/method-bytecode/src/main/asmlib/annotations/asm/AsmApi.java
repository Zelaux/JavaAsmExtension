package asmlib.annotations.asm;

import lombok.*;
import org.objectweb.asm.*;

public class AsmApi{
    private MethodVisitor delegate;

    @SneakyThrows
    public AsmApi(){
        throw new IllegalAccessException();
    }

    AsmApi(MethodVisitor delegate){
        this.delegate = delegate;
    }

    public void Insn(int opcode){
        delegate.visitInsn(opcode);
    }

    public void IntInsn(int opcode, int operand){
        delegate.visitIntInsn(opcode, operand);
    }

    public void VarInsn(int opcode, int varIndex){
        delegate.visitVarInsn(opcode, varIndex);
    }

    public void TypeInsn(int opcode, String type){
        delegate.visitTypeInsn(opcode, type);
    }

    public void FieldInsn(int opcode, String owner, String name, String descriptor){
        delegate.visitFieldInsn(opcode, owner, name, descriptor);
    }

    public void MethodInsn(int opcode, String owner, String name, String descriptor){
        delegate.visitMethodInsn(opcode, owner, name, descriptor);
    }

    public void MethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface){
        delegate.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    public void InvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments){
        delegate.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    public void JumpInsn(int opcode, Label label){
        delegate.visitJumpInsn(opcode, label);
    }

    public void Label(Label label){
        delegate.visitLabel(label);
    }

    public void LdcInsn(Object value){
        delegate.visitLdcInsn(value);
    }

    public void IincInsn(int varIndex, int increment){
        delegate.visitIincInsn(varIndex, increment);
    }

    public void TableSwitchInsn(int min, int max, Label dflt, Label... labels){
        delegate.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void LookupSwitchInsn(Label dflt, int[] keys, Label[] labels){
        delegate.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void MultiANewArrayInsn(String descriptor, int numDimensions){
        delegate.visitMultiANewArrayInsn(descriptor, numDimensions);
    }


    public void TryCatchBlock(Label start, Label end, Label handler, String type){
        delegate.visitTryCatchBlock(start, end, handler, type);
    }


    public void LocalVariable(String name, String descriptor, String signature, Label start, Label end, int index){
        delegate.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    public void LineNumber(int line, Label start){
        delegate.visitLineNumber(line, start);
    }

    public void Maxs(int maxStack, int maxLocals){
        delegate.visitMaxs(maxStack, maxLocals);
    }

    public void End(){
        delegate.visitEnd();
    }
}
