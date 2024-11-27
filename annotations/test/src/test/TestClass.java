package test;

import asmlib.annotations.*;

import static org.objectweb.asm.Opcodes.*;

//@Getter
public class TestClass{
    //    @Setter
    @SuppressWarnings("unused")
    String value;

    public static void main(String[] args){
        System.out.println("Hello world");
    }

    @ByteCode({
        @Inst(value = DLOAD, sargs = "0"),
        @Inst(DCONST_0),
        @Inst(DCMPG),
        @Inst(IRETURN),
    })
    public static native int sign(double value);

    @ByteCode({
        @Inst(value = DLOAD, sargs = "1"),
        @Inst(value = INVOKESTATIC, sargs = {"test.TestClass", "sign", "(D)I", "false"}),
        @Inst(I2D),
        @Inst(DRETURN),
    })
    public native double sign2(double it);
}
