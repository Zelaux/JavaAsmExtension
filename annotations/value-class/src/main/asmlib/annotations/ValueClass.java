package asmlib.annotations;

import lombok.core.*;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
public @interface ValueClass {
    int value() default -1;

    @interface Generated {

    }

    @interface Visited {

    }
}
