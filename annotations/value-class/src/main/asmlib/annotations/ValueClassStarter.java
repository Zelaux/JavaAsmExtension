package asmlib.annotations;


import org.jetbrains.annotations.NotNull;

public class ValueClassStarter extends LombokPluginStarter{
    static{
        initSelf(ValueClassStarter.class);
    }

    @Override
    public @NotNull String dontForgetToInitSelfInStatic() {
        return "ok";
    }
}
