package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public class CastOperation implements Operation {
	private final Operation operand;
	private final Type requiredType, returnType;
	private final boolean wide;
	private boolean implicitCastAllowed;

	public CastOperation(MethodContext context, Type requiredType, Type returnType, boolean wide) {
		this.operand = context.popAs(requiredType);
		this.requiredType = requiredType;
		this.returnType = returnType;
		this.wide = wide;
	}

	public static CastOperation narrow(MethodContext context, Type requiredType, Type returnType) {
		return new CastOperation(context, requiredType, returnType, false);
	}

	public static CastOperation wide(MethodContext context, Type requiredType, Type returnType) {
		return new CastOperation(context, requiredType, returnType, true);
	}

	@Override
	public void allowImplicitCast() {
		implicitCastAllowed = true;
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public void inferType(Type ignored) {
		operand.inferType(requiredType);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand);
	}

	@Override
	public Priority getPriority() {
		return implicitCastAllowed && wide ? operand.getPriority() : Priority.CAST;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(returnType).addImportsFor(operand);
	}


	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (!implicitCastAllowed || !wide) {
			out.record('(').record(returnType, context).record(')');
		}

		out.record(operand, context, getPriority());
	}

	@Override
	public String toString() {
		return String.format("CastOperation %08x((%s) %s)", hashCode(), returnType, operand);
	}
}
