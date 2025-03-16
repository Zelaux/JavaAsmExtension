package asmlib.lombok;

import asmlib.annotations.initializein.InitializeIn;
import lombok.javac.JavacNode;
import org.jetbrains.annotations.Contract;

public class JavacNodeError extends Exception {
    @Contract(pure = true)
    public JavacNodeError(String message, Object... args) {
        super(String.format(message,args));
    }
    public static final String MoveInitializerName= InitializeIn.class.getSimpleName();
    public void handle(JavacNode node) {
        node.addError(
                String.format(
                        "%s: %s",MoveInitializerName,getMessage()
                )
        );
    }
}
