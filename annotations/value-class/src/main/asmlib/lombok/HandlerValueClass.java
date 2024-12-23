package asmlib.lombok;

import asmlib.annotations.*;
import asmlib.lombok.ValueClassMetaData.FieldMeta;
import asmlib.lombok.ValueClassMetaData.MethodMeta;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import lombok.core.*;
import lombok.javac.*;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class HandlerValueClass extends JavacAnnotationHandler<ValueClass> {


    private static String absoluteName(JavacNode typeNode) {
        List<String> builder = new ArrayList<>();
        while (typeNode.getName() != null) {
            builder.add(typeNode.getName());
            typeNode = typeNode.up();
        }
        builder.add(typeNode.getPackageDeclaration());
        String[] strings = new String[builder.size()];
        for (int i = 0; i < builder.size(); i++) strings[builder.size() - 1 - i] = builder.get(i);

        return String.join(".", strings);
    }

    @Override
    public void handle(AnnotationValues<ValueClass> annotation, JCAnnotation ast, JavacNode annotationNode) {
        System.out.println(ast);
        JavacNode typeNode = annotationNode.up();
        LombokImmutableList<JavacNode> down = typeNode.down();
        List<FieldMeta> fields = new ArrayList<>();
        List<MethodMeta> methods = new ArrayList<>();
        boolean hasErrors = false;
        for (JavacNode node : down) {
            AST.Kind kind = node.getKind();
            JCTree jcTree = node.get();
            if (kind != AST.Kind.FIELD) {
                if (kind == AST.Kind.METHOD) {
                    JCMethodDecl methodDecl = (JCMethodDecl) jcTree;
                    if ((methodDecl.getModifiers().flags & Flags.STATIC) == 0) {
                        methods.add(new MethodMeta(methodDecl.name.toString(), methodDecl, node));
                    }
                }
                continue;
            }
            JCVariableDecl decl = (JCVariableDecl) jcTree;
            long flags = decl.getModifiers().flags;
            if ((flags & Flags.STATIC) != 0) continue;
            if ((flags & Flags.FINAL) == 0) {
                node.addError("field of @ValueClass must be final");
                hasErrors = true;
            }
            if (decl.getType().toString().equals(typeNode.getName())) {
                node.addError("@ValueClass not supported nested value classes");
                hasErrors = true;
            }
            fields.add(new FieldMeta(node.getName(), decl, node));
        }
        if (hasErrors) return;
        String name = absoluteName(typeNode);
        PostCompilerValueClass.valueClasses.put(
                name,
                new ValueClassMetaData(name, fields.toArray(FieldMeta[]::new), methods.toArray(MethodMeta[]::new))
        );
        JavacTreeMaker treeMaker = typeNode.getTreeMaker();
        boolean isEasy = fields.size() == 1;
        if (isEasy) {

        }
//        throw null;
    }

}
