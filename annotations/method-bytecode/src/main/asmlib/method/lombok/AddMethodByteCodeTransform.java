package asmlib.method.lombok;

import asmlib.annotations.*;
import lombok.bytecode.*;
import lombok.core.*;
import org.objectweb.asm.*;

import static org.objectweb.asm.ClassWriter.*;


public class AddMethodByteCodeTransform implements PostCompilerTransformation{
    @Override
    public byte[] applyTransformations(byte[] original, String s, DiagnosticsReceiver diagnosticsReceiver){
        ClassFileMetaData metaData = new ClassFileMetaData(original);
        if(!metaData.containsUtf8(Type.getDescriptor(ByteCode.class))) return null;

        ClassReader reader = new ClassReader(original);

        ClassWriter writer = new ClassWriter(reader, COMPUTE_FRAMES | COMPUTE_MAXS);
        reader.accept(new AddMethodByteCodeVisitor(writer), 0);
        return writer.toByteArray();
    }


}
