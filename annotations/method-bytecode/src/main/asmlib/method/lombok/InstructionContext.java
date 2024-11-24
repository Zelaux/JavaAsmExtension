package asmlib.method.lombok;

import org.objectweb.asm.*;

import java.util.*;

public class InstructionContext{
    Map<String, Label> labelMap = new HashMap<>();

    public Label label(String name){
        return labelMap.computeIfAbsent(name, it -> new Label());
    }
}
