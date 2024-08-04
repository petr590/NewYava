package x590.newyava.decompilation.operation.other;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Optional;

public class CastOperation implements Operation {
	@Getter
	private final Operation operand;

	@Getter
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
	public Type getImplicitType() {
		return wide ? operand.getImplicitType() : returnType;
	}

	@Override
	public void allowImplicitCast() {
		implicitCastAllowed = true;
	}

	@Override
	public void inferType(Type ignored) {
		operand.inferType(requiredType);
	}

	@Override
	public Optional<String> getPossibleVarName() {
		return operand.getPossibleVarName();
	}

	@Override
	public void addPossibleVarName(@Nullable String name) {
		operand.addPossibleVarName(name);
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operand);
	}

	@Override
	public Priority getPriority() {
		return canOmitCast() ? operand.getPriority() : Priority.CAST;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(returnType).addImportsFor(operand);
	}


	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (!canOmitCast()) {
			out.record('(').record(returnType, context).record(')');
		}

		out.record(operand, context, getPriority());
	}

	private boolean canOmitCast() {
		return implicitCastAllowed && wide || operand.getReturnType().equals(returnType);
	}

	@Override
	public String toString() {
		return String.format("CastOperation %08x((%s) %s)", hashCode(), returnType, operand);
	}
}
