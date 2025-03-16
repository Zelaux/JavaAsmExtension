package asmlib.lombok;

import asmlib.annotations.initializein.AllowInstanceInitializationOfStaticFields;
import asmlib.annotations.initializein.InitializeIn;
import asmlib.util.AnnotationArgumentMap;
import asmlib.util.ClassFileMetaData;
import asmlib.util.NodeUtil;
import lombok.*;
import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompilerTransformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class MoveInitializerPostCompilation implements PostCompilerTransformation {
    private static AnnotationArgumentMap findAnnotation(List<AnnotationNode> visibleAnnotations, List<AnnotationNode> invisibleAnnotations) {
        return findAnnotation(visibleAnnotations, invisibleAnnotations, InitializeIn.class);
    }

    private static @NotNull AnnotationArgumentMap findAnnotation(List<AnnotationNode> visibleAnnotations, List<AnnotationNode> invisibleAnnotations, Class<? extends Annotation> type) {
        AnnotationNode annotation = NodeUtil.findAnnotation(visibleAnnotations, type);
        if (annotation == null) {
            annotation = NodeUtil.findAnnotation(invisibleAnnotations, type);
            if (annotation != null) {
               invisibleAnnotations.remove(annotation);
            }
        } else {
            visibleAnnotations.remove(annotation);
        }

        return new AnnotationArgumentMap(annotation == null ? null : Objects.requireNonNullElse(annotation.values,new ArrayList<>()));
    }

    private static @Nullable InitializeIn makeAnnotation(Map<String, Object> map) {
        if (map == null) return null;
        return makeAnnotation0(map);
    }

    private static @NotNull InitializeIn makeAnnotation0(Map<String, Object> map1) {
        return (InitializeIn) Proxy.newProxyInstance(MyInvocationHandler.class.getClassLoader(), new Class[]{InitializeIn.class}, new MyInvocationHandler(map1));
    }

    private static InsnListSupplier instructionProducer(@NonNull MethodNode methodNode) {
        return new InsnListSupplier(methodNode);
    }

    private static boolean tryResolve(InitializeIn.Position pos, InsnList targetInsn, String targetMethodFullDescriptor, InsnListSupplier sourceInsn) {
        int[] totalLabels = {calcMaxLabel(targetInsn).size()};
        return switch (pos) {
            case AfterSuperOrHead,
                 AfterSuperOrTail,
                 BeforeSuperOrHead,
                 BeforeSuperOrTail -> {
                AbstractInsnNode[] array = targetInsn.toArray();
                boolean hasAny = false;
                for (AbstractInsnNode node : array) {
                    if (!(node instanceof MethodInsnNode methodInsnNode)) continue;
                    if (node.getOpcode() != Opcodes.INVOKESPECIAL) continue;
                    if (!targetMethodFullDescriptor.equals(methodInsnNode.name + methodInsnNode.desc)) continue;
                    hasAny = true;
                    if (pos.before) {
                        targetInsn.insertBefore(node, sourceInsn.get(totalLabels));
                    } else {
                        targetInsn.insert(node, sourceInsn.get(totalLabels));
                    }
                }
                if (!hasAny) yield tryResolve(
                        toHeadOrReturn(pos), targetInsn, targetMethodFullDescriptor, sourceInsn);
                yield true;
            }
            case Head -> {

                targetInsn.insertBefore(targetInsn.getFirst(), sourceInsn.get(totalLabels));
                yield true;
            }
            case BeforeReturn -> {

                AbstractInsnNode[] array = targetInsn.toArray();

                for (AbstractInsnNode node : array) {
                    int opcode = node.getOpcode();
                    if (opcode < Opcodes.IRETURN || opcode > Opcodes.RETURN) continue;
                    targetInsn.insertBefore(node, sourceInsn.get(totalLabels));
                }
                yield true;
            }
        };
    }

    private static HashSet<LabelNode> calcMaxLabel(InsnList methodNode) {
        return calcMaxLabel(methodNode.toArray());
    }

    private static @NotNull HashSet<LabelNode> calcMaxLabel(AbstractInsnNode[] array) {
        HashSet<LabelNode> set = new HashSet<>();
        AbstractMap<LabelNode, LabelNode> clonedLabels = new AbstractMap<LabelNode, LabelNode>() {
            @Override
            public @NotNull Set<Entry<LabelNode, LabelNode>> entrySet() {
                return Set.of();
            }

            @Override
            public LabelNode get(Object key) {
                set.add((LabelNode) key);
                return (LabelNode) key;
            }
        };
        for (AbstractInsnNode node : array) {
            node.clone(clonedLabels);
        }
        return set;
    }

    private static InitializeIn.@NotNull Position toHeadOrReturn(InitializeIn.Position pos) {
        return pos.head ? InitializeIn.Position.Head : InitializeIn.Position.BeforeReturn;
    }

    @Override
    public byte[] applyTransformations(byte[] bytes, String s, DiagnosticsReceiver diagnosticsReceiver) {
        ClassFileMetaData metaData = new ClassFileMetaData(bytes);
        if (!metaData.usesAnnotation(InitializeIn.class)) return bytes;
        ClassNode classNode = new ClassNode();
        new ClassReader(bytes).accept(classNode, 0);

        AnnotationArgumentMap classAnnoMap = findAnnotation(
                classNode.visibleAnnotations,
                classNode.invisibleAnnotations
        );
        findAnnotation(
                classNode.visibleAnnotations,
                classNode.invisibleAnnotations,
                AllowInstanceInitializationOfStaticFields.class
        );
        @Nullable InitializeIn classAnno = makeAnnotation(classAnnoMap.copyMap());

        HashMap<String, MethodNode> fieldToMethodMap = new HashMap<>();
        HashMap<String, MethodNode> descriptorToMethod = new HashMap<>();
        List<MethodNode> methods = classNode.methods;
        for (int i = 0; i < methods.size(); i++) {
            val method = methods.get(i);
            String name = method.name;
            if (!name.startsWith(MoveInitializerProc.FIELD_INTI_$)) {
                descriptorToMethod.put(method.name + method.desc, method);
                continue;
            }
            String fieldName = name.substring(MoveInitializerProc.FIELD_INTI_$.length());
            fieldToMethodMap.put(fieldName, method);
            methods.remove(i);
            i--;
//            method.access|=Opcodes.ACC_SYNTHETIC;
        }

        for (val field : classNode.fields) {
            val localMap = findAnnotation(field.visibleAnnotations, field.invisibleAnnotations);
            findAnnotation(field, AllowInstanceInitializationOfStaticFields.class);
            if (!findAnnotation(field, InitializeIn.Exclude.class).isNull()) {
                continue;
            }
            if (localMap.isNull() && classAnno == null) continue;
            val initializer = localMap.isNull() ? classAnno : makeAnnotation(localMap.copyMap());
            String targetMethodFullDescriptor = initializer.value();
            MethodNode targetMethod = descriptorToMethod.get(targetMethodFullDescriptor);
            MethodNode methodNode = fieldToMethodMap.get(field.name);
            if(methodNode==null)throw new RuntimeException(
                    String.format(
                            "Cannot find init for %s.%s",
                            classNode.name,
                            field.name
                    )
            );
            InsnListSupplier sourceInsn = instructionProducer(methodNode);
            InsnList targetInsn = targetMethod.instructions;
            InitializeIn.Position pos = initializer.pos();
            if ((targetMethod.access & Opcodes.ACC_STATIC) != 0 && pos.relatedSuper)
                pos = toHeadOrReturn(pos);

            boolean resolved = tryResolve(pos, targetInsn, targetMethodFullDescriptor, sourceInsn);
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static @NotNull AnnotationArgumentMap findAnnotation(FieldNode field, Class<? extends Annotation> type) {
        return findAnnotation(field.visibleAnnotations, field.invisibleAnnotations, type);
    }

    @AllArgsConstructor
    private static class MyInvocationHandler implements InvocationHandler {
        private final Map<String, Object> map;


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object defaultValue = method.getDefaultValue();
            Object orDefault = map.getOrDefault(method.getName(), defaultValue);
            if (method.getReturnType().isEnum() && orDefault instanceof String[] strings) {
                Field field = method.getReturnType().getDeclaredField(strings[1]);
                field.setAccessible(true);
                return field.get(null);
            }
            return orDefault;
        }
    }

    private static class InsnListSupplier {
        private static final Field apiField;

        static {
            try {
                apiField = MethodVisitor.class.getDeclaredField("api");
                apiField.setAccessible(true);
            } catch (Throwable e) {
                throw Lombok.sneakyThrow(e);
            }
        }

        private final AbstractInsnNode[] array;
        private final Set<LabelNode> labels;


        @SneakyThrows
        public InsnListSupplier(MethodNode methodNode) {
            InsnList list = methodNode.instructions;

            AbstractInsnNode[] array = list.toArray();
            int size = 0;
            for (size = 0; size < array.length; size++) {
                if (array[size].getOpcode() == Opcodes.RETURN) {
                    break;
                }
            }
            AbstractInsnNode[] newArr = new AbstractInsnNode[size];
            System.arraycopy(array, 0, newArr, 0, size);
            this.array = newArr;
            labels = calcMaxLabel(newArr);
        }

        public InsnList get(int[] totalLabels) {
            int offset = totalLabels[0];
            totalLabels[0] += labels.size();
            InsnList nodes = new InsnList();
            HashMap<LabelNode, LabelNode> map = new HashMap<>();
            for (LabelNode label : labels) {
                Label label1 = new Label();

                LabelNode info = new LabelNode(label1);
                label1.info = info;
                map.put(label, info);
            }
            for (AbstractInsnNode node : array) {
                nodes.add(node.clone(map));
            }
            return nodes;
        }
    }
}
