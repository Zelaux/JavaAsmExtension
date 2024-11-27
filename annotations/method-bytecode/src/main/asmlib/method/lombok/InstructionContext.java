package asmlib.method.lombok;

import org.objectweb.asm.*;

import java.util.*;

public class InstructionContext {
    final Map<String, Label> labelMap = new HashMap<>();

    public void reset() {
        labelMap.clear();
    }

    public Label label(String name) {
        return labelMap.computeIfAbsent(name, it -> new Label());
    }
}
