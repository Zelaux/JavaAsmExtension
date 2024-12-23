package asmlib.annotations.test;

import org.junit.Assert;
import org.junit.Test;
import test.struct.TestPackable;

public class PackableTest {
    @Test
    public void name() {
        TestPackable struct = new TestPackable((byte) 10, (byte) 34);
        System.out.println(struct.pack());
        Assert.fail();
    }
}
