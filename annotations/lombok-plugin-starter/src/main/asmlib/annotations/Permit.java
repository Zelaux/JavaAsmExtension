package asmlib.annotations;

import lombok.permit.Permit.*;
import sun.misc.*;

import java.lang.reflect.*;

public class Permit {
    private static final long ACCESSIBLE_OVERRIDE_FIELD_OFFSET;
    private static final IllegalAccessException INIT_ERROR;
    private static final Unsafe UNSAFE = (Unsafe)reflectiveStaticFieldAccess(Unsafe.class, "theUnsafe");

    static {
        long g;
        Throwable ex;
        try {
            g = getOverrideFieldOffset();
            ex = null;
        } catch (Throwable var4) {
            Object var10000 = null;
            g = -1L;
            ex = var4;
        }

        ACCESSIBLE_OVERRIDE_FIELD_OFFSET = g;
        if (ex == null) {
            INIT_ERROR = null;
        } else if (ex instanceof IllegalAccessException) {
            INIT_ERROR = (IllegalAccessException)ex;
        } else {
            INIT_ERROR = new IllegalAccessException("Cannot initialize Unsafe-based permit");
            INIT_ERROR.initCause(ex);
        }

    }

    private Permit() {
    }

    public static <T extends AccessibleObject> T setAccessible(T accessor) {
        if (INIT_ERROR == null) {
            UNSAFE.putBoolean(accessor, ACCESSIBLE_OVERRIDE_FIELD_OFFSET, true);
        } else {
            accessor.setAccessible(true);
        }

        return accessor;
    }

    private static long getOverrideFieldOffset() throws Throwable {
        Field f = null;
        Throwable saved = null;

        try {
            f = AccessibleObject.class.getDeclaredField("override");
        } catch (Throwable var4) {
            saved = var4;
        }

        if (f != null) {
            return UNSAFE.objectFieldOffset(f);
        } else {
            try {
                return UNSAFE.objectFieldOffset(Fake.class.getDeclaredField("override"));
            } catch (Throwable var3) {
                throw saved;
            }
        }
    }

    public static Method getMethod(Class<?> c, String mName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method m = null;
        Class<?> oc = c;

        while(c != null) {
            try {
                m = c.getDeclaredMethod(mName, parameterTypes);
                break;
            } catch (NoSuchMethodException var5) {
                c = c.getSuperclass();
            }
        }

        if (m == null) {
            throw new NoSuchMethodException(oc.getName() + " :: " + mName + "(args)");
        } else {
            return (Method)setAccessible(m);
        }
    }

    public static Method permissiveGetMethod(Class<?> c, String mName, Class<?>... parameterTypes) {
        try {
            return getMethod(c, mName, parameterTypes);
        } catch (Exception var3) {
            return null;
        }
    }

    public static Field getField(Class<?> c, String fName) throws NoSuchFieldException {
        Field f = null;
        Class<?> oc = c;

        while(c != null) {
            try {
                f = c.getDeclaredField(fName);
                break;
            } catch (NoSuchFieldException var4) {
                c = c.getSuperclass();
            }
        }

        if (f == null) {
            throw new NoSuchFieldException(oc.getName() + " :: " + fName);
        } else {
            return (Field)setAccessible(f);
        }
    }

    public static Field permissiveGetField(Class<?> c, String fName) {
        try {
            return getField(c, fName);
        } catch (Exception var2) {
            return null;
        }
    }

