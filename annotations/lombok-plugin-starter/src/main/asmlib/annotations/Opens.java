package asmlib.annotations;

import org.jetbrains.annotations.*;
import sun.misc.*;

import java.lang.reflect.*;

public class Opens{

    private static Unsafe getUnsafe(){
        try{
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe)theUnsafe.get((Object)null);
        }catch(Exception var1){
            return null;
        }
    }

    private static Object getJdkCompilerModule(ClassLoader loader){
        try{
            Class<?> cModuleLayer = findClass("java.lang.ModuleLayer", loader);
            Method mBoot = cModuleLayer.getDeclaredMethod("boot");
            Object bootLayer = mBoot.invoke((Object)null);
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

            Method m = Permit.getMethod(Class.class, "getModule", new Class[0]);
            return m.invoke(Opens.class);
        }catch(Exception var1){
            return null;
        }
    }


    private static long getFirstFieldOffset(Unsafe unsafe){
        try{
            return unsafe.objectFieldOffset(Parent.class.getDeclaredField("first"));
        }catch(NoSuchFieldException var2){
            throw new RuntimeException(var2);
        }catch(SecurityException var3){
            throw new RuntimeException(var3);
        }
    }

    public static void addOpensForLombok(Class<?> anchor){
        addOpensForLombok(anchor.getClassLoader());

    }

    public static void addOpensForLombok(ClassLoader loader){
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
            Method m = cModule.getDeclaredMethod("implAddOpens", String.class, cModule);
            long firstFieldOffset = getFirstFieldOffset(unsafe);
            unsafe.putBooleanVolatile(m, firstFieldOffset, true);
            String[] var11 = allPkgs;
            int var10 = allPkgs.length;

            for(int var9 = 0; var9 < var10; ++var9){
                String p = var11[var9];
                m.invoke(jdkCompilerModule, p, ownModule);
            }
        }catch(Exception var13){
        }
    }
}
