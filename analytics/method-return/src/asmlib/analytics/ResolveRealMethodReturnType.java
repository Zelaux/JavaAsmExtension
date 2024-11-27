package asmlib.analytics;

import asmlib.dev.annotations.AsmVersion;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

public class ResolveRealMethodReturnType {

    public static final String[] EMPTY = new String[0];
    public static final String JAVA_LANG_OBJECT = "java.lang.Object";


    public static TypeAndSubtypes resolveDescriptor(@AsmVersion int api, @Nullable String owner, MethodNode node) throws AnalyzerException {
        Type returnType = Type.getReturnType(node.desc);
        if (returnType.getSort() <= Type.ARRAY) return new TypeAndSubtypes(returnType.getDescriptor(), EMPTY);
        BasicValue[] union = {null};

        new Analyzer<>(new AbstractSimpleInterpreter(api) {
            @Override
            public void returnOperation(AbstractInsnNode insn, BasicValue value, BasicValue expected) throws AnalyzerException {
                if (union[0] == null) {
                    union[0] = value;
                } else if (!union[0].equals(value)) {
                    union[0] = UnionBasicValue.merge(union[0], value);
                }
                super.returnOperation(insn, value, expected);
            }
        }).analyze(owner == null ? JAVA_LANG_OBJECT : owner, node);

        String[] possibleReturnValues = UnionBasicValue.unwrap(union[0])
                .map(Object::toString)
                .toArray(String[]::new);

        return new TypeAndSubtypes(returnType.getDescriptor(), possibleReturnValues);
    }

    public static Class<?> resolveClass(@AsmVersion int api, @Nullable ClassLoader loader, @Nullable Class<?> owner, MethodNode node) throws ClassNotFoundException, AnalyzerException {
        Type type = owner == null ? null : Type.getType(owner);
        Type superType = owner == null ? null : Type.getType(owner.getSuperclass());
        boolean isInterface = owner != null && owner.isInterface();
        BasicValue[] returnType = {null};
        new Analyzer<>(new SimpleVerifier(api, type, superType, null, isInterface) {
            @Override
            public void returnOperation(AbstractInsnNode insn, BasicValue value, BasicValue expected) throws AnalyzerException {
                if (returnType[0] == null) {
                    returnType[0] = value;
                } else {
                    returnType[0] = merge(returnType[0], value);
                }
                super.returnOperation(insn, value, expected);
            }
        }).analyze(owner == null ? JAVA_LANG_OBJECT : owner.getName(), node);
        String className = returnType[0].getType().getClassName();
        if (loader != null) return Class.forName(className, false, loader);
        return Class.forName(className);
    }

}
