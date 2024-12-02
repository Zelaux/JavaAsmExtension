package asmlib.annotations;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class LombokPluginClassLoader extends URLClassLoader {
    final List<String> arrayList;
    private final ClassLoader shadowClassLoader;
    private final ClassLoader parentLoader;
    private final Module myModule;
    private final Module parentModule;
    private final Set<String> parentLoad;

    public LombokPluginClassLoader(URL jarFileUrl, ClassLoader shadowClassLoader, Set<String> parentLoad) {
        super(new URL[]{jarFileUrl}, null);
        this.parentLoad = parentLoad;
        arrayList = new ArrayList<>();
        this.shadowClassLoader = shadowClassLoader;
        this.parentLoader = LombokPluginClassLoader.class.getClassLoader();
        this.myModule = getUnnamedModule();
        this.parentModule = parentLoader.getUnnamedModule();

    }


    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (arrayList.contains(name)) throw new ClassNotFoundException(name);
        if (parentLoad.contains(name)) return loadParent(name);
        arrayList.add(name);
        try {
            try {
                return findClass(name);
            } catch (ClassNotFoundException ignored) {
            }
            if (name.startsWith("org.objectweb.asm")) {
                return loadParent(name);
            }
            if (name.startsWith("lombok.")) {
                try {
                    return shadowClassLoader.loadClass(name);
                } catch (ClassNotFoundException ex) {
                    return loadParent(name);
                }

            }
            try {
                return loadParent(name);
            } catch (ClassNotFoundException ex) {
                return shadowClassLoader.loadClass(name);
            }
        } finally {
            arrayList.remove(name);
        }
    }

    private Class<?> loadParent(String name) throws ClassNotFoundException {
        return parentLoader.loadClass(name);
    }
}
