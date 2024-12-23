package asmlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Packable {
    FieldSort sort() default FieldSort.noSort;
    @Target(ElementType.METHOD)
    @interface Pack {

    }

    @Target(ElementType.METHOD)
    @interface Unpack {
    }

    @Target(ElementType.FIELD)
    @interface Field {
        int size() default -1;

        boolean ignore() default false;
    }
}
