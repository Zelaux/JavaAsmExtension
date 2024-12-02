package asmlib.lombok;

import asmlib.annotations.DebugAST;
import asmlib.annotations.DebugByteCode;
import asmlib.annotations.DebugPrintStarter;
import asmlib.util.AnnotationArgumentMap;
import asmlib.util.ClassFileMetaData;
import asmlib.util.NodeUtil;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.util.Context;
import lombok.Lombok;
import lombok.SneakyThrows;
import lombok.core.AnnotationValues;
import lombok.core.DiagnosticsReceiver;
import lombok.core.HandlerPriority;
import lombok.core.PostCompilerTransformation;
import lombok.javac.JavacNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;
import java.util.Objects;

public class DebugSaveFirstByteCode implements PostCompilerTransformation {
    private static void checkByteCodeSaving(byte[] bytes) {
        ClassFileMetaData data = new ClassFileMetaData(bytes);
        if (!data.usesAnnotation(DebugByteCode.class)) return;
        ClassNode classNode = NodeUtil.classNode(bytes, Opcodes.ASM9);
        AnnotationNode found = NodeUtil.findAnnotation(classNode.visibleAnnotations, DebugByteCode.class);
        if (found == null) return;
        AnnotationArgumentMap argMap = new AnnotationArgumentMap(found);
        String outfile = Objects.requireNonNullElse(argMap.get("outfile"), "");
        if (outfile.isEmpty()) {
            classNode.accept(new TraceClassVisitor(new PrintWriter(System.out)));
            return;
        }
        File classFile = DebugPrintStarter.outputFile(null, outfile + ".class");
        try (FileOutputStream stream = new FileOutputStream(classFile)) {
            stream.write(bytes);
        } catch (IOException e) {
            throw Lombok.sneakyThrow(e);
        }
        File asmFile = DebugPrintStarter.outputFile(null, outfile + ".asm-class");
        try (FileOutputStream stream = new FileOutputStream(asmFile)) {
            classNode.accept(new TraceClassVisitor(new PrintWriter(stream)));
        } catch (IOException e) {
            throw Lombok.sneakyThrow(e);
        }

    }

    @Override
    public byte[] applyTransformations(byte[] bytes, String s, DiagnosticsReceiver diagnosticsReceiver) {
        checkByteCodeSaving(bytes);
        return bytes;
    }

}
