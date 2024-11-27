package asmlib.javaparser;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.Modifier.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.expr.BinaryExpr.*;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.nodeTypes.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType.*;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.WildcardType;
import com.github.javaparser.ast.visitor.*;
import com.sun.source.tree.CaseTree.*;
import com.sun.source.tree.MemberReferenceTree.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.parser.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.JCDiagnostic.*;
import com.sun.tools.javac.util.List;
import lombok.*;
import lombok.javac.*;
import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

import static com.sun.tools.javac.tree.JCTree.*;


@SuppressWarnings("unused")
public class CompileBodyVisitor implements GenericVisitor<DiagnosticPosition, Void> {
    public final JavacTreeMaker maker;
    public final TreeMaker imaker;

    private final Symtab symtab;
    private final JavacAST ast;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Names names;
    private final ParserFactory parserFactory;

    @SneakyThrows
    public CompileBodyVisitor(JavacTreeMaker maker, JavacAST ast, Context context) {
        this.maker = maker;
        imaker = maker.getUnderlyingTreeMaker();
        this.ast = ast;
//        this.context = context;
        this.symtab = Symtab.instance(context);
        names = Names.instance(context);
        parserFactory = ParserFactory.instance(context);
    }

    @SuppressWarnings("unused")
    @MagicConstant(flagsFromClass = Flags.class)
    private static long flags(@MagicConstant(flagsFromClass = Flags.class) long... flags) {
        long flag = 0;
        for (long l : flags) {
            flag |= l;
        }
        return flag;
    }

    @MagicConstant(flagsFromClass = Flags.class)
    private static long flags(@MagicConstant(flagsFromClass = Flags.class) int... flags) {
        long flag = 0;
        for (long l : flags) {
            flag |= l;
        }
        return flag;
    }

    private static <T> List<T> nil() {
        return List.nil();
    }

    @NotNull
    private static <T> T parent(Node n) {
        //noinspection unchecked,OptionalGetWithoutIsPresent
        return (T) n.getParentNode().get();
    }

    private static Tag map(Operator operator) {
        return switch (operator) {

            case OR -> Tag.OR;
            case AND -> Tag.AND;
            case BINARY_OR -> Tag.BITOR;
            case BINARY_AND -> Tag.BITAND;
            case XOR -> Tag.BITXOR;
            case EQUALS -> Tag.EQ;
            case NOT_EQUALS -> Tag.NE;
            case LESS -> Tag.LT;
            case GREATER -> Tag.GT;
            case LESS_EQUALS -> Tag.LE;
            case GREATER_EQUALS -> Tag.GE;
            case LEFT_SHIFT -> Tag.SL;
            case SIGNED_RIGHT_SHIFT -> Tag.SR;
            case UNSIGNED_RIGHT_SHIFT -> Tag.USR;
            case PLUS -> Tag.PLUS;
            case MINUS -> Tag.MINUS;
            case MULTIPLY -> Tag.MUL;
            case DIVIDE -> Tag.DIV;
            case REMAINDER -> Tag.MOD;
        };
    }

    private static long map(Keyword keyword) {
        return switch (keyword) {
            case PUBLIC -> java.lang.reflect.Modifier.PUBLIC;
            case PROTECTED -> java.lang.reflect.Modifier.PROTECTED;
            case PRIVATE -> java.lang.reflect.Modifier.PRIVATE;
            case ABSTRACT -> java.lang.reflect.Modifier.ABSTRACT;
            case STATIC -> java.lang.reflect.Modifier.STATIC;
            case FINAL -> java.lang.reflect.Modifier.FINAL;
            case TRANSIENT -> java.lang.reflect.Modifier.TRANSIENT;
            case VOLATILE -> java.lang.reflect.Modifier.VOLATILE;
            case SYNCHRONIZED -> java.lang.reflect.Modifier.SYNCHRONIZED;
            case NATIVE -> java.lang.reflect.Modifier.NATIVE;
            case STRICTFP -> java.lang.reflect.Modifier.STRICT;
            case DEFAULT, TRANSITIVE, SEALED, NON_SEALED -> 0;
        };
    }

