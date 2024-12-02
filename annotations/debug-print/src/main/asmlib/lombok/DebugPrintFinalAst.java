package asmlib.lombok;

import asmlib.annotations.DebugAST;
import asmlib.annotations.DebugPrintStarter;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.util.Context;
import lombok.SneakyThrows;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;

import java.io.PrintStream;

@HandlerPriority(1 << 29)
public class DebugPrintFinalAst extends JavacAnnotationHandler<DebugAST> {
    @SneakyThrows
    @Override
    public void handle(AnnotationValues<DebugAST> annotation, JCAnnotation jcAnnotation, JavacNode annotationNode) {
        PrintStream stream = System.out;
        DebugAST debugAST = annotation.getInstance();
        String fileName = debugAST.outfile();
        Context context = annotationNode.getAst().getContext();
        //noinspection SizeReplaceableByIsEmpty
        if (fileName.length() > 0) {
            stream = new PrintStream(DebugPrintStarter.outputFile(context, fileName));
        }

        try {
            stream.print(annotationNode.up().get().toString());
        } finally {
            if (stream != System.out) {
                stream.close();
            }

        }
    }

}
