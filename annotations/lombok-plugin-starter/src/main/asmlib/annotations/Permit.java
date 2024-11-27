package asmlib.annotations;

import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.lang.reflect.*;

@SuppressWarnings("unused")
public class Permit {
    private static final long ACCESSIBLE_OVERRIDE_FIELD_OFFSET;
    private static final IllegalAccessException INIT_ERROR;
    @SuppressWarnings("DataFlowIssue")
    @NotNull
    private static final Unsafe UNSAFE = (Unsafe)reflectiveStaticFieldAccess(Unsafe.class, "theUnsafe");

    static {
        long g;
        Throwable ex;
        try {
            g = getOverrideFieldOffset();
            ex = null;
        } catch (Throwable throwable) {
            g = -1L;
            ex = throwable;
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

    public static Method getMethod(@NotNull Class<?> c, String mName, Class<?>... parameterTypes) throws NoSuchMethodException {
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
            return setAccessible(m);
        }
    }

    public static Method permissiveGetMethod(Class<?> c, String mName, Class<?>... parameterTypes) {
        try {
            return getMethod(c, mName, parameterTypes);
        } catch (Exception var3) {
            return null;
        }
    }

    public static Field getField(@NotNull Class<?> c, String fName) throws NoSuchFieldException {
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
            return setAccessible(f);
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
        return setAccessible(c.getDeclaredConstructor(parameterTypes));
    }

    @SuppressWarnings("SameParameterValue")
    private static Object reflectiveStaticFieldAccess(Class<?> c, String fName) {
        try {
            Field f = c.getDeclaredField(fName);
            f.setAccessible(true);
            return f.get(null);
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
        return invoke(null, m, receiver, args);
    }

    public static Object invoke(Throwable initError, Method m, Object receiver, Object... args) throws IllegalAccessException, InvocationTargetException {
        try {
            return m.invoke(receiver, args);
        } catch (IllegalAccessException | RuntimeException | Error e) {
            handleReflectionDebug(e, initError);
            throw e;
        }
    }

    public static Object invokeSneaky(Method m, Object receiver, Object... args) {
        return invokeSneaky(null, m, receiver, args);
    }

    public static Object invokeSneaky(Throwable initError, Method m, Object receiver, Object... args) {
        try {
            return m.invoke(receiver, args);
        } catch (NoClassDefFoundError | NullPointerException e) {
            handleReflectionDebug(e, initError);
            return null;

        } catch (IllegalAccessException e) {
            handleReflectionDebug(e, initError);
            throw sneakyThrow(e);

        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getCause());

        } catch (RuntimeException | Error e) {
            handleReflectionDebug(e, initError);
            throw e;
        }
    }

    public static <T> T newInstance(Constructor<T> c, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return newInstance(null, c, args);
    }

    public static <T> T newInstance(Throwable initError, Constructor<T> c, Object... args) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        try {
            return c.newInstance(args);
        } catch (IllegalAccessException | InstantiationException | RuntimeException | Error e) {
            handleReflectionDebug(e, initError);
            throw e;
        }
    }

    public static <T> T newInstanceSneaky(Constructor<T> c, Object... args) {
        return newInstanceSneaky(null, c, args);
    }

    public static <T> T newInstanceSneaky(Throwable initError, Constructor<T> c, Object... args) {
        try {
            return c.newInstance(args);
        } catch (NoClassDefFoundError | NullPointerException e) {
            handleReflectionDebug(e, initError);
            return null;
        } catch (IllegalAccessException | InstantiationException e) {
            handleReflectionDebug(e, initError);
            throw sneakyThrow(e);
        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getCause());
        } catch (RuntimeException | Error e) {
            handleReflectionDebug(e, initError);
            throw e;
        }
    }

    public static <T> T get(Field f, Object receiver) throws IllegalAccessException {
        try {
            //noinspection unchecked
            return (T)f.get(receiver);
        } catch (IllegalAccessException | RuntimeException | Error e) {
            handleReflectionDebug(e, null);
            throw e;
        }
    }

    public static void set(Field f, Object receiver, Object newValue) throws IllegalAccessException {
        try {
            f.set(receiver, newValue);
        } catch (IllegalAccessException | RuntimeException | Error e) {
            handleReflectionDebug(e, null);
            throw e;
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
            return sneakyThrow0(t);
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
