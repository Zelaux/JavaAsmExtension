package asmlib.util;

import asmlib.dev.annotations.AsmVersion;
import lombok.SneakyThrows;
import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

public class NodeUtil {
    public static AnnotationNode findAnnotation(@Nullable List<AnnotationNode> list, Class<? extends Annotation> annotationClass) {
        return findAnnotation(list, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode findAnnotation(@Nullable List<AnnotationNode> list, String descriptor) {
        if (list == null) return null;
        for (AnnotationNode node : list) {
            if (node.desc.equals(descriptor)) return node;
        }
        return null;
    }

    @NotNull
    public static ClassNode classNode(Class<?> clazz, @AsmVersion int api) throws IOException {
        ClassReader cr = new ClassReader(clazz.getName());
        return classNode(cr, api);
    }
    @NotNull
    public static ClassNode classNode(byte[] bytes, @AsmVersion int api) {
        ClassReader cr = new ClassReader(bytes);
        return classNode(cr, api);
    }

    @NotNull
    public static ClassNode classNode(ClassReader cr, int api) {
        ClassNode rootClass = new ClassNode(api);
        cr.accept(rootClass, 0);
        return rootClass;
    }

    @SneakyThrows
    public static MethodNode methodNode(Method method, @AsmVersion int api) {
        return extractMethod(classNode(method.getDeclaringClass(), api), method.getName(), Type.getMethodDescriptor(method));
    }

    public static MethodNode extractMethod(ClassNode rootClass, String methodName, String methodDesc) throws NoSuchMethodException {
        return rootClass.methods
                .stream()
                .filter(it -> it.name.equals(methodName) && it.desc.equals(methodDesc))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException(methodName + methodDesc));
    }
}
