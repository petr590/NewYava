package x590.newyava.decompilation.operation.other;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
@AllArgsConstructor
public class ProxyOperation implements Operation {

	@Getter
	@Setter
	private Operation operation;
	
	@Override
	public Type getReturnType() {
		return operation.getReturnType();
	}

	@Override
	public void allowImplicitCast() {
		operation.allowImplicitCast();
	}

	@Override
	public void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {
		operation.resolveLabelNames(currentScope, generator);
	}

	@Override
	public boolean isThisRef() {
		return operation.isThisRef();
	}

	@Override
	public boolean isDefaultConstructor(MethodContext context) {
		return operation.isDefaultConstructor(context);
	}

	@Override
	public boolean isScopeLike() {
		return operation.isScopeLike();
	}

	@Override
	public boolean needWrapWithBrackets() {
		return operation.needWrapWithBrackets();
	}

	@Override
	public boolean isTerminal() {
		return operation.isTerminal();
	}

	@Override
	public boolean isReturn() {
		return operation.isReturn();
	}

	@Override
	public boolean isThrow() {
		return operation.isThrow();
	}

	@Override
	public boolean usesAnyVariable() {
		return operation.usesAnyVariable();
	}

	@Override
	public boolean usesVariable(Variable variable) {
		return operation.usesVariable(variable);
	}

	@Override
	public VarUsage getVarUsage(int slotId) {
		return operation.getVarUsage(slotId);
	}

	@Override
	public void inferType(Type requiredType) {
		operation.inferType(requiredType);
	}

	@Override
	public Optional<String> getPossibleVarName() {
		return operation.getPossibleVarName();
	}

	@Override
	public void addPossibleVarName(@Nullable String name) {
		operation.addPossibleVarName(name);
	}

	@Override
	public boolean needEmptyLinesAround() {
		return operation.needEmptyLinesAround();
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of(operation);
	}

	@Override
	public void addImports(ClassContext context) {
		operation.addImports(context);
	}

	@Override
	public Priority getPriority() {
		return operation.getPriority();
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		operation.write(out, context);
	}

	@Override
	public void writeAsArrayInitializer(DecompilationWriter out, MethodWriteContext context) {
		operation.writeAsArrayInitializer(out, context);
	}

	@Override
	public String toString() {
		return String.format("ProxyOperation(%s)", operation);
	}
}
