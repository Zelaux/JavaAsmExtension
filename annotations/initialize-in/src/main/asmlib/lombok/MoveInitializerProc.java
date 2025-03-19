package asmlib.lombok;

import asmlib.annotations.initializein.AllowInstanceInitializationOfStaticFields;
import asmlib.annotations.initializein.InitializeIn;
import com.sun.source.tree.*;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.*;
import lombok.core.AST;
import lombok.core.AnnotationValues;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacASTAdapter;
import lombok.javac.JavacNode;
import lombok.javac.JavacResolution;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.lang.String.format;


public class MoveInitializerProc extends JavacASTAdapter {

    public static final String MOVE_INIT_CLASS_NAME = InitializeIn.class.getCanonicalName();
    public static final String MOVE_INIT_SIMPLE_NAME = InitializeIn.class.getSimpleName();
    public static final String FIELD_INTI_$ = "__FIELD__INIT__to_insert$";
    private static final RuntimeException CONTINUE_ERROR = new RuntimeException();

    @Override
    public void visitType(JavacNode typeNode, JCTree.JCClassDecl type) {
        try {
            visitType0(typeNode, type);
        } catch (Throwable e) {
//            System.out.println("ERROR WHILE HANDLING: "+typeNode.getFileName());
            System.err.println("ERROR WHILE HANDLING: "+typeNode.getFileName());
            System.err.println("CLASS NAME          : "+type.name);
            throw Lombok.sneakyThrow(e);
        }
    }
    public void visitType0(JavacNode typeNode, JCTree.JCClassDecl type) {


        AnnotationNode<InitializeIn> classAnno0 = findAnnotation(InitializeIn.class, typeNode);
        InitializeIn classAnno;
        MethodPseudoDesc classPseudoDesc;
        if (classAnno0.isNull()) {
            classAnno = null;
            classPseudoDesc = null;
        } else {
            classAnno = classAnno0.tryCreateAnno(null);
            try {
                classPseudoDesc = MethodPseudoDesc.make(classAnno.value());
            } catch (JavacNodeError e) {
                e.handle(classAnno0.annotationJavacNode);
                return;
            }
        }
        HashMap<MethodPseudoDesc, HashMap<InitializeIn.Position, ArrayList<FieldEntry>>> fields = new HashMap<>();
        boolean hasClassConfig = classAnno != null;

        JavacResolution javacResolution = new JavacResolution(typeNode.getContext());
        JavacTreeMaker maker = typeNode.getTreeMaker();

        HashMap<String, ArrayList<JavacNode>> methodMap = new HashMap<>();
        JCTree.JCBlock nilBoby = maker.Block(0, List.nil());
        for (JavacNode node : typeNode.down()) {
            if (node.getKind() != AST.Kind.METHOD) continue;
            JCTree.JCMethodDecl decl = (JCTree.JCMethodDecl) node.get();
            boolean wasNull = decl.body == null;
            if(wasNull) decl.body = nilBoby;
            javacResolution.resolveClassMember(node);
            if(wasNull)decl.body=null;
            methodMap.computeIfAbsent(node.getName(), it -> new ArrayList<>()).add(node);
        }
        boolean allowedStaticInNonStatic_class = typeNode.hasAnnotation(AllowInstanceInitializationOfStaticFields.class);
        for (JavacNode node : typeNode.down()) {
            if (node.getKind() != AST.Kind.FIELD) continue;

            if (node.hasAnnotation(InitializeIn.Exclude.class)) continue;
            boolean allowedStaticInNonStatic = allowedStaticInNonStatic_class || node.hasAnnotation(AllowInstanceInitializationOfStaticFields.class);
            JCTree.JCVariableDecl jcTree = (JCTree.JCVariableDecl) node.get();

            if ((jcTree.mods.flags & Flags.FINAL) != 0) continue;
            boolean isStatic = (jcTree.mods.flags & Flags.STATIC) != 0;

            AnnotationNode<InitializeIn> values = findAnnotation(InitializeIn.class, node);
            if (hasClassConfig || values.nonNull()) {

                if (jcTree.getInitializer() == null) {
                    if (values.nonNull()) {
                        node.addError(format("Field is annotated with '%s' but has no initializer.", MOVE_INIT_CLASS_NAME));
                    } else {
                        node.addWarning(format("%s: Field skipped because it is not initialized.", MOVE_INIT_SIMPLE_NAME));
                    }
                    continue;
                }
                InitializeIn initializer = values.tryCreateAnno(classAnno);
                JavacNode annotationNode = values.isNull() ? classAnno0.annotationJavacNode : values.annotationJavacNode;
                try {
                    FieldEntry entry = new FieldEntry(
                            node,
                            jcTree,
                            initializer,
                            annotationNode,
                            MethodPseudoDesc.make(initializer.value()),
                            isStatic
                    );
                    if (entry.pseudoDesc.tryResolve(typeNode, annotationNode, methodMap)) {
                        JCTree.JCMethodDecl methodDecl = entry.pseudoDesc.resolved;
                        boolean isMethodStatic = (methodDecl.mods.flags & Flags.STATIC) != 0;
                        if (isMethodStatic && !entry.isStatic) {
                            annotationNode.addError(
                                    format("Cannot initialize non-static field '%s' in static method '%s'", node.getName(), methodDecl.getName().toString())
                            );
                            continue;
                        }
                        if (!isMethodStatic && entry.isStatic && !allowedStaticInNonStatic) {
                            annotationNode.addError(
                                    format("To initialize static field '%s' in non-static method (%s), annotate the field or class with @%s.",
                                            node.getName(),
                                            entry.pseudoDesc.signature(),
                                            AllowInstanceInitializationOfStaticFields.class.getCanonicalName())
                            );
                            continue;
                        }
                        fields
                                .computeIfAbsent(entry.pseudoDesc, it -> new HashMap<>())
                                .computeIfAbsent(entry.initializer.pos(), it -> new ArrayList<>())
                                .add(entry);
                    }
                } catch (JavacNodeError e) {

                    e.handle(annotationNode);
                    continue;
                }
            }

        }
        if (fields.isEmpty()) {
            if (hasClassConfig) {
                typeNode.addWarning(
                        format("Class is annotated with @%s, but no eligible fields were found for initialization.", MOVE_INIT_CLASS_NAME)
                );
            }
            return;
        }
        for (var entry : fields.entrySet()) {
            try {
                for (var entry1 : entry.getValue().entrySet()) {
                    ArrayList<FieldEntry> fieldEntries = entry1.getValue();

                    val types = Types.instance(typeNode.getContext());
                    val symtab = Symtab.instance(typeNode.getContext());
                    for (FieldEntry field : fieldEntries) {
                        JCTree.JCMethodDecl methodDecl = maker.MethodDef(
                                maker.Modifiers(0),
                                typeNode.toName(FIELD_INTI_$ + field.name),
                                maker.Type(symtab.voidType),
                                List.nil(),
                                List.nil(),
                                List.nil(),
                                maker.Block(0, List.of(
                                        maker.Exec(
                                                maker.Assign(
                                                        maker.Ident(field.decl.name),
                                                        field.decl.init
                                                )
                                        )
                                )),
                                null
                        );
                        field.decl.init = null;
                        if (field.annotationNode.get() instanceof JCTree.JCAnnotation annotation) {
                            for (JCTree.JCExpression arg : annotation.args) {
                                if (!(arg instanceof JCTree.JCAssign assign)) continue;
                                if (!assign.lhs.toString().equals("value")) continue;
                                assign.rhs = maker.Literal(field.pseudoDesc.toRealDescriptor(types));
                            }
                        }
                        JavacHandlerUtil.injectMethod(typeNode, methodDecl);
                    }
                }

            } catch (RuntimeException e) {
                if (e == CONTINUE_ERROR) {
                    continue;
                } else {
                    throw e;
                }
            }
        }


    }

