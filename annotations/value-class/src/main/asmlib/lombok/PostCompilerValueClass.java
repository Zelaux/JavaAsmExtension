package asmlib.lombok;

import asmlib.util.ClassFileMetaData;
import lombok.core.*;

import java.util.*;

public class PostCompilerValueClass implements PostCompilerTransformation {
    final static Map<String, ValueClassMetaData> valueClasses = new HashMap<>();

    @Override
    public byte[] applyTransformations(byte[] bytes, String s, DiagnosticsReceiver diagnosticsReceiver) {
        ClassFileMetaData metaData = new ClassFileMetaData(bytes);
        List<ValueClassMetaData> usedClasses = metaData.usedClasses()
                .map(valueClasses::get)
                .distinct()
                .filter(Objects::nonNull)
                .toList();
        if (usedClasses.isEmpty()) return bytes;

        return bytes;
    }
}
