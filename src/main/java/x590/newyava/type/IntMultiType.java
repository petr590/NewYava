package x590.newyava.type;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

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


	private final int flags;
	private String name;


	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(getName());
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

	public static boolean isAssignable(IntMultiType givenType, IntMultiType requiredType) {
		return (givenType.flags & requiredType.flags) != 0;
	}

	public static @Nullable IntMultiType assignQuiet(IntMultiType givenType, IntMultiType requiredType) {
		int flags = givenType.flags & requiredType.flags;
		return flags != 0 ? valueOf(flags) : null;
	}
}
