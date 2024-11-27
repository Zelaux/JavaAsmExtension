package asmlib.transform.file;

import lombok.*;

import java.lang.reflect.*;
import java.util.*;


public class FileExtensions{
    public static final String CLASS = "class";
    private static final Map<String, String> interner = new HashMap<>();

    static{
        for(Field field : FileExtensions.class.getDeclaredFields()){
            if(field.getType() == String.class){
                interner.put(getValue(field), getValue(field));
            }
        }
    }

    public static String intern(String value){
        return interner.getOrDefault(value, value);
    }

    @SneakyThrows
    private static String getValue(Field field){
        return (String)field.get(null);
    }
}
