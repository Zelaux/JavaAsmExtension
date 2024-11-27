package asmlib.transform;

import asmlib.transform.file.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.stream.*;

public class TransformationPipeline{
    ClassFileTransformer[] providers;

    public TransformationPipeline(ClassFileTransformer... providers){
        this.providers = new ClassFileTransformer[providers.length];
        System.arraycopy(providers, 0, this.providers, 0, providers.length);
        Arrays.sort(this.providers);
    }

    private static void saveToFile(File outputOperationClass, byte[] byteArray) throws IOException{
        //noinspection ResultOfMethodCallIgnored
        outputOperationClass.delete();
        //noinspection ResultOfMethodCallIgnored
        outputOperationClass.getParentFile().mkdirs();
        try(FileOutputStream stream = new FileOutputStream(outputOperationClass)){
            stream.write(byteArray);
        }
    }

    public boolean round(FileTree root, List<FileEntry> classpath) throws IOException{
        readState(classpath);
        Stream<FileEntry> state = writeState(classpath);
        ArrayList<ClassFileTransformer> transformers = new ArrayList<>(providers.length);
        for(ClassFileTransformer provider : providers){
            if(provider.needNextRound()){
                transformers.add(provider);
            }
        }
        this.providers = transformers.toArray(ClassFileTransformer[]::new);
        boolean hasFiles = false;
        for(FileEntry entry : state.toList()){
            System.out.printf("Class '%s' changed%n", entry.classpathName());
            hasFiles = true;
        }
        return hasFiles && this.providers.length > 0;
    }

    private Stream<FileEntry> writeState(List<FileEntry> classpath) throws IOException{
        Map<FileEntry, List<ClassFileTransformer>> collect = classpath.stream().collect(Collectors.toMap(it -> it, it -> {
            String className = it.classpathName();
            return Arrays.stream(providers).filter(prov -> prov.shouldWrite(className))
                         .toList();
        }));
        for(Entry<FileEntry, List<ClassFileTransformer>> entry : collect.entrySet()){
            List<ClassFileTransformer> transformers = entry.getValue();
            if(transformers.isEmpty()) continue;
            FileEntry key = entry.getKey();


            byte[] allBytes;
            try(FileInputStream fileInputStream = new FileInputStream(key.file)){
                allBytes = fileInputStream.readAllBytes();
            }
            String className = key.classpathName();
            ClassNode classNode = new ClassNode();
            ClassWriter classWriter;
            {
                ClassReader reader = new ClassReader(allBytes);
                reader.accept(classNode, 0);
                classWriter = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            }


            ClassVisitor visitor = classWriter;
            for(ClassFileTransformer transformer : transformers){
                classNode = transformer.transformClass(classNode);
                ClassVisitor writeVisitor = transformer.createWriteVisitor(className, visitor);
                visitor = writeVisitor == null ? visitor : writeVisitor;
            }
            classNode.accept(visitor);
            saveToFile(key.file, classWriter.toByteArray());
        }
        return collect.entrySet().stream().filter(it -> !it.getValue().isEmpty()).map(Entry::getKey);
    }

    protected void readState(List<FileEntry> classpath) throws IOException{
        Map<FileEntry, List<ClassFileTransformer>> collect = classpath.stream().collect(Collectors.toMap(it -> it, it -> {
            String className = it.classpathName();
            return Arrays.stream(providers).filter(prov -> prov.shouldRead(className))
                         .toList();
        }));
        for(Entry<FileEntry, List<ClassFileTransformer>> entry : collect.entrySet()){
            List<ClassFileTransformer> transformers = entry.getValue();
            if(transformers.isEmpty()) continue;
            FileEntry key = entry.getKey();


            byte[] allBytes;
            try(FileInputStream fileInputStream = new FileInputStream(key.file)){
                allBytes = fileInputStream.readAllBytes();
            }
            String className = key.classpathName();
            for(ClassFileTransformer transformer : transformers){
                ClassReader reader = new ClassReader(allBytes);
                transformer.readClass(className, reader);
            }
        }
    }

}
