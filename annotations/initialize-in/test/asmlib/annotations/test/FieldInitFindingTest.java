package asmlib.annotations.test;

import asmlib.util.NodeUtil;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class FieldInitFindingTest {
    @SneakyThrows
    @Test
    public void test() {
        TestCorrectInit testCorrectInit = new TestCorrectInit();
        //region start
        Assert.assertEquals(0, testCorrectInit.f4);
        Assert.assertNull(testCorrectInit.f5);

        Assert.assertNull(testCorrectInit.f2);
        Assert.assertNull(testCorrectInit.f3);

        Assert.assertNull(TestCorrectInit.f1);
        Assert.assertNull(testCorrectInit.f12);
//endregion
        testCorrectInit.init();

        //region after init
        Assert.assertEquals(1, testCorrectInit.f4);
        Assert.assertNotNull(testCorrectInit.f5);

        Assert.assertNull(testCorrectInit.f2);
        Assert.assertNull(testCorrectInit.f3);

        Assert.assertNull(TestCorrectInit.f1);
        Assert.assertNull(testCorrectInit.f12);
        //endregion
    testCorrectInit.init(13);
        //region after init(int)
        Assert.assertEquals(1, testCorrectInit.f4);
        Assert.assertNotNull(testCorrectInit.f5);

        Assert.assertNotNull(testCorrectInit.f2);
        Assert.assertNotNull(testCorrectInit.f3);

        Assert.assertNull(TestCorrectInit.f1);
        Assert.assertNull(testCorrectInit.f12);
        //endregion

        Assert.assertEquals(13,testCorrectInit.init((Integer) 23));

        //region after init(Integer)
        Assert.assertEquals(1, testCorrectInit.f4);
        Assert.assertNotNull(testCorrectInit.f5);

        Assert.assertNotNull(testCorrectInit.f2);
        Assert.assertNotNull(testCorrectInit.f3);

        Assert.assertNotNull(TestCorrectInit.f1);
        Assert.assertNotNull(testCorrectInit.f12);
        //endregion

    }

}
