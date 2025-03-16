package asmlib.lombok;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;
import lombok.javac.JavacNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static java.lang.String.format;

@RequiredArgsConstructor
public class MethodPseudoDesc implements Comparable<MethodPseudoDesc> {
    public static final String[] EMPTY_STRING = new String[0];
    public final String name;
    public final String[] params;
    public JCTree.JCMethodDecl resolved;


    public static MethodPseudoDesc make(String string) throws JavacNodeError {
        string = string.trim();
        int paramsStart = string.indexOf('(');
        if (paramsStart == -1) return new MethodPseudoDesc(string, EMPTY_STRING);
        int endParams = string.indexOf(')');
        if (endParams == -1) throw new JavacNodeError(format("Missing ')' in '%s'", string));
        String paramStrings = string.substring(paramsStart + 1, endParams).trim();
        if (paramStrings.isEmpty()) return new MethodPseudoDesc(string, EMPTY_STRING);
        String[] split = paramStrings.split(",");
        for (int i = 0; i < split.length; i++) {
            String trim = split[i].trim();
            if (!trim.matches("\\w+(\\.\\w+)*"))
                throw new JavacNodeError(format("Error in param[%d]='%s', allowed only this type declaration 'A' or 'package1.package2.A'", i, trim));
            split[i] = trim;
        }
        return new MethodPseudoDesc(string.substring(0, paramsStart), split);
    }

    private static @NotNull String toRealDescriptor(JCTree.JCMethodDecl decl, Types types) {
        String name = decl.name.toString();
        JCTree returnType1 = decl.getReturnType();
        Type returnType = resolveType(returnType1, types);

        Type[] args = new Type[decl.params.size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = resolveType(decl.params.get(i).vartype, types);
        }

//        String returnType = type.toString();
        return name + Type.getMethodDescriptor(
                returnType,
                args
        );
    }

    private static @NotNull Type resolveType(JCTree returnType1, Types types) {
        var type = types.erasure(returnType1.type);
        return resolve(type);
    }

    private static @Nullable Type resolve(com.sun.tools.javac.code.Type type) {
        TypeTag tag = type.getTag();
        return switch (tag) {
            case BYTE -> Type.BYTE_TYPE;
            case CHAR -> Type.CHAR_TYPE;
            case SHORT -> Type.SHORT_TYPE;
            case LONG -> Type.LONG_TYPE;
            case FLOAT -> Type.FLOAT_TYPE;
            case INT -> Type.INT_TYPE;
            case DOUBLE -> Type.DOUBLE_TYPE;
            case BOOLEAN -> Type.BOOLEAN_TYPE;
            case VOID -> Type.VOID_TYPE;
            case CLASS -> Type.getObjectType(getString(type));

            case ARRAY ->
                    Type.getObjectType("[" + resolve(((com.sun.tools.javac.code.Type.ArrayType) type).elemtype).getDescriptor());

            case METHOD -> null;
            case BOT -> null;
            case PACKAGE -> null;
            case MODULE -> null;
            case TYPEVAR -> null;
            case WILDCARD -> null;
            case FORALL -> null;
            case DEFERRED -> null;
            case NONE -> null;
            case ERROR -> null;
            case UNKNOWN -> null;
            case UNDETVAR -> null;
            case UNINITIALIZED_THIS -> null;
            case UNINITIALIZED_OBJECT -> null;
        };
    }

    private static @NotNull String getString(com.sun.tools.javac.code.Type type) {
        String string = type.toString();
        int i = string.indexOf('<');
        if (i != -1) string = string.substring(0, i);
        return string.replace('.', '/');
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MethodPseudoDesc that = (MethodPseudoDesc) o;
        if (resolved != null) {
            return resolved == that.resolved;
        }
        return Objects.equals(name, that.name) && Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        if (resolved != null) return resolved.hashCode() + name.hashCode();
        int result = Objects.hashCode(name);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }

    @Override
    public int compareTo(@NotNull MethodPseudoDesc o) {
        if (equals(o)) return 0;
        int i = name.compareTo(o.name);
        if (i != 0) return i;
        return Arrays.compare(params, o.params);
    }

    @Override
    public String toString() {
        return signature() + (resolved != null ? "[resolved]" : "");
    }

    public @NotNull String signature() {
        return name + '(' + String.join(", ", params) + ')';
    }

    public boolean tryResolve(JavacNode typeNode, JavacNode annotationNode, HashMap<String, ArrayList<JavacNode>> methodMap) {
        ArrayList<JavacNode> list = methodMap.get(name);
        if (list == null || list.isEmpty()) {
            annotationNode.addError(
                    format("cannot find method with name '%s'", name)
            );
            return false;
        }
        if(params.length==0 && list.size()==1) {
            return checkAndSet(
                    (JCTree.JCMethodDecl) list.get(0).get(),
                    typeNode,annotationNode
            );
        }
        listVisitor:
        for (JavacNode node : list) {
            JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) node.get();
//            JavacResolver.CLASS.resolveMember();


            List<JCTree.JCVariableDecl> jcVariableDecls = methodDecl.params;

            if (jcVariableDecls.size() != params.length) continue;
            for (int i = 0; i < jcVariableDecls.size(); i++) {
                JCTree.JCVariableDecl param = jcVariableDecls.get(i);
                if (!Util.typeMatches(param.vartype.toString(), node, params[i])) {
                    continue listVisitor;
                }
            }
            return checkAndSet(methodDecl, typeNode, annotationNode);

        }
        annotationNode.addError(
                format("Cannot find method with signature '%s'", signature())
        );
        return false;
    }

    private boolean checkAndSet(JCTree.JCMethodDecl resolved, JavacNode typeNode, JavacNode annotationNode) {
        this.resolved = resolved;
        if (resolved.body != null) return true;
        if (resolved.restype != null && resolved.restype.toString().equals("void")) {
            resolved.mods.flags&=~(Flags.NATIVE|Flags.ABSTRACT);
            resolved.body=typeNode.getTreeMaker().Block(0,List.nil());
            return true;
        }
        annotationNode.addError("You cannot move initialization in native or abstract with non void return type");

        return false;
    }

    public String toRealDescriptor(Types types) {
        if (resolved == null) throw new RuntimeException("I'm not resolved (" + signature() + ")");
        return toRealDescriptor(resolved, types);
    }
}
