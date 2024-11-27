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
import java.util.function.Supplier;

public class NodeUtil {
    public static AnnotationNode find(@Nullable List<AnnotationNode> list, Class<? extends Annotation> annotationClass) {
        return find(list, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode find(@Nullable List<AnnotationNode> list, String descriptor) {
        if (list == null) return null;
        for (AnnotationNode node : list) {
            if (node.desc.equals(descriptor)) return node;
        }
        return null;
    }

    @NotNull
    public static ClassNode classNode(Class<?> clazz, @AsmVersion int api) throws IOException {
        ClassReader cr = new ClassReader(clazz.getName());
        ClassNode rootClass = new ClassNode(api);
        cr.accept(rootClass, 0);
        return rootClass;
    }

    @SneakyThrows
    public static MethodNode methodNode(Method method, @AsmVersion int api) throws IOException {
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
