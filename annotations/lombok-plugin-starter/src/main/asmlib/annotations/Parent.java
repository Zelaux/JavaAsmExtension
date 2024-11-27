package asmlib.annotations;

import java.io.*;
@SuppressWarnings("ALL")
public class Parent {
    boolean first;
    static final Object staticObj = OutputStream.class;
    volatile Object second;
    private static volatile boolean staticSecond;
    private static volatile boolean staticThird;

    public Parent() {
    }
}
