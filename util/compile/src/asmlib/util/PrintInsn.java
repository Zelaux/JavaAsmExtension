package asmlib.util;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unused")
public class PrintInsn{
    public static void visitPrintDouble(MethodVisitor visitor){
        visitor.visitInsn(DUP2);
        visitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        //out,double,...
        //out,double_part1,double_part2,...
        visitor.visitInsn(DUP_X2);//out,double,out
        visitor.visitInsn(POP);
        visitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(D)V", false);
    }

    /**
     * Adds instructions to print int on stack
     */
    public static void visitPrintInt(MethodVisitor visitor){

        visitor.visitInsn(DUP);
        visitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        visitor.visitInsn(SWAP);//out,int
        visitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    }
}
