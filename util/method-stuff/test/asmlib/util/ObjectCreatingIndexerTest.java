package asmlib.util;

import org.junit.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.net.*;

public class ObjectCreatingIndexerTest{
    @lombok.SneakyThrows
    @Test
    public void name(){
        ClassNode classNode = MethodStuff.classNode(ObjectCreatingIndexerTest.class, Opcodes.ASM9);
        MethodNode methodNode = MethodStuff.extractMethod(classNode, "methodToTest", "()V");
        GetObjectCreationMethodVisitor visitor = new GetObjectCreationMethodVisitor(Opcodes.ASM9);
        methodNode.accept(visitor);

        Assert.assertEquals(2, visitor.rootRanges.size());
        Assert.assertEquals(1, visitor.rootRanges.get(1).innerObjects.size());

    }

    @SuppressWarnings("unused")
    void methodToTest() throws URISyntaxException, FileNotFoundException{
        byte[] bytes = new byte[0];
        String string = new String(bytes);
        int i = 0;
        int i1 = 0;
        int i2 = 0;
        //noinspection resource
        FileOutputStream stream = new FileOutputStream(new File(new URI("")), true);


    }
}
