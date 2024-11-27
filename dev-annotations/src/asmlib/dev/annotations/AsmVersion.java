package asmlib.dev.annotations;

import org.intellij.lang.annotations.MagicConstant;
import org.objectweb.asm.Opcodes;

@MagicConstant(intValues = {
        Opcodes.ASM4,
        Opcodes.ASM5,
        Opcodes.ASM6,
        Opcodes.ASM7,
        Opcodes.ASM8,
        Opcodes.ASM9,
})
public @interface AsmVersion {
}
