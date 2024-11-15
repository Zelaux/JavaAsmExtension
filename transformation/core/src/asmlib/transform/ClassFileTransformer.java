package asmlib.transform;

import org.jetbrains.annotations.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public interface ClassFileTransformer extends Comparable<ClassFileTransformer>{
    @Override
    default int compareTo(@NotNull ClassFileTransformer o){
        return Integer.compare(priority(), o.priority());
    }

    boolean shouldRead(@NotNull String className);
    default ClassNode transformClass(ClassNode classNode){
        return classNode;
    }
    boolean shouldWrite(@NotNull String className);


    default int priority(){
        return 0;
    }
    @Nullable
    ClassVisitor createWriteVisitor( @NotNull String className, @Nullable ClassVisitor parent);

    boolean needNextRound();
    void readClass(@NotNull String className,@NotNull ClassReader reader);
}
