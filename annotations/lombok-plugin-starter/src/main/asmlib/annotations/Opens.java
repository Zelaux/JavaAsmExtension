package asmlib.annotations;

import org.jetbrains.annotations.*;
import sun.misc.*;

import java.lang.reflect.*;

public class Opens{

    private static Unsafe getUnsafe(){
        try{
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe)theUnsafe.get(null);
        }catch(Exception var1){
            return null;
        }
    }

    private static Object getJdkCompilerModule(ClassLoader loader){
        try{
            Class<?> cModuleLayer = findClass("java.lang.ModuleLayer", loader);
            Method mBoot = cModuleLayer.getDeclaredMethod("boot");
            Object bootLayer = mBoot.invoke(null);
            Class<?> cOptional = findClass("java.util.Optional", loader);
            Method mFindModule = cModuleLayer.getDeclaredMethod("findModule", String.class);
            Object oCompilerO = mFindModule.invoke(bootLayer, "jdk.compiler");
            return cOptional.getDeclaredMethod("get").invoke(oCompilerO);
        }catch(Exception var6){
            return null;
        }
    }

    @NotNull
    private static Class<?> findClass(String className, ClassLoader loader) throws ClassNotFoundException{
        return Class.forName(className, true, loader);
    }

    private static Object getOwnModule(){
        try{

            Method m = Permit.getMethod(Class.class, "getModule");
            return m.invoke(Opens.class);
        }catch(Exception var1){
            return null;
        }
    }


    private static long getFirstFieldOffset(Unsafe unsafe){
        try{
            return unsafe.objectFieldOffset(Parent.class.getDeclaredField("first"));
        }catch(NoSuchFieldException | SecurityException var2){
            throw new RuntimeException(var2);
        }
    }

    public static void addOpensForLombok(Class<?> anchor){
        addOpensForLombok(anchor.getClassLoader());

    }

    public static void addOpensForLombok(ClassLoader loader){
        //noinspection rawtypes
        Class cModule;
        try{
            cModule = findClass("java.lang.Module", loader);
        }catch(ClassNotFoundException var12){
            return;
        }

        Unsafe unsafe = getUnsafe();
        Object jdkCompilerModule = getJdkCompilerModule(loader);
        Object ownModule = Opens.class.getClassLoader() == loader ? getOwnModule() : loader.getUnnamedModule();
        String[] allPkgs = new String[]{"com.sun.tools.javac.code", "com.sun.tools.javac.comp", "com.sun.tools.javac.file", "com.sun.tools.javac.main", "com.sun.tools.javac.model", "com.sun.tools.javac.parser", "com.sun.tools.javac.processing", "com.sun.tools.javac.tree", "com.sun.tools.javac.util", "com.sun.tools.javac.jvm"};

        try{
            //noinspection unchecked
            Method m = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
            //noinspection DataFlowIssue
            long firstFieldOffset = getFirstFieldOffset(unsafe);
            unsafe.putBooleanVolatile(m, firstFieldOffset, true);

            for (String p : allPkgs) {
                m.invoke(jdkCompilerModule, p, ownModule);
            }
        }catch(Exception ignored){
        }
    }
}