    public static <T> T permissiveReadField(Class<T> type, Field f, Object instance) {
        try {
            return type.cast(f.get(instance));
        } catch (Exception var3) {
            return null;
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> c, Class<?>... parameterTypes) throws NoSuchMethodException {
        return (Constructor)setAccessible(c.getDeclaredConstructor(parameterTypes));
    }

    private static Object reflectiveStaticFieldAccess(Class<?> c, String fName) {
        try {
            Field f = c.getDeclaredField(fName);
            f.setAccessible(true);
            return f.get((Object)null);
        } catch (Exception var3) {
            return null;
        }
    }

    public static boolean isDebugReflection() {
        return !"false".equals(System.getProperty("lombok.debug.reflection", "false"));
    }

    public static void handleReflectionDebug(Throwable t, Throwable initError) {
        if (isDebugReflection()) {
            System.err.println("** LOMBOK REFLECTION exception: " + t.getClass() + ": " + (t.getMessage() == null ? "(no message)" : t.getMessage()));
            t.printStackTrace(System.err);
            if (initError != null) {
                System.err.println("*** ADDITIONALLY, exception occurred setting up reflection: ");
                initError.printStackTrace(System.err);
            }

        }
    }

    public static Object invoke(Method m, Object receiver, Object... args) throws IllegalAccessException, InvocationTargetException {
        return invoke((Throwable)null, m, receiver, args);
    }

    public static Object invoke(Throwable initError, Method m, Object receiver, Object... args) throws IllegalAccessException, InvocationTargetException {
        try {
            return m.invoke(receiver, args);
        } catch (IllegalAccessException var5) {
            handleReflectionDebug(var5, initError);
            throw var5;
        } catch (RuntimeException var6) {
            handleReflectionDebug(var6, initError);
            throw var6;
        } catch (Error var7) {
            handleReflectionDebug(var7, initError);
            throw var7;
        }
    }

    public static Object invokeSneaky(Method m, Object receiver, Object... args) {
        return invokeSneaky((Throwable)null, m, receiver, args);
    }

    public static Object invokeSneaky(Throwable initError, Method m, Object receiver, Object... args) {
        try {
            return m.invoke(receiver, args);
        } catch (NoClassDefFoundError var5) {
            handleReflectionDebug(var5, initError);
            return null;
        } catch (NullPointerException var6) {
            handleReflectionDebug(var6, initError);
            return null;
        } catch (IllegalAccessException var7) {
            handleReflectionDebug(var7, initError);
            throw sneakyThrow(var7);
        } catch (InvocationTargetException var8) {
            throw sneakyThrow(var8.getCause());
        } catch (RuntimeException var9) {
            handleReflectionDebug(var9, initError);
            throw var9;
        } catch (Error var10) {
            handleReflectionDebug(var10, initError);
            throw var10;
        }
    }

    public static <T> T newInstance(Constructor<T> c, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return newInstance((Throwable)null, c, args);
    }

    public static <T> T newInstance(Throwable initError, Constructor<T> c, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return c.newInstance(args);
        } catch (IllegalAccessException var4) {
            handleReflectionDebug(var4, initError);
            throw var4;
        } catch (InstantiationException var5) {
            handleReflectionDebug(var5, initError);
            throw var5;
        } catch (RuntimeException var6) {
            handleReflectionDebug(var6, initError);
            throw var6;
        } catch (Error var7) {
            handleReflectionDebug(var7, initError);
            throw var7;
        }
    }

    public static <T> T newInstanceSneaky(Constructor<T> c, Object... args) {
        return newInstanceSneaky((Throwable)null, c, args);
    }

    public static <T> T newInstanceSneaky(Throwable initError, Constructor<T> c, Object... args) {
        try {
            return c.newInstance(args);
        } catch (NoClassDefFoundError var4) {
            handleReflectionDebug(var4, initError);
            return null;
        } catch (NullPointerException var5) {
            handleReflectionDebug(var5, initError);
            return null;
        } catch (IllegalAccessException var6) {
            handleReflectionDebug(var6, initError);
            throw sneakyThrow(var6);
        } catch (InstantiationException var7) {
            handleReflectionDebug(var7, initError);
            throw sneakyThrow(var7);
        } catch (InvocationTargetException var8) {
            throw sneakyThrow(var8.getCause());
        } catch (RuntimeException var9) {
            handleReflectionDebug(var9, initError);
            throw var9;
        } catch (Error var10) {
            handleReflectionDebug(var10, initError);
            throw var10;
        }
    }

    public static <T> T get(Field f, Object receiver) throws IllegalAccessException {
        try {
            return (T)f.get(receiver);
        } catch (IllegalAccessException var3) {
            handleReflectionDebug(var3, (Throwable)null);
            throw var3;
        } catch (RuntimeException var4) {
            handleReflectionDebug(var4, (Throwable)null);
            throw var4;
        } catch (Error var5) {
            handleReflectionDebug(var5, (Throwable)null);
            throw var5;
        }
    }

    public static void set(Field f, Object receiver, Object newValue) throws IllegalAccessException {
        try {
            f.set(receiver, newValue);
        } catch (IllegalAccessException var4) {
            handleReflectionDebug(var4, (Throwable)null);
            throw var4;
        } catch (RuntimeException var5) {
            handleReflectionDebug(var5, (Throwable)null);
            throw var5;
        } catch (Error var6) {
            handleReflectionDebug(var6, (Throwable)null);
            throw var6;
        }
    }

    public static void reportReflectionProblem(Throwable initError, String msg) {
        if (isDebugReflection()) {
            System.err.println("** LOMBOK REFLECTION issue: " + msg);
            if (initError != null) {
                System.err.println("*** ADDITIONALLY, exception occurred setting up reflection: ");
                initError.printStackTrace(System.err);
            }

        }
    }

    public static RuntimeException sneakyThrow(Throwable t) {
        if (t == null) {
            throw new NullPointerException("t");
        } else {
            return (RuntimeException)sneakyThrow0(t);
        }
    }

    private static <T extends Throwable> T sneakyThrow0(Throwable t) throws T {
        //noinspection unchecked
        throw (T)t;
    }

    static class Fake {
        boolean override;
        Object accessCheckCache;

        Fake() {
        }
    }
}