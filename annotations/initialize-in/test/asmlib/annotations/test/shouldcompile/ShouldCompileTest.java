package asmlib.annotations.test.shouldcompile;

import asmlib.annotations.initializein.InitializeIn;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class ShouldCompileTest {

    static final Object staticFinalObj = new Object();
    static final int finalInt = 2 + 1 + 1;//x + y + color + value
    @InitializeIn("init")
    public Exception targetObject = new Exception("SomeString");

    public String noInit;
    int size = 3;
    final float[] array = new float[size * size * finalInt];

    public void init() {}

    @Test
    public void test() {
        assertNotNull(staticFinalObj);
        assertEquals(4, finalInt);

        ShouldCompileTest test = new ShouldCompileTest();

        assertEquals(3,test.size);
        assertEquals(3*3*4,test.array.length);

        assertNull(test.targetObject);

        test.init();
        assertNotNull(test.targetObject);


        assertNotNull(staticFinalObj);
        assertEquals(4, finalInt);
        assertEquals(3,test.size);
        assertEquals(3*3*4,test.array.length);
    }
}
