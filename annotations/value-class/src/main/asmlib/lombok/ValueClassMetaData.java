package asmlib.lombok;

import com.sun.tools.javac.tree.JCTree;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacNode;

@SuppressWarnings("ClassCanBeRecord")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class ValueClassMetaData {
    String name;
    FieldMeta[] fields;
    MethodMeta[] methods;
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static class FieldMeta {
        String name;
        JCTree.JCVariableDecl decl;
        JavacNode node;
    }
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    public static class MethodMeta {
        String name;
        JCTree.JCMethodDecl decl;
//        String desc;
        JavacNode node;
    }

}
