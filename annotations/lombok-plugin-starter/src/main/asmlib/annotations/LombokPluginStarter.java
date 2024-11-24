package asmlib.annotations;

import com.sun.tools.javac.processing.*;
import lombok.*;
import lombok.bytecode.*;
import org.jetbrains.annotations.*;
import org.objectweb.asm.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.*;
import java.io.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.*;
import java.util.regex.*;

import static asmlib.annotations.Opens.addOpensForLombok;

@SupportedAnnotationTypes("*")
public class LombokPluginStarter extends AbstractProcessor{
    public static final String lombokShadowJarSavePrefix = "$";
    public static final String lombokShadowJarSavePostfix = "$";

    static{
        addOpensForLombok(LombokPluginStarter.class);
    }

    static{
        initSelf(LombokPluginStarter.class);
    }

    private static <T> T get(Class<? extends ClassLoader> clazz, Object this$, String name) throws NoSuchFieldException, IllegalAccessException{
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return (T)field.get(this$);
    }

    @NotNull
    private static Field getField(Class<?> delegateClass, String delegate) throws NoSuchFieldException{
        Field field = delegateClass.getDeclaredField(delegate);
        field.setAccessible(true);
        return field;
    }

    @SneakyThrows
    private static void wait0(){
        System.out.println("Sleep");
        for(int i = 0; i < 5; i++){
            Thread.sleep(1000);
            System.out.println(i);
        }
        System.out.println("Sleep2");
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    protected static void initSelf(Class<?> anchorClass){
        if(LombokPluginStarter.class == anchorClass) wait0();
        Class<?> launchMain = anchorClass.getClassLoader().loadClass("lombok.launch.Main");


        ClassLoader shadowClassLoader = (ClassLoader)Permit.getMethod(launchMain, "getShadowClassLoader").invoke(null);
        Class<? extends ClassLoader> clazz = shadowClassLoader.getClass();

        List<File> override = (List<File>)Permit.getField(clazz, "override").get(shadowClassLoader);
//        File SELF_BASE_FILE = (File)Permit.getField(clazz, "SELF_BASE_FILE").get(shadowClassLoader);
        Set<ClassLoader> prependedParentLoaders = (Set<ClassLoader>)Permit.getField(clazz, "prependedParentLoaders").get(shadowClassLoader);
        Map<String, Boolean> jarLocCache = (Map<String, Boolean>)Permit.getField(clazz, "jarLocCache").get(shadowClassLoader);
        ConcurrentMap<String, Class<?>> highlanderMap = (ConcurrentMap<String, Class<?>>)Permit.getField(clazz, "highlanderMap").get(null);
        List<String> highlanders = (List<String>)Permit.getField(clazz, "highlanders").get(shadowClassLoader);
//        if(override.isEmpty()) override.add(SELF_BASE_FILE);
        {

            // Получаем путь к текущему классу
            String path = anchorClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            // Создаем объект File для получения абсолютного пути
            File jarFileFile = new File(path);
            URL jarFileUrl = jarFileFile.toURI().toURL();
            jarLocCache.put(jarFileUrl + "::lombok", true);

            MyURLClassLoader loader = new MyURLClassLoader(anchorClass, jarFileUrl, shadowClassLoader);
            addOpensForLombok(loader);
            JarFile jarFile = new JarFile(jarFileFile);
            Enumeration<JarEntry> entries = jarFile.entries();
            String directoryPath = "META-INF/services/";
            List<String> classList = new ArrayList<>();
            while(entries.hasMoreElements()){
                JarEntry entry = entries.nextElement();
                // Проверяем, что это файл и он находится в нужной директории
                String name = entry.getName();
                if(!name.startsWith(directoryPath) || entry.isDirectory() || !name.contains("lombok")) continue;
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(entry)))){
                    String line;
                    while((line = reader.readLine()) != null){
                        classList.add(line);
                    }
                }
            }
            jarFile.close();
            for(String string : classList){
                highlanders.add(string);
                Class<?> aClass = loader.findClass(string);
                highlanderMap.put(string, aClass);
            }
            prependedParentLoaders.add(loader);
//            override.add(jarFileFile);
        }
    }

    /**
     * Gradle incremental processing
     */
    private Object tryGetDelegateField(Class<?> delegateClass, Object instance){
        try{
            return getField(delegateClass, "delegate").get(instance);
        }catch(Exception e){
            return null;
        }
    }

    private Object tryGetProxyDelegateToField(Class<?> delegateClass, Object instance){
        try{
            InvocationHandler handler = Proxy.getInvocationHandler(instance);
            return getField(handler.getClass(), "val$delegateTo").get(handler);
        }catch(Exception var4){
            return null;
        }
    }

    private Object tryGetProcessingEnvField(Class<?> delegateClass, Object instance){
        try{
            return getField(delegateClass, "processingEnv").get(instance);
        }catch(Exception var3){
            return null;
        }
    }

    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv){
        addOpensForLombok(LombokPluginStarter.class);
        if(procEnv instanceof JavacProcessingEnvironment) return (JavacProcessingEnvironment)procEnv;

        // try to find a "delegate" field in the object, and use this to try to obtain a JavacProcessingEnvironment
        for(Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()){
            Object delegate = tryGetDelegateField(procEnvClass, procEnv);
            if(delegate == null) delegate = tryGetProxyDelegateToField(procEnvClass, procEnv);
            if(delegate == null) delegate = tryGetProcessingEnvField(procEnvClass, procEnv);

            if(delegate != null) return getJavacProcessingEnvironment(delegate);
            // delegate field was not found, try on superclass
        }

        processingEnv.getMessager().printMessage(Kind.WARNING,
                                                 "Can't get the delegate of the gradle IncrementalProcessingEnvironment. Lombok won't work.");
        return null;
    }

    @SneakyThrows
    @Override
    public void init(ProcessingEnvironment procEnv){
        super.init(procEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        return false;
    }

    private static class MyURLClassLoader extends URLClassLoader{
        final List<String> arrayList;
        private final ClassLoader shadowClassLoader;
        private final ClassLoader parentLoader;

        public MyURLClassLoader(Class<?> anchorClass, URL jarFileUrl, ClassLoader shadowClassLoader){
            super( new URL[]{jarFileUrl}, null);
            arrayList = new ArrayList<>();
            this.shadowClassLoader = shadowClassLoader;
            this.parentLoader = MyURLClassLoader.class.getClassLoader();
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException{
            return super.findClass(name);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException{
            if(arrayList.contains(name)) throw new ClassNotFoundException(name);
            arrayList.add(name);
            try{
                Class<?> aClass = null;
                try{
                    aClass = super.loadClass(name);
                }catch(ClassNotFoundException e){

                    if(name.startsWith("org.objectweb.asm")){
                        return parentLoader.loadClass(name);
                    }
                    try{
                        return shadowClassLoader.loadClass(name);
                    }catch(ClassNotFoundException ex){
                        return parentLoader.loadClass(name);
                    }
                }
                return aClass;
            }finally{
                arrayList.remove(name);
            }
        }
    }
}
