package x590.newyava.constant;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.ClassContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public final class ClassConstant extends Constant {

	public static final ClassConstant
			BYTE    = new ClassConstant(PrimitiveType.BYTE),
			SHORT   = new ClassConstant(PrimitiveType.SHORT),
			CHAR    = new ClassConstant(PrimitiveType.CHAR),
			INT     = new ClassConstant(PrimitiveType.INT),
			LONG    = new ClassConstant(PrimitiveType.LONG),
			FLOAT   = new ClassConstant(PrimitiveType.FLOAT),
			DOUBLE  = new ClassConstant(PrimitiveType.DOUBLE),
			BOOLEAN = new ClassConstant(PrimitiveType.BOOLEAN),
			VOID    = new ClassConstant(PrimitiveType.VOID);

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
	public void write(DecompilationWriter out, ClassContext context, Type type) {
		out.record(this.type, context).record(".class");
	}
}
