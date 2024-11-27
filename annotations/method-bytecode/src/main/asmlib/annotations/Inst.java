package asmlib.annotations;

import org.intellij.lang.annotations.*;
import org.objectweb.asm.*;

@SuppressWarnings("unused")
public @interface Inst{
    @MagicConstant(valuesFromClass = Opcodes.class)
    int value();

    Arg[] args() default {};

    /** Args in string format */
    String[] sargs() default {};
}
