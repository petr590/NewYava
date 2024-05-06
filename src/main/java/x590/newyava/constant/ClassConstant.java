package x590.newyava.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassConstant extends Constant {

	public static final ClassConstant
			BYTE    = valueOf(PrimitiveType.BYTE),
			SHORT   = valueOf(PrimitiveType.SHORT),
			CHAR    = valueOf(PrimitiveType.CHAR),
			INT     = valueOf(PrimitiveType.INT),
			LONG    = valueOf(PrimitiveType.LONG),
			FLOAT   = valueOf(PrimitiveType.FLOAT),
			DOUBLE  = valueOf(PrimitiveType.DOUBLE),
			BOOLEAN = valueOf(PrimitiveType.BOOLEAN),
			VOID    = valueOf(PrimitiveType.VOID);

	public static ClassConstant valueOf(Type type) {
		return new ClassConstant(type);
	}

	private final Type type;

	@Override
	public Type getType() {
		return ClassType.CLASS;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(type);
	}

	@Override
	public void write(DecompilationWriter out, Context context, @Nullable Type type) {
		out.record(this.type, context).record(".class");
	}
}
