package asmlib.annotations.test.shouldcompile;

import asmlib.annotations.initializein.InitializeIn;
@InitializeIn("init")
public class ShouldCompileTest2 {

    static final Object staticFinalObj = new Object();
    static final int finalInt = 2 + 1 + 1;//x + y + color + value
    public Exception targetObject = new Exception("SomeString");
    public String noInit;
    public void init(){

    }
}
