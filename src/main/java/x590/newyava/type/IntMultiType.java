package x590.newyava.type;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

/**
 * Суперпозиция типов, которые обрабатываются как int: boolean, byte, short, char, int.
 * Все экземпляры этого класса кешируются, поэтому можно сравнивать типы напрямую.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntMultiType implements PrimitiveType {

	public static final int
			BOOLEAN_FLAG = 0x1,
			BYTE_FLAG = 0x2,
			SHORT_FLAG = 0x4,
			CHAR_FLAG = 0x8,
			INT_FLAG = 0x10,
			NUMERIC_FLAGS = INT_FLAG | CHAR_FLAG | SHORT_FLAG | BYTE_FLAG,
			ALL_FLAGS = NUMERIC_FLAGS | BOOLEAN_FLAG;


	private static final Int2ObjectMap<IntMultiType> TYPE_POOL = new Int2ObjectOpenHashMap<>();

	public static IntMultiType valueOf(@MagicConstant(flagsFromClass = IntMultiType.class) int flags) {
		if ((flags & ~ALL_FLAGS) != 0)
			throw new IllegalArgumentException("Invalid flags");

		if (flags == 0)
			throw new IllegalArgumentException("No one flag set");
		
		return TYPE_POOL.computeIfAbsent(flags, IntMultiType::new);
	}




	@MagicConstant(flagsFromClass = IntMultiType.class)
	private final int flags;
	private @Nullable String name;


	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(getName());
	}

	@Override
	public @Nullable String getBinName() {
		return switch (flags) {
			case INT_FLAG     -> "I";
			case CHAR_FLAG    -> "C";
			case SHORT_FLAG   -> "S";
			case BYTE_FLAG    -> "B";
			case BOOLEAN_FLAG -> "Z";
			default -> null;
		};
	}

	private String getName() {
		if ((flags & INT_FLAG)     != 0) return "int";
		if ((flags & CHAR_FLAG)    != 0) return "char";
		if ((flags & SHORT_FLAG)   != 0) return "short";
		if ((flags & BYTE_FLAG)    != 0) return "byte";
		if ((flags & BOOLEAN_FLAG) != 0) return "boolean";
		throw new IllegalStateException("No one flag set");
	}

	@Override
	public String getVarName() {
		if ((flags & INT_FLAG)     != 0) return "n";
		if ((flags & CHAR_FLAG)    != 0) return "ch";
		if ((flags & SHORT_FLAG)   != 0) return "s";
		if ((flags & BYTE_FLAG)    != 0) return "b";
		if ((flags & BOOLEAN_FLAG) != 0) return "bool";
		throw new IllegalStateException("No one flag set");
	}

	@Override
	public String getArrVarName() {
		return getName();
	}

	@Override
	public String toString() {
		if (name != null)
			return name;

		return this.name = switch (flags) {
			case INT_FLAG     -> "int";
			case CHAR_FLAG    -> "char";
			case SHORT_FLAG   -> "short";
			case BYTE_FLAG    -> "byte";
			case BOOLEAN_FLAG -> "boolean";
			default -> {
				var str = new StringBuilder("(");
				if ((flags & INT_FLAG)     != 0) str.append("int").append(' ');
				if ((flags & CHAR_FLAG)    != 0) str.append("char").append(' ');
				if ((flags & SHORT_FLAG)   != 0) str.append("short").append(' ');
				if ((flags & BYTE_FLAG)    != 0) str.append("byte").append(' ');
				if ((flags & BOOLEAN_FLAG) != 0) str.append("boolean").append(' ');

				yield str.deleteCharAt(str.length() - 1).append(')').toString();
			}
		};
	}

	private @Nullable IntMultiType widenedUp, widenedDown;

	@Override
	public IntMultiType wideUp() {
		if (widenedUp != null)
			return widenedUp;

		int flags = this.flags;

		if ((flags & BYTE_FLAG)  != 0) flags |= INT_FLAG | SHORT_FLAG;
		if ((flags & SHORT_FLAG) != 0) flags |= INT_FLAG;
		if ((flags & CHAR_FLAG)  != 0) flags |= INT_FLAG;

		return widenedUp = valueOf(flags);
	}

	@Override
	public IntMultiType wideDown() {
		if (widenedDown != null)
			return widenedDown;

		int flags = this.flags;

		if ((flags & INT_FLAG)   != 0) flags |= BYTE_FLAG | SHORT_FLAG | CHAR_FLAG;
		if ((flags & SHORT_FLAG) != 0) flags |= BYTE_FLAG;

		return widenedDown = valueOf(flags);
	}


	@Deprecated(since = "0.8.15")
	public static boolean isAssignable(IntMultiType givenType, IntMultiType requiredType) {
		return (givenType.flags & requiredType.flags) != 0;
	}

	public static @Nullable IntMultiType assignQuiet(IntMultiType givenType, IntMultiType requiredType) {
		int flags = givenType.flags & requiredType.flags;
		return flags != 0 ? valueOf(flags) : null;
	}
}
