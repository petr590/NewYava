package x590.newyava.decompilation.instruction;

import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.DoubleConstant;
import x590.newyava.constant.FloatConstant;
import x590.newyava.constant.IntConstant;
import x590.newyava.constant.LongConstant;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Dup;
import x590.newyava.decompilation.operation.*;
import x590.newyava.decompilation.operation.array.ArrayLengthOperation;
import x590.newyava.decompilation.operation.array.ArrayLoadOperation;
import x590.newyava.decompilation.operation.array.ArrayStoreOperation;
import x590.newyava.decompilation.operation.condition.CmpOperation;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.AnyObjectType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.TypeSize;

import static x590.newyava.decompilation.operation.BinaryOperation.Operator.*;
import static org.objectweb.asm.Opcodes.*;

public record JustInsn(int opcode) implements Instruction {

	@Override
	public int getOpcode() {
		return opcode;
	}

	@Override
	public @Nullable Operation toOperation(MethodContext context) {
		return switch (opcode) {
			case NOP -> null;

			case ACONST_NULL -> ConstNullOperation.INSTANCE;
			case ICONST_M1 -> new LdcOperation(IntConstant.MINUS_ONE);
			case ICONST_0 -> new LdcOperation(IntConstant.ZERO);
			case ICONST_1 -> new LdcOperation(IntConstant.ONE);
			case ICONST_2 -> new LdcOperation(IntConstant.TWO);
			case ICONST_3 -> new LdcOperation(IntConstant.THREE);
			case ICONST_4 -> new LdcOperation(IntConstant.FOUR);
			case ICONST_5 -> new LdcOperation(IntConstant.FIVE);
			case LCONST_0 -> new LdcOperation(LongConstant.ZERO);
			case LCONST_1 -> new LdcOperation(LongConstant.ONE);
			case FCONST_0 -> new LdcOperation(FloatConstant.ZERO);
			case FCONST_1 -> new LdcOperation(FloatConstant.ONE);
			case FCONST_2 -> new LdcOperation(FloatConstant.TWO);
			case DCONST_0 -> new LdcOperation(DoubleConstant.ZERO);
			case DCONST_1 -> new LdcOperation(DoubleConstant.ONE);

			case IALOAD -> new ArrayLoadOperation(context, PrimitiveType.INT);
			case LALOAD -> new ArrayLoadOperation(context, PrimitiveType.LONG);
			case FALOAD -> new ArrayLoadOperation(context, PrimitiveType.FLOAT);
			case DALOAD -> new ArrayLoadOperation(context, PrimitiveType.DOUBLE);
			case AALOAD -> new ArrayLoadOperation(context, AnyObjectType.INSTANCE);
			case BALOAD -> new ArrayLoadOperation(context, PrimitiveType.BYTE_OR_BOOLEAN);
			case CALOAD -> new ArrayLoadOperation(context, PrimitiveType.CHAR);
			case SALOAD -> new ArrayLoadOperation(context, PrimitiveType.SHORT);

			case IASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.INT);
			case LASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.LONG);
			case FASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.FLOAT);
			case DASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.DOUBLE);
			case AASTORE -> ArrayStoreOperation.valueOf(context, AnyObjectType.INSTANCE);
			case BASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.BYTE_OR_BOOLEAN);
			case CASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.CHAR);
			case SASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.SHORT);

			case POP  -> new PopOperation(context, TypeSize.WORD);
			case POP2 -> new PopOperation(context, TypeSize.LONG);

			case DUP     -> { Dup.dup(context.getStack(), TypeSize.WORD); yield null; }
			case DUP2    -> { Dup.dup(context.getStack(), TypeSize.LONG); yield null; }
			case DUP_X1  -> { Dup.dupX1(context.getStack(), TypeSize.WORD, TypeSize.WORD); yield null; }
			case DUP2_X1 -> { Dup.dupX1(context.getStack(), TypeSize.LONG, TypeSize.WORD); yield null; }
			case DUP2_X2 -> { Dup.dupX1(context.getStack(), TypeSize.LONG, TypeSize.LONG); yield null; }
			case DUP_X2  -> { Dup.dupX2(context.getStack()); yield null; }

			case IADD -> new BinaryOperation(context, ADD, PrimitiveType.INT);
			case LADD -> new BinaryOperation(context, ADD, PrimitiveType.LONG);
			case FADD -> new BinaryOperation(context, ADD, PrimitiveType.FLOAT);
			case DADD -> new BinaryOperation(context, ADD, PrimitiveType.DOUBLE);

			case ISUB -> new BinaryOperation(context, SUB, PrimitiveType.INT);
			case LSUB -> new BinaryOperation(context, SUB, PrimitiveType.LONG);
			case FSUB -> new BinaryOperation(context, SUB, PrimitiveType.FLOAT);
			case DSUB -> new BinaryOperation(context, SUB, PrimitiveType.DOUBLE);

			case IMUL -> new BinaryOperation(context, MUL, PrimitiveType.INT);
			case LMUL -> new BinaryOperation(context, MUL, PrimitiveType.LONG);
			case FMUL -> new BinaryOperation(context, MUL, PrimitiveType.FLOAT);
			case DMUL -> new BinaryOperation(context, MUL, PrimitiveType.DOUBLE);

			case IDIV -> new BinaryOperation(context, DIV, PrimitiveType.INT);
			case LDIV -> new BinaryOperation(context, DIV, PrimitiveType.LONG);
			case FDIV -> new BinaryOperation(context, DIV, PrimitiveType.FLOAT);
			case DDIV -> new BinaryOperation(context, DIV, PrimitiveType.DOUBLE);

			case IREM -> new BinaryOperation(context, REM, PrimitiveType.INT);
			case LREM -> new BinaryOperation(context, REM, PrimitiveType.LONG);
			case FREM -> new BinaryOperation(context, REM, PrimitiveType.FLOAT);
			case DREM -> new BinaryOperation(context, REM, PrimitiveType.DOUBLE);

			case INEG -> new UnaryOperation(context, "-", PrimitiveType.INT);
			case LNEG -> new UnaryOperation(context, "-", PrimitiveType.LONG);
			case FNEG -> new UnaryOperation(context, "-", PrimitiveType.FLOAT);
			case DNEG -> new UnaryOperation(context, "-", PrimitiveType.DOUBLE);

			case ISHL -> new BinaryOperation(context, SHL, PrimitiveType.INT, PrimitiveType.INT);
			case LSHL -> new BinaryOperation(context, SHL, PrimitiveType.LONG, PrimitiveType.INT);

			case ISHR -> new BinaryOperation(context, SHR, PrimitiveType.INT, PrimitiveType.INT);
			case LSHR -> new BinaryOperation(context, SHR, PrimitiveType.LONG, PrimitiveType.INT);

			case IUSHR -> new BinaryOperation(context, USHR, PrimitiveType.INT, PrimitiveType.INT);
			case LUSHR -> new BinaryOperation(context, USHR, PrimitiveType.LONG, PrimitiveType.INT);

			case IAND -> new BinaryOperation(context, AND, PrimitiveType.INT);
			case LAND -> new BinaryOperation(context, AND, PrimitiveType.LONG);

			case IOR -> new BinaryOperation(context, OR, PrimitiveType.INT);
			case LOR -> new BinaryOperation(context, OR, PrimitiveType.LONG);

			case IXOR -> new BinaryOperation(context, XOR, PrimitiveType.INT);
			case LXOR -> new BinaryOperation(context, XOR, PrimitiveType.LONG);

			case I2L -> CastOperation.wide(context, PrimitiveType.INT, PrimitiveType.LONG);
			case I2F -> CastOperation.wide(context, PrimitiveType.INT, PrimitiveType.FLOAT);
			case I2D -> CastOperation.wide(context, PrimitiveType.INT, PrimitiveType.DOUBLE);

			case L2I -> CastOperation.narrow(context, PrimitiveType.LONG, PrimitiveType.INT);
			case L2F -> CastOperation.wide(context, PrimitiveType.LONG, PrimitiveType.FLOAT);
			case L2D -> CastOperation.wide(context, PrimitiveType.LONG, PrimitiveType.DOUBLE);

			case F2I -> CastOperation.narrow(context, PrimitiveType.FLOAT, PrimitiveType.INT);
			case F2L -> CastOperation.narrow(context, PrimitiveType.FLOAT, PrimitiveType.LONG);
			case F2D -> CastOperation.wide(context, PrimitiveType.FLOAT, PrimitiveType.DOUBLE);

			case D2I -> CastOperation.narrow(context, PrimitiveType.DOUBLE, PrimitiveType.INT);
			case D2L -> CastOperation.narrow(context, PrimitiveType.DOUBLE, PrimitiveType.LONG);
			case D2F -> CastOperation.narrow(context, PrimitiveType.DOUBLE, PrimitiveType.FLOAT);

			case I2B -> CastOperation.narrow(context, PrimitiveType.INT, PrimitiveType.BYTE);
			case I2C -> CastOperation.narrow(context, PrimitiveType.INT, PrimitiveType.CHAR);
			case I2S -> CastOperation.narrow(context, PrimitiveType.INT, PrimitiveType.SHORT);

			case LCMP -> new CmpOperation(context, PrimitiveType.LONG);
			case FCMPL, FCMPG -> new CmpOperation(context, PrimitiveType.FLOAT);
			case DCMPL, DCMPG -> new CmpOperation(context, PrimitiveType.DOUBLE);

			case ARRAYLENGTH -> new ArrayLengthOperation(context);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}
}
