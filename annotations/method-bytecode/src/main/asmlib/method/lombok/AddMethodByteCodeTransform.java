package asmlib.method.lombok;

import asmlib.annotations.*;
import asmlib.util.ClassFileMetaData;
import lombok.bytecode.*;
import lombok.core.*;
import org.objectweb.asm.*;

import java.util.Set;
import java.util.stream.Collectors;

import static org.objectweb.asm.ClassWriter.*;


public class AddMethodByteCodeTransform implements PostCompilerTransformation {
    @Override
    public byte[] applyTransformations(byte[] original, String s, DiagnosticsReceiver diagnosticsReceiver) {
        ClassFileMetaData metaData = new ClassFileMetaData(original);
        if (!metaData.containsUtf8(Type.getDescriptor(ByteCode.class))) return null;

        ClassReader reader = new ClassReader(original);

        ClassWriter writer = new ClassWriter(reader, COMPUTE_FRAMES | COMPUTE_MAXS);
        reader.accept(new AddMethodByteCodeVisitor(writer), 0);
        byte[] byteArray = writer.toByteArray();
        /*$ClassFileMetaData$ metaData2 = new $ClassFileMetaData$(byteArray);
        Set<String> set = metaData.usedClasses().collect(Collectors.toSet());
        for (String string : metaData2.usedClasses().filter(set::add).toList()) {

        }*/
        return byteArray;
    }


}
