package asmlib.annotations;

import org.jetbrains.annotations.NotNull;

public class PackableStarter extends LombokPluginStarter{
    static{
        initSelf(asmlib.annotations.PackableStarter.class);
    }

    @Override
    public @NotNull String dontForgetToInitSelfInStatic(){
        return "ok";
    }
}
