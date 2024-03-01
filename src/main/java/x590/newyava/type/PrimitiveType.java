package x590.newyava.type;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;

import static x590.newyava.type.IntMultiType.*;

@RequiredArgsConstructor
public enum PrimitiveType implements Type {
	LONG("long", TypeSize.LONG),
	FLOAT("float", TypeSize.WORD),
	DOUBLE("double", TypeSize.LONG),
	VOID("void", TypeSize.VOID);

	public static final IntMultiType
			BOOLEAN = IntMultiType.valueOf(BOOLEAN_FLAG),
			BYTE = IntMultiType.valueOf(BYTE_FLAG),
			SHORT = IntMultiType.valueOf(SHORT_FLAG),
			CHAR = IntMultiType.valueOf(CHAR_FLAG),
			INT = IntMultiType.valueOf(INT_FLAG),

			/** Включает в себя boolean, byte, short, char, int */
			INTEGRAL = IntMultiType.valueOf(ALL_FLAGS),
			BYTE_OR_BOOLEAN = IntMultiType.valueOf(BYTE_FLAG | BOOLEAN_FLAG);

	private final String name;
	private final TypeSize size;

	@Override
	public TypeSize getSize() {
		return size;
	}

	public static boolean isAssignable(PrimitiveType givenType, PrimitiveType requiredType) {
		return givenType == requiredType;
	}

	public static @Nullable PrimitiveType assignQuiet(PrimitiveType givenType, PrimitiveType requiredType) {
		return givenType == requiredType ? givenType : null;
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(name);
	}

	@Override
	public String getVarName() {
		return name.substring(0, 1);
	}


	@Override
	public String toString() {
		return name;
	}
}
