package asmlib.util;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.lang.annotation.*;
import java.util.*;

public class NodeUtil{
    public static AnnotationNode find(@Nullable List<AnnotationNode> list, Class<? extends Annotation> annotationClass){
        return find(list, Type.getDescriptor(annotationClass));
    }

    public static AnnotationNode find(@Nullable List<AnnotationNode> list, String descriptor){
        if(list == null) return null;
        for(AnnotationNode node : list){
            if(node.desc.equals(descriptor)) return node;
        }
        return null;
    }
}
