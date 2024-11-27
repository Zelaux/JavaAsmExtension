package asmlib.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ByteCode{

    @SuppressWarnings("unused")
    Inst[] value();
}
