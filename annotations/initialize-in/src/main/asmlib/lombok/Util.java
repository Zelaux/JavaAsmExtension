package asmlib.lombok;

import lombok.core.TypeResolver;
import lombok.javac.JavacNode;

public class Util {
     static boolean typeMatches(String type, JavacNode node, String typeName) {
        if (typeName != null && typeName.length() != 0) {
            int lastIndexA = typeName.lastIndexOf(46) + 1;
            int lastIndexB = Math.max(type.lastIndexOf(46), type.lastIndexOf(36)) + 1;
            int len = typeName.length() - lastIndexA;
            if (len != type.length() - lastIndexB) {
                return false;
            } else {
                for(int i = 0; i < len; ++i) {
                    if (typeName.charAt(i + lastIndexA) != type.charAt(i + lastIndexB)) {
                        return false;
                    }
                }

                TypeResolver resolver = node.getImportListAsTypeResolver();
                return resolver.typeMatches(node, type, typeName);
            }
        } else {
            return false;
        }
    }
}
