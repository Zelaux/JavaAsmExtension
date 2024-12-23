package asmlib.lombok;

import asmlib.annotations.Packable;
import asmlib.lombok.javaparser.CompileBodyVisitor;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.core.LombokImmutableList;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import org.intellij.lang.annotations.MagicConstant;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@HandlerPriority(1_000)
public class HandlerPackable extends JavacAnnotationHandler<Packable> {

    public static final String DUPLICATED_PACK_METHOD = "Duplicated @Pack method";
    public static final String DUPLICATED_UNPACK_METHOD = "Duplicated @Unpack method";
    public static final String UNPACK_METHOD_IS_NON_STATIC = "@Unpack method is non-static";
    public static final String PACK_METHOD_MUST_BE_NON_STATIC = "@Pack method must be non-static";

    private static JCTree.JCMethodDecl methodDecl(JavacNode unpackMethod) {
        return (JCTree.JCMethodDecl) unpackMethod.get();
    }

    private static void processUnpack(@MagicConstant(intValues = {-1, 0, 1}) byte hasEmptyConstructor, JavacNode typeNode, JavacNode unpackMethod, List<SizedField> fields) {
        if (hasEmptyConstructor == -1) {
            typeNode.addError("Has no empty constructor for unpack");
            return;
        }
        JCTree.JCMethodDecl decl = methodDecl(unpackMethod);
        int paramsAmount = decl.getParameters().size();
        JCTree.JCVariableDecl variableDecl = paramsAmount == 0 ? null : decl.getParameters().get(0);
        if (paramsAmount != 1 || !(variableDecl.getType() instanceof JCTree.JCPrimitiveTypeTree)) {
            unpackMethod.addError("Expected one primitive type param");
            return;
        }
        FieldType fieldType = FieldType.forType((JCTree.JCPrimitiveTypeTree) variableDecl.getType());
        if (fieldType == null || fieldType.ordinal() > FieldType.Long.ordinal()) {
            unpackMethod.addError("Supported only byte, short, int, long types");
            return;
        }
        String paramName = variableDecl.getName().toString();

        decl.getModifiers().flags -= decl.getModifiers().flags & (Flags.ABSTRACT | Flags.NATIVE);
        CompileBodyVisitor visitor = new CompileBodyVisitor(typeNode.getTreeMaker(), typeNode.getAst(), typeNode.getContext());
        BlockStmt blockStmt = new BlockStmt();
        String selfName = typeNode.getName();
        blockStmt.addStatement(String.format("%1$s $tmp$ = new %1$s();", selfName));
        String typePostfix = fieldType == FieldType.Long ? "L" : "";
        for (SizedField field : fields) {
            String extractedValue = String.format("(%s >> %d%s) & %s", paramName, field.offset(), typePostfix, field.mask());
            blockStmt.addStatement("$tmp$." + field.name + " = " + String.format(field.type.mapperString, extractedValue)+";");
        }
        blockStmt.addStatement("return $tmp$;");
        decl.body = (JCTree.JCBlock) blockStmt
                .accept(visitor, null);
    }

