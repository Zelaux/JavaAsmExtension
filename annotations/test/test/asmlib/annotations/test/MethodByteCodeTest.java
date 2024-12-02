package asmlib.annotations.test;

import org.junit.Assert;
import org.junit.Test;
import test.MethodByteCodeObject;

public class MethodByteCodeTest {
    @Test
    public void sign() {
        Assert.assertEquals(1, MethodByteCodeObject.sign(100));
        Assert.assertEquals(0, MethodByteCodeObject.sign(0));
        Assert.assertEquals(-1, MethodByteCodeObject.sign(-100));
    }

    @Test
    public void signRedirect() {
        MethodByteCodeObject object = new MethodByteCodeObject();
        assertEquals(1, object.sign2(100));
        assertEquals(0, object.sign2(0));
        assertEquals(-1, object.sign2(-100));
    }

    private void assertEquals(double expected, double actual) {
        Assert.assertEquals(expected, actual, 0.00001);
    }
}
