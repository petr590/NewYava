package x590.newyava.decompilation.instruction;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.DoubleConstant;
import x590.newyava.constant.FloatConstant;
import x590.newyava.constant.IntConstant;
import x590.newyava.constant.LongConstant;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.*;
import x590.newyava.decompilation.operation.array.ArrayLengthOperation;
import x590.newyava.decompilation.operation.array.ArrayLoadOperation;
import x590.newyava.decompilation.operation.array.ArrayStoreOperation;
import x590.newyava.decompilation.operation.condition.CmpOperation;
import x590.newyava.decompilation.operation.monitor.MonitorEnterOperation;
import x590.newyava.decompilation.operation.monitor.MonitorExitOperation;
import x590.newyava.decompilation.operation.operator.BinaryOperator;
import x590.newyava.decompilation.operation.operator.UnaryOperator;
import x590.newyava.decompilation.operation.other.CastOperation;
import x590.newyava.decompilation.operation.other.ConstNullOperation;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.other.PopOperation;
import x590.newyava.exception.UnknownOpcodeException;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.TypeSize;
import x590.newyava.type.Types;

import static org.objectweb.asm.Opcodes.*;
import static x590.newyava.decompilation.operation.operator.OperatorType.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JustInsn implements Instruction {
	private static final Int2ObjectMap<JustInsn> CACHE = new Int2ObjectOpenHashMap<>();

	public static JustInsn of(int opcode) {
		return CACHE.computeIfAbsent(opcode, JustInsn::new);
	}

	@Getter
	private final int opcode;

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
			case AALOAD -> new ArrayLoadOperation(context, Types.ANY_OBJECT_TYPE);
			case BALOAD -> new ArrayLoadOperation(context, PrimitiveType.BYTE_OR_BOOLEAN);
			case CALOAD -> new ArrayLoadOperation(context, PrimitiveType.CHAR);
			case SALOAD -> new ArrayLoadOperation(context, PrimitiveType.SHORT);

			case IASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.INT);
			case LASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.LONG);
			case FASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.FLOAT);
			case DASTORE -> ArrayStoreOperation.valueOf(context, PrimitiveType.DOUBLE);
			case AASTORE -> ArrayStoreOperation.valueOf(context, Types.ANY_OBJECT_TYPE);
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

			case IADD -> new BinaryOperator(context, ADD, PrimitiveType.INT);
			case LADD -> new BinaryOperator(context, ADD, PrimitiveType.LONG);
			case FADD -> new BinaryOperator(context, ADD, PrimitiveType.FLOAT);
			case DADD -> new BinaryOperator(context, ADD, PrimitiveType.DOUBLE);

			case ISUB -> new BinaryOperator(context, SUB, PrimitiveType.INT);
			case LSUB -> new BinaryOperator(context, SUB, PrimitiveType.LONG);
			case FSUB -> new BinaryOperator(context, SUB, PrimitiveType.FLOAT);
			case DSUB -> new BinaryOperator(context, SUB, PrimitiveType.DOUBLE);

			case IMUL -> new BinaryOperator(context, MUL, PrimitiveType.INT);
			case LMUL -> new BinaryOperator(context, MUL, PrimitiveType.LONG);
			case FMUL -> new BinaryOperator(context, MUL, PrimitiveType.FLOAT);
			case DMUL -> new BinaryOperator(context, MUL, PrimitiveType.DOUBLE);

			case IDIV -> new BinaryOperator(context, DIV, PrimitiveType.INT);
			case LDIV -> new BinaryOperator(context, DIV, PrimitiveType.LONG);
			case FDIV -> new BinaryOperator(context, DIV, PrimitiveType.FLOAT);
			case DDIV -> new BinaryOperator(context, DIV, PrimitiveType.DOUBLE);

			case IREM -> new BinaryOperator(context, REM, PrimitiveType.INT);
			case LREM -> new BinaryOperator(context, REM, PrimitiveType.LONG);
			case FREM -> new BinaryOperator(context, REM, PrimitiveType.FLOAT);
			case DREM -> new BinaryOperator(context, REM, PrimitiveType.DOUBLE);

			case INEG -> new UnaryOperator(context, "-", PrimitiveType.INT);
			case LNEG -> new UnaryOperator(context, "-", PrimitiveType.LONG);
			case FNEG -> new UnaryOperator(context, "-", PrimitiveType.FLOAT);
			case DNEG -> new UnaryOperator(context, "-", PrimitiveType.DOUBLE);

			case ISHL -> new BinaryOperator(context, SHL, PrimitiveType.INT, PrimitiveType.INT);
			case LSHL -> new BinaryOperator(context, SHL, PrimitiveType.LONG, PrimitiveType.INT);

			case ISHR -> new BinaryOperator(context, SHR, PrimitiveType.INT, PrimitiveType.INT);
			case LSHR -> new BinaryOperator(context, SHR, PrimitiveType.LONG, PrimitiveType.INT);

			case IUSHR -> new BinaryOperator(context, USHR, PrimitiveType.INT, PrimitiveType.INT);
			case LUSHR -> new BinaryOperator(context, USHR, PrimitiveType.LONG, PrimitiveType.INT);

			case IAND -> new BinaryOperator(context, AND, PrimitiveType.INT_OR_BOOLEAN);
			case LAND -> new BinaryOperator(context, AND, PrimitiveType.LONG);

			case IOR -> new BinaryOperator(context, OR, PrimitiveType.INT_OR_BOOLEAN);
			case LOR -> new BinaryOperator(context, OR, PrimitiveType.LONG);

			case IXOR -> new BinaryOperator(context, XOR, PrimitiveType.INT_OR_BOOLEAN);
			case LXOR -> new BinaryOperator(context, XOR, PrimitiveType.LONG);

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

			case ARRAYLENGTH  -> new ArrayLengthOperation(context);
			case MONITORENTER -> new MonitorEnterOperation(context);
			case MONITOREXIT  -> new MonitorExitOperation(context);

			default -> throw new UnknownOpcodeException(opcode);
		};
	}

	@Override
	public String toString() {
		return String.format("JustInsn(%s)", InsnUtils.opcodeToString(opcode));
	}
}
