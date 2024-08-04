package x590.newyava.decompilation.operation.other;

import x590.newyava.context.MethodWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

/**
 * Пустая операция, которая должна быть заменена в дальнейшем
 */
public enum DummyOperation implements SpecialOperation {
	INSTANCE;

	@Override
	public Type getReturnType() {
		return Types.ANY_TYPE;
	}


	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record("<DUMMY>");
	}


	@Override
	public String toString() {
		return "DummyOperation";
	}
}