    @Override
    public void handle(AnnotationValues<Packable> annotationValues, JCTree.JCAnnotation annotation, JavacNode annotationNode) {
        JavacNode typeNode = annotationNode.up();
        LombokImmutableList<JavacNode> down = typeNode.down();
        List<SizedField> fields = new ArrayList<>();
        @MagicConstant(flagsFromClass = ErrorTypes.class)
        int errorTypes = 0;

        JavacNode packMethod = null;
        JavacNode unpackMethod = null;
        @MagicConstant(intValues = {-1, 0, 1})
        byte hasEmptyConstructor = 0;
        for (JavacNode node : down) {
            AST.Kind kind = node.getKind();
            JCTree jcTree = node.get();
            if (kind != AST.Kind.FIELD) {
                if (kind != AST.Kind.METHOD) continue;
                if ("<init>".equals(node.getName())) {
                    if (node.countMethodParameters() == 0) {
                        hasEmptyConstructor = 1;
                    } else if (hasEmptyConstructor == 0) {
                        hasEmptyConstructor = -1;
                    }
                }
                JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) jcTree;

                boolean isStatic = (methodDecl.getModifiers().flags & Flags.STATIC) != 0;
                boolean isUnpack = node.hasAnnotation(Packable.Unpack.class);
                boolean isPack = node.hasAnnotation(Packable.Pack.class);
                if (isStatic && isPack) {
                    node.addError(PACK_METHOD_MUST_BE_NON_STATIC);
                    errorTypes |= ErrorTypes.staticPack;
                } else if (!isStatic && isUnpack) {
                    node.addWarning(UNPACK_METHOD_IS_NON_STATIC);
                }
                if (isUnpack) {
                    if (unpackMethod != null) {
                        if ((errorTypes & ErrorTypes.dupUnpack) == 0) {
                            unpackMethod.addError(DUPLICATED_UNPACK_METHOD);
                        }
                        errorTypes |= ErrorTypes.dupUnpack;
                        node.addError(DUPLICATED_UNPACK_METHOD);
                    }
                    unpackMethod = node;
                }
                if (!isStatic && isPack) {
                    if (packMethod != null) {
                        if ((errorTypes & ErrorTypes.dupPack) == 0) packMethod.addError(DUPLICATED_PACK_METHOD);
                        errorTypes |= ErrorTypes.dupPack;
                        node.addError(DUPLICATED_PACK_METHOD);
                    }
                    packMethod = node;
                }
                continue;
            }
            JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) jcTree;
            long flags = decl.getModifiers().flags;
            if ((flags & Flags.STATIC) != 0) continue;

            AnnotationValues<Packable.Field> values = node.findAnnotation(Packable.Field.class);
            Packable.Field fieldAnnotation = values == null ? null : values.getInstance();
            if (fieldAnnotation != null && fieldAnnotation.ignore()) continue;
            JCTree type = decl.getType();
            if (!(type instanceof JCTree.JCPrimitiveTypeTree)) {
                node.addError("field of @Packable must be primitive");
                errorTypes |= ErrorTypes.nonPrimitive;
                continue;
            }
            FieldType fieldType = FieldType.forType(((JCTree.JCPrimitiveTypeTree) type));

            if (fieldType == null) {
                node.addError("Unsupported field type for @Packable");
                errorTypes |= ErrorTypes.unsupportedType;
                continue;
            }


            fields.add(new SizedField(
                    node.getName(),
                    fieldType,
                    fieldAnnotation,
                    decl,
                    node
            ));
        }
        Void nil = switch (annotationValues.getInstance().sort()) {

            case noSort -> null;
            case nameSort -> {
                fields.sort(Comparator.comparing(SizedField::getName));
                yield null;
            }
            case sizeSort -> {
                fields.sort(Comparator.comparing(it -> -it.actualSize()));
                yield null;
            }
        };
        if (errorTypes > 0) return;
        if (fields.isEmpty()) {
            typeNode.addError("making a Packable with no fields is utterly pointless.");
            return;
        }
        int total = 0;

        for (SizedField field : fields) {
            field.offset(total);
            total += field.actualSize();
            if (field.fieldAnnotation == null) continue;
            int size = field.fieldAnnotation.size();
            if (size == -1) continue;
            if (!field.type.canRedefineSize) {
                field.node.addError("Cannot change size for " + field.type);
                errorTypes |= ErrorTypes.changingNonChangableSize;
                continue;
            }
        }
        if (errorTypes > 0) return;
        if (total > 64) {
            typeNode.addError("Total field size is bigger than long. Try to use @" + Packable.Field.class.getCanonicalName() + "(size=...)");
            return;
        }
        if (unpackMethod != null) processUnpack(hasEmptyConstructor, typeNode, unpackMethod, fields);
        if (packMethod != null) {

        }
        if (packMethod == null && unpackMethod == null) {
            typeNode.addError("No @Pack and @Unpack method");
        }
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    static class ErrorTypes {
        public static final int dupPack = 1 << 0;
        public static final int dupUnpack = 1 << 1;
        public static final int staticPack = 1 << 2;
        public static final int nonPrimitive = 1 << 3;
        public static final int unsupportedType = 1 << 4;
        public static final int changingNonChangableSize = 1 << 5;
    }
}