    private <T extends Annotation> AnnotationNode<T> findAnnotation(Class<T> type, JavacNode typeNode) {
        JavacNode javacNode = JavacHandlerUtil.findAnnotation(type, typeNode, true);
        if (javacNode == null) return AnnotationNode.nil();
        AnnotationValues<T> values = JavacHandlerUtil.createAnnotation(type, javacNode);
        return new AnnotationNode<>(values, javacNode);
    }

    @AllArgsConstructor
    static class ListEntry {
        List<JCTree.JCStatement> list;
        Consumer<List<JCTree.JCStatement>> setter;

    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC)
    public static class FieldEntry {
        JavacNode node;
        JCTree.JCVariableDecl decl;
        InitializeIn initializer;
        JavacNode annotationNode;
        MethodPseudoDesc pseudoDesc;
        boolean isStatic;
        public String name;

        public FieldEntry(JavacNode node,
                          JCTree.JCVariableDecl decl,
                          InitializeIn initializer,
                          JavacNode annotationNode,
                          MethodPseudoDesc pseudoDesc,
                          boolean isStatic) {
            this(
                    node,
                    decl,
                    initializer,
                    annotationNode,
                    pseudoDesc,
                    isStatic,
                    node.getName()
            );
        }
    }

    @RequiredArgsConstructor
    private static class ListEntryFinder extends TreeScanner<Void, Consumer<ListEntry>> {
        public final JavacTreeMaker maker;
        public final Predicate<Tree> filter;
        public boolean found;

