package asmlib.analytics;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UnionBasicValue extends BasicValue {
    public static final Type JAVA_LANG_OBJECT = Type.getType(Object.class);
    private final BasicValue[] types;

    private UnionBasicValue(BasicValue... types) {
        super(JAVA_LANG_OBJECT);
        Arrays.sort(types,
                Comparator.comparing(BasicValue::getType,
                        Comparator.comparingInt(Type::getSort)
                                .thenComparing(Type::getDescriptor)
                ));
        this.types = types;
    }

    public static BasicValue merge(BasicValue value1, BasicValue value2) {
        if (value1.equals(value2)) return value1;
        Stream<BasicValue> original = null;
        BasicValue otherValue = null;
        if (value1 instanceof UnionBasicValue unionBasicValue) {
            original = Arrays.stream(unionBasicValue.types);
            otherValue = value2;
        }
        if (value2 instanceof UnionBasicValue unionBasicValue) {
            if (original != null)
                return make(Stream
                        .concat(original, Arrays.stream(unionBasicValue.types)));
            original = Arrays.stream(unionBasicValue.types);
            otherValue = value1;
        }
        if (original == null) return new UnionBasicValue(value1, value2);
        return make(Stream.concat(original, Stream.of(otherValue)));
    }

    static BasicValue fromStream(Stream<BasicValue> stream) {
        return make(stream.flatMap(it -> it instanceof UnionBasicValue union ? Arrays.stream(union.types) : Stream.of(it)));
    }

    private static BasicValue make(Stream<BasicValue> stream) {
        BasicValue[] array = stream.distinct().toArray(BasicValue[]::new);
        if (array.length == 1) return array[0];
        return new UnionBasicValue(array);
    }

    public static Stream<BasicValue> unwrap(BasicValue value) {
        if (value instanceof UnionBasicValue) return Arrays.stream(((UnionBasicValue) value).types);
        return Stream.of(value);
    }

    @Override
    public boolean equals(Object value) {
        if (!(value instanceof UnionBasicValue unionBasicValue)) return false;
        return Arrays.equals(types, unionBasicValue.types);
    }

    @Override
    public String toString() {
        return Arrays.stream(types)
                .map(Object::toString)
                .collect(Collectors.joining("| ", "(", ")"));
    }
}
