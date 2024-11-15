package asmlib.transform.file;

import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.Debug.*;

import java.io.*;

import static asmlib.transform.file.FileTree.add;

@Renderer(text = "this.classpathName()")
public class FileEntry{
    public final FileTree parent;
    public final File file;


    @MagicConstant(valuesFromClass = FileExtensions.class)
    public final String extension;
    private final String[] name;

    public FileEntry(File file, FileTree parent, String[] prefix){
        this.file = file;
        this.parent = parent;
        String rawName = file.getName();
        int i = rawName.indexOf('.');
        if(i == -1){
            this.name = add(prefix, rawName);
            extension = null;
        }else{
            this.name = add(prefix, rawName.substring(0, i));
            //noinspection MagicConstant
            extension = FileExtensions.intern(rawName.substring(i + 1));
        }
    }

    public String classpathName(){
        return String.join(".", name);
    }
}