        private boolean checkStatement(Tree head) {
            boolean test = filter.test(head);
            if (test) found = true;
            return test;
        }

        @Override
        public Void visitIf(IfTree node, Consumer<ListEntry> collector) {
            JCTree.JCIf it = (JCTree.JCIf) node;
            super.visitIf(node, collector);
            it.thenpart = checkAndReplaceWithBlock(it.thenpart, collector);
            it.elsepart = checkAndReplaceWithBlock(it.elsepart, collector);
            return null;
        }

        private JCTree.JCStatement checkAndReplaceWithBlock(JCTree.JCStatement statement, Consumer<ListEntry> collector) {
            checkStatement(statement);
            return statement;
//            List<JCTree.JCStatement> stats = List.of(statement);
//            JCTree.JCBlock block = maker.Block(0, stats);
//            collector.accept(new ListEntry(stats, it -> block.stats = it));
        }

        @Override
        public Void visitForLoop(ForLoopTree node, Consumer<ListEntry> collector) {
            JCTree.JCForLoop it = (JCTree.JCForLoop) node;
            super.visitForLoop(node, collector);
            it.body = checkAndReplaceWithBlock(it.body, collector);
            return null;
        }

        @Override
        public Void visitEnhancedForLoop(EnhancedForLoopTree node, Consumer<ListEntry> collector) {
            JCTree.JCEnhancedForLoop it = (JCTree.JCEnhancedForLoop) node;
            super.visitEnhancedForLoop(node, collector);
            it.body = checkAndReplaceWithBlock(it.body, collector);
            return null;
        }

        @Override
        public Void visitDoWhileLoop(DoWhileLoopTree node, Consumer<ListEntry> collector) {
            JCTree.JCDoWhileLoop it = (JCTree.JCDoWhileLoop) node;
            super.visitDoWhileLoop(node, collector);
            it.body = checkAndReplaceWithBlock(it.body, collector);
            return null;
        }

        @Override
        public Void visitWhileLoop(WhileLoopTree node, Consumer<ListEntry> collector) {
            JCTree.JCWhileLoop it = (JCTree.JCWhileLoop) node;
            super.visitWhileLoop(node, collector);
            it.body = checkAndReplaceWithBlock(it.body, collector);
            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Consumer<ListEntry> listEntryConsumer) {
            return null;
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Consumer<ListEntry> listEntryConsumer) {
            return null;
        }

        @Override
        public Void scan(Iterable<? extends Tree> nodes, Consumer<ListEntry> collector) {
            if (!(nodes instanceof List<? extends Tree> list)) return super.scan(nodes, collector);

            List<? extends Tree> prev = list;
            for (List<? extends Tree> it = list; it != null; it = it.tail) {
                if (checkStatement(it.head)) {
                    //noinspection unchecked
//                    List<JCTree.JCStatement> prev1 = (List<JCTree.JCStatement>) prev;
                    //noinspection unchecked
//                    collector.accept(new ListEntry((List<JCTree.JCStatement>) it, v -> prev1.tail = v));
                }
                prev = it;
            }

            return super.scan(list, collector);
        }
    }
}
