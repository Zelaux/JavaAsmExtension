package asmlib.transform.file;

import org.jetbrains.annotations.Debug.*;
import org.jetbrains.annotations.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

@Renderer(text = "this.packageName()")
public class FileTree{
    public final FileTree parent;
    final File self;
    final FileEntry[] files;
    final FileTree[] folders;
    private final String[] name;

    public FileTree(File root){
        this(root, null, null);
    }

    protected FileTree(File root, @Nullable FileTree parent, @Nullable String[] prefix){
        this.name = add(prefix, root.getName());
        this.parent = parent;
        this.self = root;
        ArrayList<FileEntry> fileList = new ArrayList<>();
        ArrayList<FileTree> folderList = new ArrayList<>();
        for(File file : root.listFiles()){
            if(file.isFile()){
                fileList.add(new FileEntry(file, this, name));
            }else{
                folderList.add(new FileTree(file, this, name));
            }
        }
        files = fileList.toArray(FileEntry[]::new);
        folders = folderList.toArray(FileTree[]::new);
    }

    static String[] add(@Nullable String[] prefix, String name){
        if(prefix == null) return new String[0];
        String[] strings = new String[prefix.length + 1];
        System.arraycopy(prefix, 0, strings, 0, prefix.length);
        strings[strings.length - 1] = name;
        return strings;
    }

    public String packageName(){
        return String.join(".", name);
    }

    public void visitFiles(Consumer<FileEntry> consumer){
        for(FileTree folder : folders){
            folder.visitFiles(consumer);
        }
        for(FileEntry file : files){
            consumer.accept(file);
        }
    }
}
