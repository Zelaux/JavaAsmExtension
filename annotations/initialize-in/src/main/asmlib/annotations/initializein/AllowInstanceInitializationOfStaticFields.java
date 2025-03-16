package asmlib.annotations.initializein;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marker annotation to explicitly allow initialization of static fields from an instance initializer method.
 * This is a safety measure to prevent accidental initialization of static fields from instance methods.
 */
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface AllowInstanceInitializationOfStaticFields {

}
