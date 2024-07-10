package x590.newyava.constant;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@EqualsAndHashCode(callSuper = false)
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

	@Getter
	private final Type typeOfClass;

	public Type getType() {
		return ClassType.CLASS;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(typeOfClass);
	}

	@Override
	public void write(DecompilationWriter out, ConstantWriteContext context) {
		out.record(typeOfClass, context).record(".class");
	}

	@Override
	public String toString() {
		return "ClassConstant(" + typeOfClass + ")";
	}
}
