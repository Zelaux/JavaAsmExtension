package asmlib.method.gen;
import static org.objectweb.asm.Opcodes.*;
@SuppressWarnings("ALL")
class ByteCodeMappingInternal{
    public static String name(int code){
		switch(code){
			case NEW:
				return "NEW";
			case ANEWARRAY:
				return "ANEWARRAY";
			case CHECKCAST:
				return "CHECKCAST";
			case INSTANCEOF:
				return "INSTANCEOF";
			case TABLESWITCH:
				return "TABLESWITCH";
			case IFEQ:
				return "IFEQ";
			case IFNE:
				return "IFNE";
			case IFLT:
				return "IFLT";
			case IFGE:
				return "IFGE";
			case IFGT:
				return "IFGT";
			case IFLE:
				return "IFLE";
			case IF_ICMPEQ:
				return "IF_ICMPEQ";
			case IF_ICMPNE:
				return "IF_ICMPNE";
			case IF_ICMPLT:
				return "IF_ICMPLT";
			case IF_ICMPGE:
				return "IF_ICMPGE";
			case IF_ICMPGT:
				return "IF_ICMPGT";
			case IF_ICMPLE:
				return "IF_ICMPLE";
			case IF_ACMPEQ:
				return "IF_ACMPEQ";
			case IF_ACMPNE:
				return "IF_ACMPNE";
			case GOTO:
				return "GOTO";
			case JSR:
				return "JSR";
			case IFNULL:
				return "IFNULL";
			case IFNONNULL:
				return "IFNONNULL";
			case INVOKEVIRTUAL:
				return "INVOKEVIRTUAL";
			case INVOKESPECIAL:
				return "INVOKESPECIAL";
			case INVOKESTATIC:
				return "INVOKESTATIC";
			case INVOKEINTERFACE:
				return "INVOKEINTERFACE";
			case BIPUSH:
				return "BIPUSH";
			case SIPUSH:
				return "SIPUSH";
			case NEWARRAY:
				return "NEWARRAY";
			case IINC:
				return "IINC";
			case NOP:
				return "NOP";
			case ACONST_NULL:
				return "ACONST_NULL";
			case ICONST_M1:
				return "ICONST_M1";
			case ICONST_0:
				return "ICONST_0";
			case ICONST_1:
				return "ICONST_1";
			case ICONST_2:
				return "ICONST_2";
			case ICONST_3:
				return "ICONST_3";
			case ICONST_4:
				return "ICONST_4";
			case ICONST_5:
				return "ICONST_5";
			case LCONST_0:
				return "LCONST_0";
			case LCONST_1:
				return "LCONST_1";
			case FCONST_0:
				return "FCONST_0";
			case FCONST_1:
				return "FCONST_1";
			case FCONST_2:
				return "FCONST_2";
			case DCONST_0:
				return "DCONST_0";
			case DCONST_1:
				return "DCONST_1";
			case IALOAD:
				return "IALOAD";
			case LALOAD:
				return "LALOAD";
			case FALOAD:
				return "FALOAD";
			case DALOAD:
				return "DALOAD";
			case AALOAD:
				return "AALOAD";
			case BALOAD:
				return "BALOAD";
			case CALOAD:
				return "CALOAD";
			case SALOAD:
				return "SALOAD";
			case IASTORE:
				return "IASTORE";
			case LASTORE:
				return "LASTORE";
			case FASTORE:
				return "FASTORE";
			case DASTORE:
				return "DASTORE";
			case AASTORE:
				return "AASTORE";
			case BASTORE:
				return "BASTORE";
			case CASTORE:
				return "CASTORE";
			case SASTORE:
				return "SASTORE";
			case POP:
				return "POP";
			case POP2:
				return "POP2";
			case DUP:
				return "DUP";
			case DUP_X1:
				return "DUP_X1";
			case DUP_X2:
				return "DUP_X2";
			case DUP2:
				return "DUP2";
			case DUP2_X1:
				return "DUP2_X1";
			case DUP2_X2:
				return "DUP2_X2";
			case SWAP:
				return "SWAP";
			case IADD:
				return "IADD";
			case LADD:
				return "LADD";
			case FADD:
				return "FADD";
			case DADD:
				return "DADD";
			case ISUB:
				return "ISUB";
			case LSUB:
				return "LSUB";
			case FSUB:
				return "FSUB";
			case DSUB:
				return "DSUB";
			case IMUL:
				return "IMUL";
			case LMUL:
				return "LMUL";
			case FMUL:
				return "FMUL";
			case DMUL:
				return "DMUL";
			case IDIV:
				return "IDIV";
			case LDIV:
				return "LDIV";
			case FDIV:
				return "FDIV";
			case DDIV:
				return "DDIV";
			case IREM:
				return "IREM";
			case LREM:
				return "LREM";
			case FREM:
				return "FREM";
			case DREM:
				return "DREM";
			case INEG:
				return "INEG";
			case LNEG:
				return "LNEG";
			case FNEG:
				return "FNEG";
			case DNEG:
				return "DNEG";
			case ISHL:
				return "ISHL";
			case LSHL:
				return "LSHL";
			case ISHR:
				return "ISHR";
			case LSHR:
				return "LSHR";
			case IUSHR:
				return "IUSHR";
			case LUSHR:
				return "LUSHR";
			case IAND:
				return "IAND";
			case LAND:
				return "LAND";
			case IOR:
				return "IOR";
			case LOR:
				return "LOR";
			case IXOR:
				return "IXOR";
			case LXOR:
				return "LXOR";
			case I2L:
				return "I2L";
			case I2F:
				return "I2F";
			case I2D:
				return "I2D";
			case L2I:
				return "L2I";
			case L2F:
				return "L2F";
			case L2D:
				return "L2D";
			case F2I:
				return "F2I";
			case F2L:
				return "F2L";
			case F2D:
				return "F2D";
			case D2I:
				return "D2I";
			case D2L:
				return "D2L";
			case D2F:
				return "D2F";
			case I2B:
				return "I2B";
			case I2C:
				return "I2C";
			case I2S:
				return "I2S";
			case LCMP:
				return "LCMP";
			case FCMPL:
				return "FCMPL";
			case FCMPG:
				return "FCMPG";
			case DCMPL:
				return "DCMPL";
			case DCMPG:
				return "DCMPG";
			case IRETURN:
				return "IRETURN";
			case LRETURN:
				return "LRETURN";
			case FRETURN:
				return "FRETURN";
			case DRETURN:
				return "DRETURN";
			case ARETURN:
				return "ARETURN";
			case RETURN:
				return "RETURN";
			case ARRAYLENGTH:
				return "ARRAYLENGTH";
			case ATHROW:
				return "ATHROW";
			case MONITORENTER:
				return "MONITORENTER";
			case MONITOREXIT:
				return "MONITOREXIT";
			case ILOAD:
				return "ILOAD";
			case LLOAD:
				return "LLOAD";
			case FLOAD:
				return "FLOAD";
			case DLOAD:
				return "DLOAD";
			case ALOAD:
				return "ALOAD";
			case ISTORE:
				return "ISTORE";
			case LSTORE:
				return "LSTORE";
			case FSTORE:
				return "FSTORE";
			case DSTORE:
				return "DSTORE";
			case ASTORE:
				return "ASTORE";
			case RET:
				return "RET";
			case INVOKEDYNAMIC:
				return "INVOKEDYNAMIC";
			case MULTIANEWARRAY:
				return "MULTIANEWARRAY";
			case LDC:
				return "LDC";
			case LOOKUPSWITCH:
				return "LOOKUPSWITCH";
			case GETSTATIC:
				return "GETSTATIC";
			case PUTSTATIC:
				return "PUTSTATIC";
			case GETFIELD:
				return "GETFIELD";
			case PUTFIELD:
				return "PUTFIELD";
		}
		return null;
	}

