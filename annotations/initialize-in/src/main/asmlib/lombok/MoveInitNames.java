package asmlib.lombok;

import asmlib.annotations.initializein.InitializeIn;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Function;


public class MoveInitNames {

    public static final String initStaticInNonStatic = extractName(InitializeIn::initializeStaticFromInstanceMethod);
    public static final String pos = extractName(InitializeIn::pos);
    public static final String value = extractName(InitializeIn::value);

    private static String extractName(Function<InitializeIn, Object> consumer) {
        consumer.apply(NameGetterHandler.mockAnno);
        return NameGetterHandler.nameGetterHandler.calledMethod.getName();
    }


    private static class NameGetterHandler implements InvocationHandler {
        private static final NameGetterHandler nameGetterHandler = new NameGetterHandler();
        private static final InitializeIn mockAnno =
                (InitializeIn) Proxy.newProxyInstance(NameGetterHandler.class.getClassLoader(), new Class[]{InitializeIn.class}, nameGetterHandler);
        Method calledMethod;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            calledMethod = method;
            return null;
        }
    }
}
