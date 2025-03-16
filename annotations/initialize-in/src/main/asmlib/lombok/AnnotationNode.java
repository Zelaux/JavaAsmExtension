package asmlib.lombok;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.core.AnnotationValues;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacNode;

import java.lang.annotation.Annotation;


@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class AnnotationNode<T extends Annotation> {
    public static final AnnotationNode<?> zero = new AnnotationNode<>(null, null);
    AnnotationValues<T> values;
    JavacNode annotationJavacNode;
    ;

    public static <T extends Annotation> AnnotationNode<T> nil() {
        //noinspection unchecked
        return (AnnotationNode<T>) zero;
    }

    public boolean isNull() {
        return values == null || annotationJavacNode == null;
    }

    public T tryCreateAnno(T def) {
        return values == null ? def : values.getInstance();
    }

    public boolean nonNull() {
        return !isNull();
    }
}
