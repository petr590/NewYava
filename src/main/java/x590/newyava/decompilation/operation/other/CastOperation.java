package x590.newyava.decompilation.operation.other;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.invoke.InvokeOperation;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
@RequiredArgsConstructor
public class CastOperation implements Operation {
	@Getter
	private final Operation operand;

	@Getter
	private final Type requiredType, returnType;

	private final boolean wide;

	@EqualsAndHashCode.Exclude
	private boolean implicitCastAllowed;

	public CastOperation(MethodContext context, Type requiredType, Type returnType, boolean wide) {
		this(context.popAs(requiredType), requiredType, returnType, wide);
	}

	public static CastOperation narrow(Operation operand, Type requiredType, Type returnType) {
		return new CastOperation(operand, requiredType, returnType, false);
	}

	public static CastOperation narrow(MethodContext context, Type requiredType, Type returnType) {
		return new CastOperation(context, requiredType, returnType, false);
	}

	public static CastOperation wide(MethodContext context, Type requiredType, Type returnType) {
		return new CastOperation(context, requiredType, returnType, true);
	}


	private boolean isGeneric;

	@Override
	public void beforeVariablesInit(Context context, @Nullable MethodScope methodScope) {
		Operation.super.beforeVariablesInit(context, methodScope);

		if (operand instanceof InvokeOperation invokeOp) {
			var method = context.findIMethod(invokeOp.getDescriptor());

			isGeneric =
					method.isPresent() &&
					Type.isGeneric(method.get().getVisibleDescriptor().returnType());

		} else if (operand instanceof FieldOperation fieldOp && fieldOp.isGetter()) {
			var field = context.findIField(fieldOp.getDescriptor());

			isGeneric =
					field.isPresent() &&
					Type.isGeneric(field.get().getVisibleDescriptor().type());
		}
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
			out.record('(').record(returnType, context).record(')').space();
		}

		out.record(operand, context, getPriority());
	}

	private boolean canOmitCast() {
		return implicitCastAllowed && wide || isGeneric || operand.getReturnType().equals(returnType);
	}

	@Override
	public String toString() {
		return String.format("CastOperation((%s) %s)", returnType, operand);
	}
}
