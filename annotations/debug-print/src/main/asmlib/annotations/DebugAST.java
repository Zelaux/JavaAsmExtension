package asmlib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * Prints AST before compilation(after all transformations)
 * */
@Retention(RetentionPolicy.RUNTIME)
public @interface DebugAST {
    String outfile() default "";
}
