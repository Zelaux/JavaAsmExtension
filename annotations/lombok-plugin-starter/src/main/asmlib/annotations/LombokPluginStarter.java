package asmlib.annotations;

import asmlib.util.ClassFileMetaData;
import com.sun.tools.javac.processing.*;
import lombok.*;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.*;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;

import static asmlib.annotations.Opens.*;

@SupportedAnnotationTypes("*")
public abstract class LombokPluginStarter extends AbstractProcessor {
    static {
        addOpensForLombok(LombokPluginStarter.class);
    }

    static {
        initSelf(LombokPluginStarter.class);
    }

    public LombokPluginStarter() {

        String string = dontForgetToInitSelfInStatic();
        //noinspection ConstantValue
        if (string != null && string.equals("ok")) return;
        String tmpMessage = String.format("You forgot do add '''\nstatic{\n\tinitSelf(%s.class);\n}\n\n@Override\npublic String dontForgetToInitSelfInStatic(){\n\t return \"ok\";\n}\n '''", getClass().getName());
        RuntimeException exception = new RuntimeException(tmpMessage);

        StackTraceElement[] stackTrace = exception.getStackTrace();
        StackTraceElement[] newStack = new StackTraceElement[stackTrace.length - 1];
        System.arraycopy(stackTrace, 1, newStack, 0, newStack.length);
        exception.setStackTrace(newStack);
        throw exception;
    }

    @NotNull
    private static Field getField(Class<?> delegateClass, String delegate) throws NoSuchFieldException {
        Field field = delegateClass.getDeclaredField(delegate);
        field.setAccessible(true);
        return field;
    }

    @SneakyThrows
    private static void wait0() {
        String numberString = System.getenv().getOrDefault("lombok.plugin.debug_sleep", "0");
        if (!numberString.matches("\\d")) {
            System.err.println("WARNING: env property 'lombok.plugin.debug_sleep' must contain number >=0");
            return;
        }
        int number = Integer.parseInt(numberString);
        if (number <= 0) return;
        System.out.println("Debug Sleep Start");
        for (int i = 0; i < number; i++) {
            Thread.sleep(1000);
            System.out.printf("%d/%d %n", i,number);
        }
        System.out.println("Debug Sleep End");
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    protected static void initSelf(Class<?> anchorClass) {
        if (LombokPluginStarter.class == anchorClass) wait0();
        Class<?> launchMain = anchorClass.getClassLoader().loadClass("lombok.launch.Main");


        ClassLoader shadowClassLoader = (ClassLoader) Permit.getMethod(launchMain, "getShadowClassLoader").invoke(null);
        Class<? extends ClassLoader> clazz = shadowClassLoader.getClass();

//        List<File> override = (List<File>)Permit.getField(clazz, "override").get(shadowClassLoader);
//        File SELF_BASE_FILE = (File)Permit.getField(clazz, "SELF_BASE_FILE").get(shadowClassLoader);
        Set<ClassLoader> prependedParentLoaders = (Set<ClassLoader>) Permit.getField(clazz, "prependedParentLoaders").get(shadowClassLoader);
        Map<String, Boolean> jarLocCache = (Map<String, Boolean>) Permit.getField(clazz, "jarLocCache").get(shadowClassLoader);
        ConcurrentMap<String, Class<?>> highlanderMap = (ConcurrentMap<String, Class<?>>) Permit.getField(clazz, "highlanderMap").get(null);
        List<String> highlanders = (List<String>) Permit.getField(clazz, "highlanders").get(shadowClassLoader);
//        if(override.isEmpty()) override.add(SELF_BASE_FILE);
        {

            String pathToAnchorClassJar = anchorClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

            File jarFileFile = new File(pathToAnchorClassJar);
            URL jarFileUrl = jarFileFile.toURI().toURL();
            jarLocCache.put(jarFileUrl + "::lombok", true);

            JarFile jarFile = new JarFile(jarFileFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            String directoryPath = "META-INF/services/";
            List<String> classList = new ArrayList<>();
            List<String> entryPoints = new ArrayList<>();
            Set<String> allClasses = new HashSet<>();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                String name = entry.getName();
                if (name.endsWith(".class")) {
                    allClasses.add(name.replace('/', '.').substring(0, name.length() - ".class".length()));
                }
                if (!name.startsWith(directoryPath) || entry.isDirectory()) {
                    continue;
                }
                if (!name.contains("lombok")) {

                    if (name.endsWith(Processor.class.getName())) {
                        try (BufferedReader reader = reader(jarFile, entry)) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                entryPoints.add(line);
                            }
                        }
                    }
                    continue;
                }

                try (BufferedReader reader = reader(jarFile, entry)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        classList.add(line);
                    }
                }

            }
            Set<String> loadFirst = new HashSet<>();
            for (String point : entryPoints) {
                findAllLoadFirstClasses(point, anchorClass.getClassLoader(), allClasses, loadFirst);

            }
            jarFile.close();

            LombokPluginClassLoader loader = new LombokPluginClassLoader(jarFileUrl, shadowClassLoader, loadFirst);
            addOpensForLombok(loader);


