package asmlib.util;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

@SuppressWarnings("unused")
public class CompilerUtil{

    public static void swap2(MethodVisitor visitor){
        visitor.visitInsn(DUP2_X2);//to*p, p,to*p
        visitor.visitInsn(POP2);//p, to*p
    }


    @NotNull
    @SuppressWarnings("unused")
    public static String className(Class<?> functionTypeClass){
        return Type.getInternalName(functionTypeClass);
    }

    public static byte[] bytes(ClassNode classNode){
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    public static String descriptor(Class<?> clazz){
        return Type.getDescriptor(clazz);
    }

    @SuppressWarnings("unused")
    public static String descriptor(Class<?>... classes){

        String[] names = new String[classes.length];
        for(int i = 0; i < classes.length; i++){
            names[i] = descriptor(classes[i]);
        }
        return String.join("", names);
    }

    public static Type methodType(Class<?> returnType, Class<?>... args){
        Type type = Type.getType(returnType);
        Type[] paramTypes = new Type[args.length];
        for(int i = 0; i < args.length; i++){
            paramTypes[i] = Type.getType(args[i]);
        }

        return Type.getMethodType(type, paramTypes);
    }

    public static String methodTypeDesc(Class<?> returnType, Class<?>... args){
        return methodType(returnType, args).getDescriptor();
    }
}