    private static <T> T impossible() {
        throw new UnsupportedOperationException();
    }

    private JCModifiers modifiers(NodeWithModifiers<?> node) {
        long modifiers = 0;
        for (Modifier modifier : node.getModifiers()) {
            Keyword keyword = modifier.getKeyword();
            modifiers |= map(keyword);
        }
        return maker.Modifiers(modifiers);
    }

    @Override
    public JCTree visit(CompilationUnit n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(PackageDeclaration n, Void arg) {
        return impossible();
    }

    @Override
    public JCTypeParameter visit(TypeParameter n, Void arg) {
        return maker.TypeParameter(name(n), transformList(n.getTypeBound()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T extends JCTree> List<T> transformList(Optional<? extends NodeList<? extends Node>> nodeList) {
        return transformList(nodeList.orElse(null));
    }

    private <T extends JCTree> List<T> transformList(@Nullable NodeList<? extends Node> nodeList) {
        if (nodeList == null) return nil();

        //noinspection unchecked
        JCTree[] array = nodeList.stream().flatMap(it -> {
            NodeList<VariableDeclarator> inner = null;
            if (it instanceof FieldDeclaration fieldDeclaration) {
                inner = fieldDeclaration.getVariables();
            } else if (it instanceof VariableDeclarationExpr declarationExpr) {
                inner = declarationExpr.getVariables();
            }
            if (inner != null) //noinspection unchecked
                return inner.stream().map(it1 -> (T) it1.accept(this, null));
            //noinspection unchecked
            return Stream.of((T) it.accept(this, null));
        }).toArray(JCTree[]::new);
        //noinspection unchecked
        return (List<T>) List.from(array);

    }

    private com.sun.tools.javac.util.Name name(NodeWithName<?> name) {
        return name(name.getNameAsString());
    }

    @Contract("null->null;!null->!null")
    @Nullable
    private com.sun.tools.javac.util.Name name(@Nullable NodeWithSimpleName<?> name) {
        if (name == null) return null;
        return name(name.getNameAsString());
    }

    @Contract("null->null;!null->!null")
    @Nullable
    private com.sun.tools.javac.util.Name name(@Nullable String name) {
        if (name == null) return null;
        return ast.toName(name);
    }

    @Override
    public JCTree visit(LineComment n, Void arg) {
        return null;//TODO comment support
    }

    @Override
    public JCTree visit(BlockComment n, Void arg) {
        return null;//TODO comment support
    }

    @Override
    public JCClassDecl visit(ClassOrInterfaceDeclaration n, Void arg) {
        return maker.ClassDef(modifiers(n), name(n), typeArgs(n), (JCExpression) n.accept(this, null), transformList(n.getImplementedTypes()), transformList(n.getMembers()));
    }

    private List<JCTypeParameter> typeArgs(NodeWithTypeParameters<?> n) {
        return transformList(n.getTypeParameters());
    }

    private JCModifiers modifiers(NodeWithModifiers<?> node, @MagicConstant(flagsFromClass = Flags.class) long extraMod) {
        JCModifiers modifiers = modifiers(node);
        modifiers.flags |= extraMod;
        return modifiers;
    }

    @SuppressWarnings("unused")
    private JCModifiers modifiers(NodeWithModifiers<?> node, @MagicConstant(flagsFromClass = Flags.class) int extraMod) {
        return modifiers(node, (long) extraMod);
    }

    //region record
    @Override
    public JCTree visit(RecordDeclaration n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(CompactConstructorDeclaration n, Void arg) {
        return impossible();
    }

    //endregion
    @Override
    public JCTree visit(EnumDeclaration n, Void arg) {
        JCModifiers modifiers = modifiers(n);
        modifiers.flags |= Flags.ENUM;

        return maker.ClassDef(modifiers, name(n), nil(), null, transformList(n.getImplementedTypes()), transformList(n.getMembers()));
    }

    @Override
    public JCTree visit(EnumConstantDeclaration n, Void arg) {

        EnumDeclaration parent = parent(n);

        JCExpression init = maker.NewClass(null, nil(), ident(parent), nil(), null);
        return maker.VarDef(modifiers(Flags.GENERATEDCONSTR | Flags.PUBLIC | Flags.STATIC | Flags.FINAL), name(parent), ident(parent), init);
    }

    @SuppressWarnings("SameParameterValue")
    private JCModifiers modifiers(@MagicConstant(flagsFromClass = Flags.class) Long flags) {//TODO after compile replace Long to long
        return maker.Modifiers(flags);
    }

    @SuppressWarnings("SameParameterValue")
    private JCModifiers modifiers(@MagicConstant(flagsFromClass = Flags.class) int flags) {//TODO after compile replace Long to long
        return maker.Modifiers(flags);
    }

    @Contract("null->null;!null->!null")
    @Nullable
    private JCIdent ident(@Nullable NodeWithName<?> parent) {
        if (parent == null) return null;
        return maker.Ident(name(parent));
    }

    @Contract("null->null;!null->!null")
    @Nullable
    private JCIdent ident(@Nullable NodeWithSimpleName<?> parent) {
        if (parent == null) return null;
        return maker.Ident(name(parent));
    }

    @Override
    public JCClassDecl visit(AnnotationDeclaration n, Void arg) {

        return maker.ClassDef(modifiers(n, flags(Flags.ANNOTATION | Flags.ENUM)), name(n), nil(), null, null, transformList(n.getMembers()));
    }

    protected <T extends JCTree> T transform(Node node) {
        //noinspection unchecked
        return (T) node.accept(this, null);
    }

    @Override
    public JCMethodDecl visit(AnnotationMemberDeclaration n, Void arg) {
        return maker.MethodDef(maker.Modifiers(flags()), name(n), type(n.getType()), nil(), nil(), nil(), null, transform(n.getDefaultValue()));
    }

    @Nullable
    private <T extends JCTree, B extends Node> T transform(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<B> defaultValue) {
        return defaultValue.map(this::<T>transform).orElse(null);
    }

    @Override
    public JCVariableDecl visit(FieldDeclaration n, Void arg) {
        return why();
    }

    private <T> T why() {
        throw new UnsupportedOperationException("This shouldn't be call");
    }

    @Override
    public JCTree visit(VariableDeclarator n, Void arg) {
        NodeWithModifiers<?> fieldDeclaration = parent(n);
        return maker.VarDef(modifiers(fieldDeclaration), name(n), type(n.getType()), transform(n.getInitializer()));
    }

    private JCExpression type(Type type) {
//        return maker.Ident(name(type.asString()));
        return transform(type);
    }

    @Override
    public JCMethodDecl visit(ConstructorDeclaration n, Void arg) {
        return maker.MethodDef(
                modifiers(n),
                n.isStatic() ? name("<cinit>") : name("<init>"),
                maker.Type(symtab.voidType),
                typeArgs(n),
                transformList(n.getParameters()),
                transformList(n.getThrownExceptions()),
                transform(n.getBody()),
                null
        );
    }

    @Override
    public JCMethodDecl visit(MethodDeclaration n, Void arg) {
        return maker.MethodDef(
                modifiers(n),
                name(n),
                type(n.getType()),
                typeArgs(n),
                transformList(n.getParameters()),
                transformList(n.getThrownExceptions()),
                transform(n.getBody()),
                null
        );
    }

    @Override
    public JCVariableDecl visit(Parameter n, Void arg) {
        return maker.VarDef(
                modifiers(n),
                name(n),
                type(n.getType()),
                null
        );
    }

    @Override
    public JCTree visit(InitializerDeclaration n, Void arg) {
        return maker.MethodDef(
                modifiers(0L),
                name("<cinit>"),
                maker.Type(symtab.voidType),
                nil(),
                nil(),
                nil(),
                transform(n.getBody()),
                null
        );
    }

    @Override
    public JCTree visit(JavadocComment n, Void arg) {
//        return maker.ReceiverVarDef()TODO java doc
        return null;
    }

    @Override
    public JCExpression visit(ClassOrInterfaceType n, Void arg) {
        classSymbol(n);
        JCExpression origin = transform(n.getScope());
        com.sun.tools.javac.util.Name name = name(n.getNameAsString());
        if (origin == null) return maker.Ident(name);
        return maker.Select(origin, name);
    }

    private ClassSymbol classSymbol(ClassOrInterfaceType n) {
        ClassSymbol owner = n.getScope().map(this::classSymbol)
                .orElse(null);
        com.sun.tools.javac.util.Name name = name(n.getNameAsString());
        if (owner == null) return new ClassSymbol(0, name, null);
        return new ClassSymbol(0, name, owner);
    }

    @Override
    public JCTree visit(PrimitiveType n, Void arg) {
        return maker.Type(jcTree(n.getType()));
    }

    @Nullable
    public JCPrimitiveType jcTree(Primitive type) {
        return switch (type) {
            case BOOLEAN -> symtab.booleanType;
            case CHAR -> symtab.charType;
            case BYTE -> symtab.byteType;
            case SHORT -> symtab.shortType;
            case INT -> symtab.intType;
            case LONG -> symtab.longType;
            case FLOAT -> symtab.floatType;
            case DOUBLE -> symtab.doubleType;
        };
    }

    @Override
    public JCTree visit(ArrayType n, Void arg) {
        return maker.TypeArray(type(n.getComponentType()));
    }

    @Override
    public JCExpression visit(ArrayCreationLevel n, Void arg) {
        return transform(n.getDimension());
    }

    @Override
    public JCTree visit(ArrayCreationExpr n, Void arg) {
        return maker.NewArray(
                type(n.getElementType()),
                transformList(n.getLevels()),
                transformList(n.getInitializer().map(ArrayInitializerExpr::getValues).orElse(null))
        );
    }

    @Override
    public JCArrayAccess visit(ArrayAccessExpr n, Void arg) {
        return maker.Indexed(transform(n.getName()), transform(n.getIndex()));
    }

    @SneakyThrows
    @Override
    public JCTypeIntersection visit(IntersectionType n, Void arg) {
        return imaker.TypeIntersection(transformList(n.getElements()));
    }

    @Override
    public JCTypeUnion visit(UnionType n, Void arg) {
        return imaker.TypeUnion(transformList(n.getElements()));
    }

    @Override
    public JCExpression visit(VoidType n, Void arg) {
        return imaker.Type(symtab.voidType);
    }

    @Override
    public JCWildcard visit(WildcardType n, Void arg) {
        BoundKind kind = BoundKind.UNBOUND;
        JCExpression expression = transform(n.getExtendedType());
        if (expression != null) kind = BoundKind.EXTENDS;
        else {
            expression = transform(n.getSuperType());
            if (expression != null) kind = BoundKind.SUPER;
        }
        return imaker.Wildcard(imaker.TypeBoundKind(kind), expression);
    }

    @Override
    public JCTree visit(UnknownType n, Void arg) {
        return imaker.Type(symtab.unknownType);
    }

    @Override
    public JCTree visit(ArrayInitializerExpr n, Void arg) {
        return impossible();
    }

    @Override
    public JCAssign visit(AssignExpr n, Void arg) {
        return maker.Assign(transform(n.getTarget()), transform(n.getValue()));
    }

    @Override
    public JCBinary visit(BinaryExpr n, Void arg) {
        return imaker.Binary(
                map(n.getOperator()),
                transform(n.getLeft()),
                transform(n.getRight())
        );
    }

    @Override
    public JCTree visit(CastExpr n, Void arg) {
        return imaker.TypeCast(
                transform(n.getType()),
                transform(n.getExpression())
        );
    }

    @Override
    public JCTree visit(ClassExpr n, Void arg) {
//        imaker.ClassLiteral(classSymbol(n.as))
        //TODO better compilation
        return parser(n).parseExpression();
    }

    @Override
    public JCTree visit(ConditionalExpr n, Void arg) {
        return maker.Conditional(
                transform(n.getCondition()),
                transform(n.getThenExpr()),
                transform(n.getElseExpr())
        );
    }

    @Override
    public JCTree visit(EnclosedExpr n, Void arg) {
        return maker.Parens(transform(n.getInner()));
    }

    @Override
    public JCFieldAccess visit(FieldAccessExpr n, Void arg) {
        return maker.Select(transform(n.getScope()), name(n));
    }

    @Override
    public JCInstanceOf visit(InstanceOfExpr n, Void arg) {
        return maker.TypeTest(
                transform(n.getExpression()),
                type(n.getType())
        );
    }

    @Override
    public JCLiteral visit(StringLiteralExpr n, Void arg) {
        return maker.Literal(n.getValue());
    }

    @Override
    public JCLiteral visit(IntegerLiteralExpr n, Void arg) {
        return maker.Literal(n.asNumber().intValue());
    }

    @Override
    public JCLiteral visit(LongLiteralExpr n, Void arg) {
        return maker.Literal(n.asNumber().longValue());
    }

    @Override
    public JCLiteral visit(CharLiteralExpr n, Void arg) {
        return maker.Literal(n.asChar());
    }

    @Override
    public JCLiteral visit(DoubleLiteralExpr n, Void arg) {
        return maker.Literal(n.asDouble());
    }

    @Override
    public JCLiteral visit(BooleanLiteralExpr n, Void arg) {
        return maker.Literal(n.getValue());
    }

    @Override
    public JCLiteral visit(NullLiteralExpr n, Void arg) {
        return maker.Literal(JavacTreeMaker.TypeTag.typeTag("BOT"), null);
    }

    @Override
    public JCMethodInvocation visit(MethodCallExpr n, Void arg) {
        com.sun.tools.javac.util.Name name = name(n);
        return maker.Apply(
                transformList(n.getTypeArguments()),
                n.getScope()
                        .map(this::<JCExpression>transform)
                        .<JCExpression>map(it -> maker.Select(it, name))
                        .orElse(maker.Ident(name))
                ,
                transformList(n.getArguments())
        );
    }

    @Override
    public JCTree visit(NameExpr n, Void arg) {
        return maker.Ident(name(n));
    }

    @Override
    public JCNewClass visit(ObjectCreationExpr n, Void arg) {
        return maker.NewClass(
                null,//TODO add enclosing expression
                transformList(n.getTypeArguments()),
                type(n.getType()),
                transformList(n.getArguments()),
                n.getAnonymousClassBody()
                        .map(it -> maker.ClassDef(
                                modifiers(Flags.PRIVATE),
                                null,
                                transformList(n.getTypeArguments()),
                                ident(n.getType()),
                                nil(),
                                transformList(it)

                        )).orElse(null)
        );
    }

    @Override
    public JCIdent visit(ThisExpr n, Void arg) {
        return parseAsExpression(n);
    }

    private JavacParser parser(Node n) {
        return parser(n.toString());
    }

    @Override
    public JCIdent visit(SuperExpr n, Void arg) {
        return parseAsExpression(n);
    }

    private <T extends JCExpression> T parseAsExpression(Node n) {
        //noinspection unchecked
        return (T) parser(n).parseExpression();
    }

    private JavacParser parser(String string) {
        return parserFactory.newParser(string, true, true, true);
    }

    @Override
    public JCUnary visit(UnaryExpr n, Void arg) {
        return imaker.Unary(map(n.getOperator()), transform(n.getExpression()));
    }

    private Tag map(UnaryExpr.Operator operator) {
        return switch (operator) {
            case PLUS -> Tag.POS;
            case MINUS -> Tag.NEG;
            case PREFIX_INCREMENT -> Tag.PREINC;
            case PREFIX_DECREMENT -> Tag.PREDEC;
            case LOGICAL_COMPLEMENT -> Tag.NOT;
            case BITWISE_COMPLEMENT -> Tag.COMPL;
            case POSTFIX_INCREMENT -> Tag.POSTINC;
            case POSTFIX_DECREMENT -> Tag.POSTDEC;
        };
    }

    @Override
    public JCTree visit(VariableDeclarationExpr n, Void arg) {

        return why();
    }

    @Override
    public JCTree visit(MarkerAnnotationExpr n, Void arg) {
        return maker.Annotation(ident(n), nil());
    }

    @Override
    public JCTree visit(SingleMemberAnnotationExpr n, Void arg) {
        return maker.Annotation(
                ident(n),
                List.of(transform(n.getMemberValue()))
        );
    }

    @Override
    public JCTree visit(NormalAnnotationExpr n, Void arg) {
        return maker.Annotation(
                ident(n),
                transformList(n.getPairs())
        );
    }

    @Override
    public JCAssign visit(MemberValuePair n, Void arg) {
        return maker.Assign(
                maker.Ident(name(n.getNameAsString())), transform(n.getValue())
        );
    }

    @Override
    public JCTree visit(ExplicitConstructorInvocationStmt n, Void arg) {
        return maker.Apply(
                transformList(n.getTypeArguments()),
                maker.Ident(n.isThis() ? names._this : names._super),
                transformList(n.getArguments())
        );
    }

    @Override
    public JCTree visit(LocalClassDeclarationStmt n, Void arg) {
        return visit(n.getClassDeclaration(), arg);
    }

    @Override
    public JCTree visit(LocalRecordDeclarationStmt n, Void arg) {
        return visit(n.getRecordDeclaration(), arg);
    }

    @Override
    public JCAssert visit(AssertStmt n, Void arg) {
        return maker.Assert(
                transform(n.getCheck()),
                transform(n.getMessage())
        );
    }

    @Override
    public JCBlock visit(BlockStmt n, Void arg) {
        return maker.Block(0, transformList(n.getStatements()));
    }

    @Override
    public JCTree visit(LabeledStmt n, Void arg) {
        return maker.Labelled(
                name(n.getLabel().getIdentifier()),
                transform(n.getStatement())
        );
    }

    @Override
    public JCSkip visit(EmptyStmt n, Void arg) {
        return maker.Skip();
    }

    @Override
    public JCExpressionStatement visit(ExpressionStmt n, Void arg) {
        return maker.Exec(transform(n.getExpression()));
    }

    @Override
    public JCSwitch visit(SwitchStmt n, Void arg) {
        return maker.Switch(
                transform(n.getSelector()),
                transformList(n.getEntries())
        );
    }

    @Override
    public JCCase visit(SwitchEntry n, Void arg) {

        SwitchEntry.Type type = n.getType();
        CaseKind kind = switch (type) {
            case STATEMENT_GROUP -> CaseKind.STATEMENT;
            case EXPRESSION, BLOCK, THROWS_STATEMENT -> CaseKind.RULE;
        };

        return imaker.Case(
                kind,
                transformList(n.getLabels()),
                transformList(n.getStatements()),
                kind == CaseKind.RULE ? transform(new BlockStmt(n.getStatements())) : null
        );
    }

    @Override
    public JCTree visit(BreakStmt n, Void arg) {
        return maker.Break(name(n.getLabel()));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private com.sun.tools.javac.util.Name name(Optional<SimpleName> label) {
        return label.map(SimpleName::getIdentifier).map(this::name).orElse(null);
    }

    @Override
    public JCTree visit(ReturnStmt n, Void arg) {
        return maker.Return(transform(n.getExpression()));
    }

    @Override
    public JCTree visit(IfStmt n, Void arg) {
        return maker.If(
                transform(n.getCondition()),
                transform(n.getThenStmt()),
                transform(n.getElseStmt())
        );
    }

    @Override
    public JCTree visit(WhileStmt n, Void arg) {
        return maker.WhileLoop(
                transform(n.getCondition()),
                transform(n.getBody())
        );
    }

    @Override
    public JCTree visit(ContinueStmt n, Void arg) {
        return maker.Continue(name(n.getLabel()));
    }

    @Override
    public JCTree visit(DoStmt n, Void arg) {
        return maker.DoLoop(
                transform(n.getBody()),
                transform(n.getCondition())
        );
    }

    @Override
    public JCTree visit(ForEachStmt n, Void arg) {
        return maker.ForeachLoop(
                transform(n.getVariable()),
                transform(n.getIterable()),
                transform(n.getBody())
        );
    }

    @Override
    public JCTree visit(ForStmt n, Void arg) {
        return maker.ForLoop(
                transformList(n.getInitialization()),
                transform(n.getCompare()),
                transformList(n.getUpdate()),
                transform(n.getBody())
        );
    }

    @Override
    public JCTree visit(ThrowStmt n, Void arg) {
        return maker.Throw(transform(n.getExpression()));
    }

    @Override
    public JCTree visit(SynchronizedStmt n, Void arg) {
        return maker.Synchronized(
                transform(n.getExpression()),
                transform(n.getBody())
        );
    }

    @Override
    public JCTree visit(TryStmt n, Void arg) {
        if (n.getResources().isEmpty()) {
            return maker.Try(
                    transform(n.getTryBlock()),
                    transformList(n.getCatchClauses()),
                    transform(n.getFinallyBlock())
            );
        }
        return maker.Try(
                transformList(n.getResources()),
                transform(n.getTryBlock()),
                transformList(n.getCatchClauses()),
                transform(n.getFinallyBlock())
        );
    }

    @Override
    public JCCatch visit(CatchClause n, Void arg) {
        return maker.Catch(
                transform(n.getParameter()),
                transform(n.getBody())
        );
    }

    @Override
    public JCTree visit(LambdaExpr n, Void arg) {
        return imaker.Lambda(
                transformList(n.getParameters()),
                transform(n.getBody())
        );
    }

    @Override
    public JCMemberReference visit(MethodReferenceExpr n, Void arg) {
        ReferenceMode invoke = ReferenceMode.INVOKE;
        if (n.getIdentifier().equals("new")) {
            invoke = ReferenceMode.NEW;
        }

        return imaker.Reference(invoke,
                name(n.getIdentifier()),
                transform(n.getScope()),
                transformList(n.getTypeArguments())
        );
    }

    @Override
    public JCTree visit(TypeExpr n, Void arg) {
        return type(n.getType());
    }

    @Override
    public JCTree visit(NodeList n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(Name n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(SimpleName n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ImportDeclaration n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ModuleDeclaration n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ModuleRequiresDirective n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ModuleExportsDirective n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ModuleProvidesDirective n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ModuleUsesDirective n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(ModuleOpensDirective n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(UnparsableStmt n, Void arg) {
        throw new UnsupportedOperationException(n.getParsed() + "");
    }

    @Override
    public JCTree visit(ReceiverParameter n, Void arg) {
        return maker.ReceiverVarDef(
                modifiers(0L),
                ident(n),
                type(n.getType())
        );
    }

    @Override
    public JCTree visit(VarType n, Void arg) {
        throw new UnsupportedOperationException("Sorry idk how to do this");
//        return imaker.VarDef()
    }

    @Override
    public JCTree visit(Modifier n, Void arg) {
        return impossible();
    }

    @Override
    public JCTree visit(SwitchExpr n, Void arg) {
        return imaker.SwitchExpression(
                transform(n.getSelector()),
                transformList(n.getEntries())
        );
    }

    @Override
    public JCTree visit(YieldStmt n, Void arg) {
        return imaker.Yield(transform(n.getExpression()));
    }

    @Override
    public JCTree visit(TextBlockLiteralExpr n, Void arg) {
        return maker.Literal(n.asString());
    }

    @Override
    public JCBindingPattern visit(TypePatternExpr n, Void arg) {
        return imaker.BindingPattern(
                maker.VarDef(
                        modifiers(n),
                        name(n),
                        type(n.getType()),
                        null
                )
        );
    }

    @Override
    public JCTree visit(RecordPatternExpr n, Void arg) {
        return impossible();//Todo idk what that in javac;

    }
}
