package asmlib.transform;

import asmlib.transform.file.*;

import java.io.*;
import java.util.*;

public class Transformations{


    public static void run(File rootFile, ClassFileTransformer... providers) throws IOException{
        System.out.println("Transformation started");
        FileTree fileTree = new FileTree(rootFile);
        ArrayList<FileEntry> classpath = new ArrayList<>();
        fileTree.visitFiles(it -> {
            if(FileExtensions.CLASS == it.extension){
                classpath.add(it);
            }
        });
        System.out.println("File tree collected");
        File outputFolder = new File(rootFile.getParentFile(), "raw");
        outputFolder.delete();
        FileUtil.copyDirectory(rootFile, outputFolder);


        TransformationPipeline transformationPipeline = new TransformationPipeline(
            providers
        );
        //noinspection StatementWithEmptyBody
        while(transformationPipeline.round(fileTree, classpath)){

        }
        System.out.println("Done.");
    }


}