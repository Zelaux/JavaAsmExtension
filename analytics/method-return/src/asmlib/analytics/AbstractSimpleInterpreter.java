package asmlib.analytics;

import asmlib.dev.annotations.AsmVersion;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

abstract class AbstractSimpleInterpreter extends SimpleVerifier {
    /**
     * Constructs a new {@link Interpreter}.
     *
     * @param api the ASM API version supported by this interpreter. Must be one of the {@code
     *            ASM}<i>x</i> values in {@link org.objectweb.asm.Opcodes}.
     */
    protected AbstractSimpleInterpreter(@AsmVersion int api) {
        super(api, null, null, null, false);
    }

    private static boolean isObject(BasicValue value1) {
        return value1.getType().getSort() == Type.OBJECT;
    }

    @Override
    protected boolean isArrayValue(BasicValue value) {
        return super.isArrayValue(value);
    }

    @Override
    protected BasicValue getElementValue(BasicValue objectArrayValue) throws AnalyzerException {
        return super.getElementValue(objectArrayValue);
    }

    @Override
    public BasicValue newValue(Type type) {
        return super.newValue(type);//TODO check
    }

    @Override
    protected boolean isSubTypeOf(BasicValue value, BasicValue expected) {
        return super.isSubTypeOf(value, expected);
    }

    @Override
    public BasicValue merge(BasicValue value1, BasicValue value2) {
        if (isObject(value1) && isObject(value2)) return UnionBasicValue.merge(value1, value2);
        return super.merge(value1, value2);
    }

    @Override
    protected boolean isInterface(Type type) {
        return super.isInterface(type);
    }

    @Override
    protected Type getSuperClass(Type type) {
        return super.getSuperClass(type);
    }

    @Override
    protected boolean isAssignableFrom(Type type1, Type type2) {
        return true;
    }

    @Override
    protected Class<?> getClass(Type type) {
        return super.getClass(type);
    }

}
