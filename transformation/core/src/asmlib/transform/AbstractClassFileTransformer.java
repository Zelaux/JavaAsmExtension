package asmlib.transform;

import org.jetbrains.annotations.*;

public abstract class AbstractClassFileTransformer implements ClassFileTransformer{
    public int roundLeft;
    public int roundIndex;

    public AbstractClassFileTransformer(int currentRound){
        this.roundLeft = currentRound;
    }

    @Override
    public boolean shouldRead(@NotNull String className){
        return false;
    }

    @Override
    public boolean shouldWrite(@NotNull String className){
        return false;
    }


    @Override
    public boolean needNextRound(){
        roundLeft--;
        roundIndex++;
        return roundLeft > 0;
    }

}
