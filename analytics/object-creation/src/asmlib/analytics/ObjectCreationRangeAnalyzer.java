package asmlib.analytics;

import asmlib.dev.annotations.AsmVersion;
import asmlib.dev.annotations.UsedInDependentProject;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

public class ObjectCreationRangeAnalyzer {
    @NotNull
    @UsedInDependentProject
    public static ObjectCreationRanges analyze(@AsmVersion int api, MethodNode node) {
        //TODO iterable analyzing
        ObjectCreationRangeAnalyzerVisitor visitor = visitor(api);
        node.accept(visitor);
        return visitor.toResult();
    }

    @NotNull
    @UsedInDependentProject
    public static ObjectCreationRangeAnalyzerVisitor visitor(@AsmVersion int api) {
        return new ObjectCreationRangeAnalyzerVisitor(api);
    }
}