    public static int code(String name){
		switch(name){
			case "NEW":
				return NEW;
			case "ANEWARRAY":
				return ANEWARRAY;
			case "CHECKCAST":
				return CHECKCAST;
			case "INSTANCEOF":
				return INSTANCEOF;
			case "TABLESWITCH":
				return TABLESWITCH;
			case "IFEQ":
				return IFEQ;
			case "IFNE":
				return IFNE;
			case "IFLT":
				return IFLT;
			case "IFGE":
				return IFGE;
			case "IFGT":
				return IFGT;
			case "IFLE":
				return IFLE;
			case "IF_ICMPEQ":
				return IF_ICMPEQ;
			case "IF_ICMPNE":
				return IF_ICMPNE;
			case "IF_ICMPLT":
				return IF_ICMPLT;
			case "IF_ICMPGE":
				return IF_ICMPGE;
			case "IF_ICMPGT":
				return IF_ICMPGT;
			case "IF_ICMPLE":
				return IF_ICMPLE;
			case "IF_ACMPEQ":
				return IF_ACMPEQ;
			case "IF_ACMPNE":
				return IF_ACMPNE;
			case "GOTO":
				return GOTO;
			case "JSR":
				return JSR;
			case "IFNULL":
				return IFNULL;
			case "IFNONNULL":
				return IFNONNULL;
			case "INVOKEVIRTUAL":
				return INVOKEVIRTUAL;
			case "INVOKESPECIAL":
				return INVOKESPECIAL;
			case "INVOKESTATIC":
				return INVOKESTATIC;
			case "INVOKEINTERFACE":
				return INVOKEINTERFACE;
			case "BIPUSH":
				return BIPUSH;
			case "SIPUSH":
				return SIPUSH;
			case "NEWARRAY":
				return NEWARRAY;
			case "IINC":
				return IINC;
			case "NOP":
				return NOP;
			case "ACONST_NULL":
				return ACONST_NULL;
			case "ICONST_M1":
				return ICONST_M1;
			case "ICONST_0":
				return ICONST_0;
			case "ICONST_1":
				return ICONST_1;
			case "ICONST_2":
				return ICONST_2;
			case "ICONST_3":
				return ICONST_3;
			case "ICONST_4":
				return ICONST_4;
			case "ICONST_5":
				return ICONST_5;
			case "LCONST_0":
				return LCONST_0;
			case "LCONST_1":
				return LCONST_1;
			case "FCONST_0":
				return FCONST_0;
			case "FCONST_1":
				return FCONST_1;
			case "FCONST_2":
				return FCONST_2;
			case "DCONST_0":
				return DCONST_0;
			case "DCONST_1":
				return DCONST_1;
			case "IALOAD":
				return IALOAD;
			case "LALOAD":
				return LALOAD;
			case "FALOAD":
				return FALOAD;
			case "DALOAD":
				return DALOAD;
			case "AALOAD":
				return AALOAD;
			case "BALOAD":
				return BALOAD;
			case "CALOAD":
				return CALOAD;
			case "SALOAD":
				return SALOAD;
			case "IASTORE":
				return IASTORE;
			case "LASTORE":
				return LASTORE;
			case "FASTORE":
				return FASTORE;
			case "DASTORE":
				return DASTORE;
			case "AASTORE":
				return AASTORE;
			case "BASTORE":
				return BASTORE;
			case "CASTORE":
				return CASTORE;
			case "SASTORE":
				return SASTORE;
			case "POP":
				return POP;
			case "POP2":
				return POP2;
			case "DUP":
				return DUP;
			case "DUP_X1":
				return DUP_X1;
			case "DUP_X2":
				return DUP_X2;
			case "DUP2":
				return DUP2;
			case "DUP2_X1":
				return DUP2_X1;
			case "DUP2_X2":
				return DUP2_X2;
			case "SWAP":
				return SWAP;
			case "IADD":
				return IADD;
			case "LADD":
				return LADD;
			case "FADD":
				return FADD;
			case "DADD":
				return DADD;
			case "ISUB":
				return ISUB;
			case "LSUB":
				return LSUB;
			case "FSUB":
				return FSUB;
			case "DSUB":
				return DSUB;
			case "IMUL":
				return IMUL;
			case "LMUL":
				return LMUL;
			case "FMUL":
				return FMUL;
			case "DMUL":
				return DMUL;
			case "IDIV":
				return IDIV;
			case "LDIV":
				return LDIV;
			case "FDIV":
				return FDIV;
			case "DDIV":
				return DDIV;
			case "IREM":
				return IREM;
			case "LREM":
				return LREM;
			case "FREM":
				return FREM;
			case "DREM":
				return DREM;
			case "INEG":
				return INEG;
			case "LNEG":
				return LNEG;
			case "FNEG":
				return FNEG;
			case "DNEG":
				return DNEG;
			case "ISHL":
				return ISHL;
			case "LSHL":
				return LSHL;
			case "ISHR":
				return ISHR;
			case "LSHR":
				return LSHR;
			case "IUSHR":
				return IUSHR;
			case "LUSHR":
				return LUSHR;
			case "IAND":
				return IAND;
			case "LAND":
				return LAND;
			case "IOR":
				return IOR;
			case "LOR":
				return LOR;
			case "IXOR":
				return IXOR;
			case "LXOR":
				return LXOR;
			case "I2L":
				return I2L;
			case "I2F":
				return I2F;
			case "I2D":
				return I2D;
			case "L2I":
				return L2I;
			case "L2F":
				return L2F;
			case "L2D":
				return L2D;
			case "F2I":
				return F2I;
			case "F2L":
				return F2L;
			case "F2D":
				return F2D;
			case "D2I":
				return D2I;
			case "D2L":
				return D2L;
			case "D2F":
				return D2F;
			case "I2B":
				return I2B;
			case "I2C":
				return I2C;
			case "I2S":
				return I2S;
			case "LCMP":
				return LCMP;
			case "FCMPL":
				return FCMPL;
			case "FCMPG":
				return FCMPG;
			case "DCMPL":
				return DCMPL;
			case "DCMPG":
				return DCMPG;
			case "IRETURN":
				return IRETURN;
			case "LRETURN":
				return LRETURN;
			case "FRETURN":
				return FRETURN;
			case "DRETURN":
				return DRETURN;
			case "ARETURN":
				return ARETURN;
			case "RETURN":
				return RETURN;
			case "ARRAYLENGTH":
				return ARRAYLENGTH;
			case "ATHROW":
				return ATHROW;
			case "MONITORENTER":
				return MONITORENTER;
			case "MONITOREXIT":
				return MONITOREXIT;
			case "ILOAD":
				return ILOAD;
			case "LLOAD":
				return LLOAD;
			case "FLOAD":
				return FLOAD;
			case "DLOAD":
				return DLOAD;
			case "ALOAD":
				return ALOAD;
			case "ISTORE":
				return ISTORE;
			case "LSTORE":
				return LSTORE;
			case "FSTORE":
				return FSTORE;
			case "DSTORE":
				return DSTORE;
			case "ASTORE":
				return ASTORE;
			case "RET":
				return RET;
			case "INVOKEDYNAMIC":
				return INVOKEDYNAMIC;
			case "MULTIANEWARRAY":
				return MULTIANEWARRAY;
			case "LDC":
				return LDC;
			case "LOOKUPSWITCH":
				return LOOKUPSWITCH;
			case "GETSTATIC":
				return GETSTATIC;
			case "PUTSTATIC":
				return PUTSTATIC;
			case "GETFIELD":
				return GETFIELD;
			case "PUTFIELD":
				return PUTFIELD;
		}
		return -1;
	}

