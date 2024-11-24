package asmlib.annotations;

public @interface Arg{
    String value();

    AsmArgType type() default AsmArgType.trySolve;
}
