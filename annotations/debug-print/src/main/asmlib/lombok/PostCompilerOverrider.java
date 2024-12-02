package asmlib.lombok;

import asmlib.annotations.DebugByteCode;
import com.sun.tools.javac.tree.JCTree;
import lombok.*;
import lombok.core.AnnotationValues;
import lombok.core.DiagnosticsReceiver;
import lombok.core.PostCompiler;
import lombok.core.PostCompilerTransformation;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class PostCompilerOverrider extends JavacAnnotationHandler<DebugByteCode> {

    @Getter(lazy = true)
    private static final Single init = new Single();

    @Override
    public void handle(AnnotationValues<DebugByteCode> annotationValues, JCTree.JCAnnotation jcAnnotation, JavacNode javacNode) {
        //noinspection ResultOfMethodCallIgnored
        getInit();
    }

    static class Single {
        @SneakyThrows
        public Single() {
            Field field = PostCompiler.class.getDeclaredField("transformations");
            field.setAccessible(true);

            List<PostCompilerTransformation> existed = getField(field);

            MyPostCompilerTransformation transformation;
            if (existed == null) {
                //noinspection Convert2Lambda
                transformation = new MyPostCompilerTransformation(new Function<>() {
                    @Override
                    @SneakyThrows
                    public List<PostCompilerTransformation> apply(DiagnosticsReceiver diagnostics) {
                        Object current = getField(field);
                        set(field, null);
                        Method method = PostCompiler.class.getDeclaredMethod("init", DiagnosticsReceiver.class);
                        method.setAccessible(true);
                        method.invoke(null, diagnostics);
                        List<PostCompilerTransformation> otherTransforms = getField(field);
                        ArrayList<PostCompilerTransformation> list = new ArrayList<>();

                        list.add(new DebugSaveFirstByteCode());
                        list.addAll(otherTransforms);

                        set(field, current);
                        return list;
                    }
                });
            } else {
                transformation = new MyPostCompilerTransformation(it -> existed);
            }
            set(field, List.of(transformation));
        }

        @SneakyThrows
        private static void set(Field field, Object value) {
            field.set(null, value);
        }

        @SneakyThrows
        private static <T> T getField(Field field) {
            //noinspection unchecked
            return (T) field.get(null);
        }

        public void test() {
            //Do nothing
        }

        @RequiredArgsConstructor
        private static class MyPostCompilerTransformation implements PostCompilerTransformation {

            private final Function<DiagnosticsReceiver, @NotNull @NonNull List<PostCompilerTransformation>> supplier;
            DiagnosticsReceiver currentDiagnostics;
            @SuppressWarnings("ConstantValue")
            @Getter(lazy = true)
            private final List<PostCompilerTransformation> transformations = supplier.apply(currentDiagnostics);

            @Override
            public byte[] applyTransformations(byte[] bytes, String fileName, DiagnosticsReceiver diagnostics) {
                currentDiagnostics = diagnostics;
                List<PostCompilerTransformation> transformations = getTransformations();
                byte[] previous = bytes;
                for (PostCompilerTransformation transformation : transformations) {
                    try {
                        byte[] next = transformation.applyTransformations(previous, fileName, diagnostics);
                        if (next != null) {
                            previous = next;
                        }
                    } catch (Exception e) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw, true));
                        diagnostics.addError(String.format("Error during the transformation of '%s'; post-compiler '%s' caused an exception: %s", fileName, transformation.getClass().getName(), sw));
                    }
                }
                return previous;
            }
        }
    }
}
