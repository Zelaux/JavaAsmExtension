package asmlib.annotations;

import org.objectweb.asm.*;

@SuppressWarnings("unused")
public enum AsmArgType{
    trySolve(Object.class),
    i(int.class),
    f(float.class),
    l(long.class),
    d(double.class),
    label(Label.class),
    labelArr(Label[].class),
    ;
    public final Class<?> clazz;

    AsmArgType(Class<?> clazz){
        this.clazz = clazz;
    }
}
