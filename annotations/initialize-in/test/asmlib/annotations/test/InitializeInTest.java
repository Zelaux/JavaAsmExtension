package asmlib.annotations.test;

import asmlib.annotations.initializein.AllowInstanceInitializationOfStaticFields;
import asmlib.annotations.initializein.InitializeIn;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class InitializeInTest {

    @Test
    void simpleFieldInitialization() {
        SimpleInit simpleInit = new SimpleInit();
        assertEquals(0, simpleInit.x);
        simpleInit.init();
        assertEquals(10, simpleInit.x);
    }


    @Test
    void methodWithParameters() {
        MethodWithParams mwParams = new MethodWithParams();
        assertNull(mwParams.s);
        mwParams.init(5);
        assertEquals("Hello 55", mwParams.s);

    }


    @Test
    void excludeField() {
        ExcludeField excludeField = new ExcludeField();

        assertEquals(0, excludeField.x);
        assertEquals(20, excludeField.y); // This field should not be initialized by the init() method
        excludeField.init();
        assertEquals(10, excludeField.x);
        assertEquals(20, excludeField.y); // This field should not be initialized by the init() method
    }


    @Test
    void initializeStaticFromInstanceMethod() {
        StaticFromInstance staticFromInstance = new StaticFromInstance();
        assertEquals(0, StaticFromInstance.staticVar);
        staticFromInstance.init();
        assertEquals(100, StaticFromInstance.staticVar);

    }

    @Test
    void testPositionBeforeSuperOrHead() {
        BeforeSuperOrHeadTest test = new BeforeSuperOrHeadTest();
        assertEquals("Initial value", test.value);
        assertNull(test.testValue);
        test.init();
        assertNull(test.value);
        assertEquals("Value set BEFORE super", test.testValue);

    }

    @Test
    void testBeforeReturn() {
        BeforeReturnTest test = new BeforeReturnTest();

        assertEquals(0, test.list.size());
        assertEquals(3, test.init());
        assertEquals(List.of(-1, -2, -3, -4, -5), test.list);
    }

    @Test
    void testNativeInstance() {
        var test = new NativeMethod();
        assertEquals(0, test.x);
        test.init();
        assertEquals(10, test.x);
    }
    @Test
    void testNativeStatic() {
        assertEquals(0, NativeStaticMethod.x);
        NativeStaticMethod.init();
        assertEquals(10, NativeStaticMethod.x);
    }

    @InitializeIn("init")
    static class SimpleInit {
        public int x = 10;

        public void init() {
        }
    }

    @InitializeIn("init(int)")
    static class MethodWithParams {
        public String s = "Hello " + "5";

        public void init(int value) {
            s+=value;
        }
    }

    @InitializeIn("init")
    static class ExcludeField {
        public int x = 10;
        @InitializeIn.Exclude
        public int y = 20;

        public void init() {

        }
    }
    @InitializeIn("init")
    static class NativeMethod {
        public int x = 10;
        public native void init();
    }
    @InitializeIn("init")
    static class NativeStaticMethod {
        public static int x = 10;
        public static native void init();
    }


    @InitializeIn("init")
    @AllowInstanceInitializationOfStaticFields
    static class StaticFromInstance {
        public static int staticVar = 100;

        public void init() {
        }
    }

    static class BaseClassPositionTest {
        String value = "Initial value";

        public void init() {
        }
    }

    @InitializeIn(value = "init", pos = InitializeIn.Position.BeforeSuperOrHead)
    static class BeforeSuperOrHeadTest extends BaseClassPositionTest {
        String testValue = "Value set BEFORE super";

        public void init() {
            value = testValue;
            super.init();
        }
    }


    @SuppressWarnings("UnusedAssignment")
    @InitializeIn(value = "init", pos = InitializeIn.Position.BeforeReturn)
    static class BeforeReturnTest {
        List<Integer> list = new ArrayList<>(List.of(
                -1, -2, -3, -4, -5
        ));

        {
            list = new ArrayList<>();
        }

        public int init() {
            list.add(1);
            list.add(2);
            list.add(3);

            return list.size();

        }

    }
}