package asmlib.annotations.test;

import asmlib.annotations.initializein.AllowInstanceInitializationOfStaticFields;
import asmlib.annotations.initializein.InitializeIn;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@InitializeIn("init")
@AllowInstanceInitializationOfStaticFields
public class TestCorrectInit extends AnotherClass {


    @InitializeIn(value = "init(Integer)")
    public static Object f1 = Boolean.FALSE;
    @InitializeIn(value = "init(Integer)",pos = InitializeIn.Position.BeforeReturn)
    public Object f12 = Boolean.FALSE;
    @InitializeIn(value = "init(int)", pos = InitializeIn.Position.BeforeSuperOrHead)
    public Object f2 = function(new Object());
    @InitializeIn(value = "init(int)", pos = InitializeIn.Position.AfterSuperOrHead)
    public Object f3 = Math.random() > 0 ? Boolean.FALSE : Integer.valueOf(1);
    public int f4 = 1;
    public Float f5 = (float) (Math.PI * 177);
    public Object f6 = new Object();
    public Object f7 = new Object() {
        @Override
        public String toString() {
            return "Bruh";
        }
    };
    public Map<Integer, Integer> f8 = new HashMap<>(8888);
    public Consumer<String> f9 = string -> {
        System.out.println(string);
    };

    private static @NotNull Object function(Object it) {
        return it;
    }

    public void init() {
    }

    public int init(Integer value) {
        if (value > 10) {
            return value - 10;
        }
        if (value < -10) return value + 10;
        return value;
    }

    public void init(int value) {
        if (value == 0) {
            System.out.println("ZERO");
            super.init(value);
        } else {
            super.init(value);
        }
    }
}
