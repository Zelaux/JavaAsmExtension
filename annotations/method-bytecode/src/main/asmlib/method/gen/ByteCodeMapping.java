package asmlib.method.gen;

import lombok.*;
import org.intellij.lang.annotations.*;
import org.objectweb.asm.*;

import java.lang.reflect.*;

public class ByteCodeMapping{
    private static final MethodMeta[] map = new MethodMeta[256];

    @SneakyThrows
    public static MethodMeta getMethodMeta(@MagicConstant(valuesFromClass = Opcodes.class) int opcode){
        if(map[opcode] == null) map[opcode] = findMethodMeta(opcode);
        return map[opcode];
    }

    private static MethodMeta findMethodMeta(@MagicConstant(valuesFromClass = Opcodes.class) int opcode) throws NoSuchMethodException{
        String methodName = ByteCodeMappingInternal.map(opcode);
        Method foundMethod = null;
        for(Method declaredMethod : MethodVisitor.class.getDeclaredMethods()){
            if(declaredMethod.isAnnotationPresent(Deprecated.class)) continue;
            if(declaredMethod.getName().equals(methodName)){
                foundMethod = declaredMethod;
                break;
            }
        }
        if(foundMethod == null) throw new NoSuchMethodException("Cannot find method for opcode " + opcode + "(" + name(opcode) + ")");

        return new MethodMeta(foundMethod, ByteCodeMappingInternal.hasOpcodeParam(methodName));
    }

    public static String name(int opcode){
        return ByteCodeMappingInternal.name(opcode);
    }

    @SuppressWarnings("unused")
    public static int code(String name){
        return ByteCodeMappingInternal.code(name);
    }

}
    