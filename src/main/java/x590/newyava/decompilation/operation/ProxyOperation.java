package x590.newyava.decompilation.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

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
	public boolean isTerminal() {
		return operation.isTerminal();
	}

	@Override
	public boolean isReturn() {
		return operation.isReturn();
	}

	@Override
	public boolean usesAnyVariable() {
		return operation.usesAnyVariable();
	}

	@Override
	public void inferType(Type requiredType) {
		operation.inferType(requiredType);
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
		return String.format("ProxyOperation %08x(%s)", hashCode(), operation);
	}
}
