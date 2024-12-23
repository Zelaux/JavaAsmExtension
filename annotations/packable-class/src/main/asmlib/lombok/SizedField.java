package asmlib.lombok;

import asmlib.annotations.Packable;
import com.sun.tools.javac.tree.JCTree;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacNode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter
public class SizedField {

    String name;
    @NonNull
    FieldType type;
    @Nullable
    Packable.Field fieldAnnotation;
    JCTree.JCVariableDecl declaration;
    JavacNode node;
    private int[] offset = {-1};
    private final String[] mask = {null};

    public int actualSize() {
        if (!type.canRedefineSize) return type.size;
        if (fieldAnnotation == null) return type.size;
        int size = fieldAnnotation.size();
        if (size == -1) return type.size;
        return size;
    }

    public String mask() {
        if (mask[0] == null) {
            byte[] chars = new byte[actualSize()];
            Arrays.fill(chars, (byte) '1');
            mask[0] = "0b" + new String(chars, StandardCharsets.US_ASCII);
        }
        return mask[0];
    }

    public void offset(@Range(from = 0, to = Integer.MAX_VALUE) int offset) {
        //noinspection ConstantValue
        if (offset < 0) throw new IllegalArgumentException("offset must be >= 0");
        this.offset[0]=offset;
    }

    public int offset() {
        if (offset[0] == -1) {
            throw new RuntimeException("SizedField is not initialized");
        }
        return offset[0];
    }
}
