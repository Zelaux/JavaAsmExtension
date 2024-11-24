package asmlib.method.gen;

import asmlib.method.lombok.*;
import lombok.*;
import lombok.experimental.*;
import org.objectweb.asm.*;

import java.lang.reflect.*;
import java.util.*;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class MethodMeta{
    Method method;
    boolean hasOpcodeParam;

    @SneakyThrows
    public static Object parseType(Class<?> type, InstructionContext context, String stringValue){
        if(type == String.class) return stringValue;
        if(type == Label.class) return context.label(stringValue);
        if(type == Label[].class){
            String[] split = stringValue.split(",");
            Label[] labels = new Label[split.length];
            for(int i = 0; i < labels.length; i++)
                labels[i] = context.label(split[i]);
            return labels;
        }
        if(type == int[].class){
            String[] split = stringValue.split(",");
            int[] ints = new int[split.length];
            for(int i = 0; i < ints.length; i++)
                ints[i] = Integer.parseInt(split[i]);
            return ints;
        }

        if(type.isPrimitive()){
            Class<?> boxedClass = getBoxedClass(type);
            Method declaredMethod = boxedClass.getDeclaredMethod("valueOf", String.class);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null, stringValue);
        }
        if(type == Object.class){
            if(stringValue.matches("\\d+\\.\\d+")){
                return Double.parseDouble(stringValue);
            }else if(stringValue.matches("\\d+")){
                return Long.parseLong(stringValue);
            }else{
                return stringValue;
            }

        }
        try{
            Method declaredMethod = type.getDeclaredMethod("valueOf", String.class);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null, stringValue);
        }catch(NoSuchMethodException ignored){

        }catch(SecurityException e){
            throw new RuntimeException(e);
        }
        throw new UnsupportedOperationException("Cannot parse " + type + " from string");

    }

    private static Class<?> getBoxedClass(Class<?> primitiveClass){
        if(primitiveClass == int.class){
            return Integer.class;
        }else if(primitiveClass == long.class){
            return Long.class;
        }else if(primitiveClass == double.class){
            return Double.class;
        }else if(primitiveClass == float.class){
            return Float.class;
        }else if(primitiveClass == short.class){
            return Short.class;
        }else if(primitiveClass == byte.class){
            return Byte.class;
        }else if(primitiveClass == char.class){
            return Character.class;
        }else if(primitiveClass == boolean.class){
            return Boolean.class;
        }else{
            throw new IllegalArgumentException("Not a primitive type: " + primitiveClass);
        }
    }

    private static boolean isSame(Class<?> expectedType, Class<?> longClass){
        return expectedType == longClass || expectedType == getBoxedClass(longClass);
    }

    @SneakyThrows
    public Object[] transformStringArgs(List<String> o, InstructionContext context){
        Parameter[] parameters = method.getParameters();
        int amount = parameters.length;
        int offset = 0;
        if(hasOpcodeParam) offset++;
        Object[] objects = new Object[amount - offset];
        for(int i = 0; i < amount - offset; i++){
            Parameter parameter = parameters[i + offset];
            String stringValue = o.get(i);
            Class<?> type = parameter.getType();
            objects[i] = parseType(type, context, stringValue);

        }
        return objects;
    }

    @SneakyThrows
    public Object parseType(int index, String string, InstructionContext context){
        return parseType(method.getParameters()[index].getType(), context, string);
    }

    @SneakyThrows
    public void invoke(MethodVisitor visitor, int opcode, Object[] withoutOpcode){
        method.setAccessible(true);
        Object[] args = withoutOpcode;
        if(hasOpcodeParam){
            args = new Object[withoutOpcode.length + 1];
            System.arraycopy(withoutOpcode, 0, args, 1, withoutOpcode.length);
            args[0] = opcode;
        }
        Parameter[] parameters = method.getParameters();
        for(int i = 0; i < args.length; i++){
            Class<?> expectedType = parameters[i].getType();
            Object o = args[i];
            if(!expectedType.isPrimitive() || !(o instanceof Number)) continue;
            Number num = (Number)o;
            Class<?> actualType = o.getClass();
            boolean isFloat = actualType == Float.class || actualType == Double.class;
            if(!isFloat){
                if(isSame(expectedType, long.class)){
                    args[i] = num.longValue();
                }else if(isSame(expectedType, int.class)){
                    args[i] = num.intValue();
                }else if(isSame(expectedType, short.class)){
                    args[i] = num.shortValue();
                }else if(isSame(expectedType, byte.class)){
                    args[i] = num.byteValue();
                }
            }else{
                if(isSame(expectedType, double.class)){
                    args[i] = num.doubleValue();
                }
                if(isSame(expectedType, float.class)){
                    args[i] = num.floatValue();
                }
            }
        }
        method.invoke(visitor, args);
    }
}
