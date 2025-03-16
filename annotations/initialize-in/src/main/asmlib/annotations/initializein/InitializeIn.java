package asmlib.annotations.initializein;

import lombok.AllArgsConstructor;
import org.intellij.lang.annotations.Pattern;

import java.lang.annotation.*;

/**
 * Annotation to relocate field initialization logic into a specified initializer method.
 * This annotation can be applied to fields and types. When applied to a type, it affects all fields within that type.
 * <p>
 * This annotation simplifies code by moving field initialization into dedicated methods, improving readability
 * and organization, especially for complex initialization logic. It helps to keep constructors concise and focused
 * on object creation, while initialization details are handled separately.
 * </p>
 * Annotation to relocate field initialization logic into a specified initializer method.
 * This annotation can be applied to fields and types. When applied to a type, it affects all fields within that type.
 * <p>
 * This annotation simplifies code by moving field initialization into dedicated methods, improving readability
 * and organization, especially for complex initialization logic. It helps to keep constructors concise and focused
 * on object creation, while initialization details are handled separately.
 * </p>
 * <p><b>Example 1: Simple field initialization</b></p>
 * <pre><code>
 * public class MyClass {
 *     {@code @InitializeIn("init")}
 *     public int x = 10;
 *
 *     public void init() {
 *     }
 * }
 * </code></pre>
 * <p><b>Generated code:</b></p>
 * <pre><code>
 * public class MyClass {
 *     public int x;
 *
 *     public void init() {
 *         this.x = 10;
 *     }
 * }
 * </code></pre>
 * <p><b>Example 2: Using a method with parameters</b></p>
 * <pre><code>
 * public class MyClass {
 *     {@code @InitializeIn("init(int)")}
 *     public String s = "Hello";
 *
 *     public void init(int value) {
 *     }
 * }
 * </code></pre>
 * <p><b>Generated code:</b></p>
 * <pre><code>
 * public class MyClass {
 *     public String s;
 *
 *     public void init(int value) {
 *         this.s = "Hello";
 *     }
 * }
 * </code></pre>
 *
 *
 * @author Zelaux
 * @see InitializeIn.Position
 * @see AllowInstanceInitializationOfStaticFields
 * @see InitializeIn.Exclude
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface InitializeIn {
    /**
     * Target initializer method signature. This specifies the method where the field initialization code will be moved.
     * Supports simple method names or full method signatures, including parameter types.
     * <p>Examples:</p>
     * <ul>
     *     <li>{@code @InitializeIn("init")}</li>
     *     <li>{@code @InitializeIn("init()")} </li>
     *     <li>{@code @InitializeIn("init(String)")}</li>
     *     <li>{@code @InitializeIn("init(String, int)")}</li>
     *     <li>{@code @InitializeIn("init(java.lang.String, int)")}</li>
     * </ul>
     * @return The signature of the initializer method.
     */
    @Pattern("\\w+(\\((\\w+(\\.\\w+)*(,\\s*\\w+(\\.\\w+)*)*)?\\))?")
    String value();

    /**
     * Specifies the insertion point within the initializer method for the field initialization code.
     * Defaults to {@link Position#AfterSuperOrHead}.
     *
     * @return The insertion position.
     * @see Position
     */
    Position pos() default Position.AfterSuperOrHead;

    /**
     * Allows initialization of static fields from an instance initializer method.
     * Defaults to {@code false}. Use with caution.  This requires a corresponding
     * {@link AllowInstanceInitializationOfStaticFields} annotation on the type or field to explicitly enable this behavior.
     *
     * @return {@code true} if static fields can be initialized from an instance method, {@code false} otherwise.
     * @see AllowInstanceInitializationOfStaticFields
     */
    boolean initializeStaticFromInstanceMethod() default false;

    /**
     * Enum defining the possible insertion points for field initialization code within the target initializer method.
     */
    @AllArgsConstructor
    enum Position {
        /**
         * After superclass initializer call or at the beginning of the method if no superclass initializer is called.
         */
        AfterSuperOrHead(true, false, true),
        /**
         * After superclass initializer call or at the end of the method if no superclass initializer is called.
         */
        AfterSuperOrTail(true, false, false),
        /**
         * Before superclass initializer call or at the beginning of the method if no superclass initializer is called.
         */
        BeforeSuperOrHead(true, true, true),
        /**
         * Before superclass initializer call or at the end of the method if no superclass initializer is called.
         */
        BeforeSuperOrTail(true, true, false),
        /**
         * At the beginning of the method.
         */
        Head(false, false, true),
        /**
         * Immediately before the return statement of the method.
         */
        BeforeReturn(false, true, false);
        public final boolean relatedSuper, before, head;
    }

    /**
     * Annotation to exclude a field from being processed by the {@code InitializeIn} processor.
     * <p><b>Example:</b></p>
     * <pre><code>
     * {@code @InitializeIn("init")}
     * public class MyClass {
     *     public int x = 10;
     *     {@code @InitializeIn.Exclude}
     *     public int y = 20;
     *
     *     public void init() {}
     * }
     * </code></pre>
     * <p><b>Generated code:</b></p>
     * <pre><code>
     * public class MyClass {
     *     public int x;
     *     public int y = 20;
     *
     *     public MyClass() {}
     *
     *     public void init() {
     *         this.x = 10;
     *     }
     * }
     * </code></pre>
     */
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.CLASS)
    @interface Exclude {
    }
}