            for (String string : classList) {
                highlanders.add(string);
                Class<?> aClass = loader.loadClass(string);
                highlanderMap.put(string, aClass);
            }
            prependedParentLoaders.add(loader);
//            override.add(jarFileFile);
        }
    }

    @NotNull
    private static BufferedReader reader(JarFile jarFile, JarEntry entry) throws IOException {
        return new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)));
    }

    private static void findAllLoadFirstClasses(String className, ClassLoader classLoader, Set<String> allClasses, Set<String> loadFirst) throws IOException {
        if (!loadFirst.add(className)) return;
        ClassWriter writer = new ClassWriter(0);
        try (InputStream stream = classLoader.getResourceAsStream(className.replace('.', '/') + ".class")) {
            assert stream != null;
            new ClassReader(stream).accept(writer, 0);
        }
        ClassFileMetaData metaData = new ClassFileMetaData(writer.toByteArray());
        String[] usedClasses = metaData.usedClasses()
                .filter(allClasses::contains)
                .toArray(String[]::new);
        for (String usedClass : usedClasses) {
            findAllLoadFirstClasses(usedClass, classLoader, allClasses, loadFirst);
        }

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Class<?> type = this.getClass();
        SupportedAnnotationTypes sat = null;
        while (type != null && sat == null) {
            sat = type.getAnnotation(SupportedAnnotationTypes.class);
            type = type.getSuperclass();
        }

        boolean initialized = isInitialized();
        if (sat == null) {
            if (initialized)
                this.processingEnv.getMessager().printMessage(Kind.WARNING,
                        "No SupportedAnnotationTypes annotation " +
                        "found on " + this.getClass().getName() +
                        ", returning an empty set.");
            return Set.of();
        } else {
            boolean stripModulePrefixes =
                    initialized &&
                    this.processingEnv.getSourceVersion().compareTo(SourceVersion.RELEASE_8) <= 0;
            return this.arrayToSet(sat.value(), stripModulePrefixes,
                    "annotation type", "@SupportedAnnotationTypes");
        }
    }

    protected Set<String> arrayToSet(String[] array,
                                     boolean stripModulePrefixes,
                                     String contentType,
                                     String annotationName) {
        assert array != null;
        Set<String> set = new HashSet<>();
        for (String s : array) {
            boolean stripped = false;
            if (stripModulePrefixes) {
                int index = s.indexOf('/');
                if (index != -1) {
                    s = s.substring(index + 1);
                    stripped = true;
                }
            }
            boolean added = set.add(s);
            // Don't issue a duplicate warning when the module name is
            // stripped off to avoid spurious warnings in a case like
            // "foo/a.B", "bar/a.B".
            if (!added && !stripped && isInitialized()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                        "Duplicate " + contentType +
                        " ``" + s + "'' for processor " +
                        this.getClass().getName() +
                        " in its " + annotationName +
                        "annotation.");
            }
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        SupportedSourceVersion ssv = this.getClass().getAnnotation(SupportedSourceVersion.class);
        SourceVersion sv = null;
        if (ssv == null) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                //com.sun.tools.javac.processing.JavacProcessingEnvironment.ProcessorState.checkSourceVersionCompatibility
                String ProcessorState = "com.sun.tools.javac.processing.JavacProcessingEnvironment$ProcessorState";
                if (element.getClassName().equals(ProcessorState) && element.getMethodName().equals("checkSourceVersionCompatibility")) {
                    return SourceVersion.latestSupported();
                }
            }
            sv = SourceVersion.RELEASE_6;
        } else
            sv = ssv.value();
        return sv;
    }

    /**
     * @return "ok" string of initSelf is added
     */
    @NotNull
    @MagicConstant(stringValues = "ok")
    public String dontForgetToInitSelfInStatic() {
        return null;
    }

    /**
     * Gradle incremental processing
     */
    private Object tryGetDelegateField(Class<?> delegateClass, Object instance) {
        try {
            return getField(delegateClass, "delegate").get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    private Object tryGetProxyDelegateToField(Object instance) {
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return getField(handler.getClass(), "val$delegateTo").get(handler);
        } catch (Exception var4) {
            return null;
        }
    }

    private Object tryGetProcessingEnvField(Class<?> delegateClass, Object instance) {
        try {
            return getField(delegateClass, "processingEnv").get(instance);
        } catch (Exception var3) {
            return null;
        }
    }

    @SuppressWarnings("unused")
    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv) {
        addOpensForLombok(LombokPluginStarter.class);
        if (procEnv instanceof JavacProcessingEnvironment) return (JavacProcessingEnvironment) procEnv;

        // try to find a "delegate" field in the object, and use this to try to obtain a JavacProcessingEnvironment
        for (Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()) {
            Object delegate = tryGetDelegateField(procEnvClass, procEnv);
            if (delegate == null) delegate = tryGetProxyDelegateToField(procEnv);
            if (delegate == null) delegate = tryGetProcessingEnvField(procEnvClass, procEnv);

            if (delegate != null) return getJavacProcessingEnvironment(delegate);
            // delegate field was not found, try on superclass
        }

        processingEnv.getMessager().printMessage(Kind.WARNING,
                "Can't get the delegate of the gradle IncrementalProcessingEnvironment. Lombok won't work.");
        return null;
    }

    @SneakyThrows
    @Override
    public void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

}
