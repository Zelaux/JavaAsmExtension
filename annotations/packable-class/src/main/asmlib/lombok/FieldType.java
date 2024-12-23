package asmlib.lombok;

import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import static com.sun.tools.javac.code.TypeTag.*;

@RequiredArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public enum FieldType {
    Byte(BYTE, 8), Short(SHORT, 16), Int(INT, 32), Long(LONG, 64),
    Char(CHAR, Character.SIZE),
    Float("java.lang.Float.intBitsToFloat((int) (%s))",FLOAT, 32, false),
    Double("java.lang.Double.longBitsToDouble((long) (%s))",DOUBLE, 64, false),
    Boolean("(%s) != 0",BOOLEAN, 1, false);

    private static final FieldType[] mapper;

    static {
        mapper = new FieldType[TypeTag.values().length];
        for (FieldType value : values())
            mapper[value.type.ordinal()] = value;
    }

    final String mapperString;

    FieldType(TypeTag type, int size) {
        this("(" + type.name().toLowerCase() + ") (%s)",type,size);
    }

    final TypeTag type;
    final int size;
    boolean canRedefineSize;

    @Nullable
    public static FieldType forType(JCTree.JCPrimitiveTypeTree type) {
        return mapper[type.typetag.ordinal()];
    }
}
