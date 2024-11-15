package lauxlang.jvm.visitor;

import org.objectweb.asm.*;
import org.objectweb.asm.util.*;

import java.io.*;

public class MyTextifier extends Textifier{
    public MyTextifier(){
        super(Opcodes.ASM9);
    }

    @Override
    public void print(PrintWriter printWriter){
        if(!stringBuilder.isEmpty()){
            printWriter.print(stringBuilder);
            stringBuilder.setLength(0);
        }
        super.print(printWriter);
        printWriter.flush();
    }

    public void clear(){
        stringBuilder.setLength(0);
        text.clear();
    }
}
