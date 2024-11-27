package asmlib.transform.file;

import lombok.*;

import java.lang.reflect.*;
import java.util.*;


public enum FileExtension {
    Class("class"),
    Jar("jar"),
    JavaSource("java"),
    KotlinSource("kt"),
    Unknown(null) {
        @Override
        public String intern(String value) {
            return value;
        }
    }, No(null);


    private static final Map<String, FileExtension> mapper = new HashMap<>();

    static {
        for (FileExtension value : values()) {
            if (value.value == null) continue;
            mapper.put(value.value, value);
        }
    }

    private final String value;

    FileExtension(String value) {
        this.value = value;
    }

    public static FileExtension classify(String value) {
        return mapper.getOrDefault(value, Unknown);
    }

    @SneakyThrows
    private static String getValue(Field field) {
        return (String) field.get(null);
    }

    public String intern(String value) {
        return this.value;
    }
}