    public static String map(int code){
		switch(code){
			case NEW:
			case ANEWARRAY:
			case CHECKCAST:
			case INSTANCEOF:
				return "visitTypeInsn";
			case TABLESWITCH:
				return "visiTableSwitchInsn";
			case IFEQ:
			case IFNE:
			case IFLT:
			case IFGE:
			case IFGT:
			case IFLE:
			case IF_ICMPEQ:
			case IF_ICMPNE:
			case IF_ICMPLT:
			case IF_ICMPGE:
			case IF_ICMPGT:
			case IF_ICMPLE:
			case IF_ACMPEQ:
			case IF_ACMPNE:
			case GOTO:
			case JSR:
			case IFNULL:
			case IFNONNULL:
				return "visitJumpInsn";
			case INVOKEVIRTUAL:
			case INVOKESPECIAL:
			case INVOKESTATIC:
			case INVOKEINTERFACE:
				return "visitMethodInsn";
			case BIPUSH:
			case SIPUSH:
			case NEWARRAY:
				return "visitIntInsn";
			case IINC:
				return "visitIincInsn";
			case NOP:
			case ACONST_NULL:
			case ICONST_M1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_5:
			case LCONST_0:
			case LCONST_1:
			case FCONST_0:
			case FCONST_1:
			case FCONST_2:
			case DCONST_0:
			case DCONST_1:
			case IALOAD:
			case LALOAD:
			case FALOAD:
			case DALOAD:
			case AALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
			case IASTORE:
			case LASTORE:
			case FASTORE:
			case DASTORE:
			case AASTORE:
			case BASTORE:
			case CASTORE:
			case SASTORE:
			case POP:
			case POP2:
			case DUP:
			case DUP_X1:
			case DUP_X2:
			case DUP2:
			case DUP2_X1:
			case DUP2_X2:
			case SWAP:
			case IADD:
			case LADD:
			case FADD:
			case DADD:
			case ISUB:
			case LSUB:
			case FSUB:
			case DSUB:
			case IMUL:
			case LMUL:
			case FMUL:
			case DMUL:
			case IDIV:
			case LDIV:
			case FDIV:
			case DDIV:
			case IREM:
			case LREM:
			case FREM:
			case DREM:
			case INEG:
			case LNEG:
			case FNEG:
			case DNEG:
			case ISHL:
			case LSHL:
			case ISHR:
			case LSHR:
			case IUSHR:
			case LUSHR:
			case IAND:
			case LAND:
			case IOR:
			case LOR:
			case IXOR:
			case LXOR:
			case I2L:
			case I2F:
			case I2D:
			case L2I:
			case L2F:
			case L2D:
			case F2I:
			case F2L:
			case F2D:
			case D2I:
			case D2L:
			case D2F:
			case I2B:
			case I2C:
			case I2S:
			case LCMP:
			case FCMPL:
			case FCMPG:
			case DCMPL:
			case DCMPG:
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case RETURN:
			case ARRAYLENGTH:
			case ATHROW:
			case MONITORENTER:
			case MONITOREXIT:
				return "visitInsn";
			case ILOAD:
			case LLOAD:
			case FLOAD:
			case DLOAD:
			case ALOAD:
			case ISTORE:
			case LSTORE:
			case FSTORE:
			case DSTORE:
			case ASTORE:
			case RET:
				return "visitVarInsn";
			case INVOKEDYNAMIC:
				return "visitInvokeDynamicInsn";
			case MULTIANEWARRAY:
				return "visitMultiANewArrayInsn";
			case LDC:
				return "visitLdcInsn";
			case LOOKUPSWITCH:
				return "visitLookupSwitch";
			case GETSTATIC:
			case PUTSTATIC:
			case GETFIELD:
			case PUTFIELD:
				return "visitFieldInsn";
		}
		return null;
	}

    public static boolean hasOpcodeParam(String methodName){
		switch(methodName){
			case "visitTypeInsn":
				return true;
			case "visiTableSwitchInsn":
				return false;
			case "visitJumpInsn":
				return true;
			case "visitMethodInsn":
				return true;
			case "visitIntInsn":
				return true;
			case "visitIincInsn":
				return false;
			case "visitInsn":
				return true;
			case "visitVarInsn":
				return true;
			case "visitInvokeDynamicInsn":
				return false;
			case "visitMultiANewArrayInsn":
				return false;
			case "visitLdcInsn":
				return false;
			case "visitLookupSwitch":
				return false;
			case "visitFieldInsn":
				return true;
		}
		return false;
	}

}
    