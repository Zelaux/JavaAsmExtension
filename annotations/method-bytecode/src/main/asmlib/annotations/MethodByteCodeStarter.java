package asmlib.annotations;


import org.jetbrains.annotations.NotNull;

public class MethodByteCodeStarter extends LombokPluginStarter{
    static{
        initSelf(MethodByteCodeStarter.class);
    }

    @Override
    public @NotNull String dontForgetToInitSelfInStatic() {
        return "ok";
    }
}
