package asmlib.annotations;

import javax.annotation.processing.SupportedAnnotationTypes;

@SupportedAnnotationTypes("asmlib.annotations.moveinit.InitializeIn")
public class MoveInitializerStarter extends LombokPluginStarter{
    static{
        initSelf(asmlib.annotations.MoveInitializerStarter.class);
    }

    @Override
    public String dontForgetToInitSelfInStatic(){
        return "ok";
    }
}
