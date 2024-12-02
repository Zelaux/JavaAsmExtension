package asmlib.util;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.*;

import java.util.*;

public class AnnotationArgumentMap {

    private final Map<String, Object> map = new HashMap<>();
    private boolean isNull;

    public boolean isNull() {
        return isNull;
    }

    public AnnotationArgumentMap(@Nullable List<Object> values) {
        values(values);
    }

    public AnnotationArgumentMap(AnnotationNode node) {
        this(node.values);
    }

    @SuppressWarnings("UnusedReturnValue")
    public AnnotationArgumentMap values(@Nullable List<Object> values) {
        map.clear();
        isNull = values == null;
        if (isNull) return this;
        for (int i = 0; i < values.size(); i += 2) {
            map.put(values.get(i).toString(), values.get(i + 1));
        }

        return this;
    }

    @SuppressWarnings("unused")
    public Set<String> keys() {
        return map.keySet();
    }

    public <T> T get(String name) {
        //noinspection unchecked
        return (T) map.get(name);
    }

    public List<AnnotationArgumentMap> getList(String name) {
        List<AnnotationNode> o = get(name);
        return o.stream().map(AnnotationArgumentMap::new).toList();
    }

    public boolean has(String name) {
        return map.containsKey(name);
    }
}
