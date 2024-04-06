package x590.newyava.decompilation.scope;


import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.invokedynamic.RecordInvokedynamicOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ReturnVoidOperation;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.exception.DecompilationException;

import java.util.List;
import java.util.Optional;

public class MethodScope extends Scope {

	private final MethodContext methodContext;

	public MethodScope(@Unmodifiable List<Chunk> chunks, MethodContext methodContext) {
		super(chunks);
		this.methodContext = methodContext;
	}

	@Override
	public void removeRedundantOperations(MethodContext context) {
		super.removeRedundantOperations(context);

		int last = operations.size() - 1;

		if (!operations.isEmpty() && operations.get(last) == ReturnVoidOperation.INSTANCE) {
			operations.remove(last);
		}

		if (context.isConstructor() && !operations.isEmpty() && operations.get(0).isDefaultConstructor(context)) {
			operations.remove(0);
		}
	}

	/** Внешний scope лямбда-метода. Не то же самое, что {@link #getParent()} */
	private @Nullable MethodScope outerScope;

	private boolean cyclicReferenceChecking;


	public void setOuterScope(MethodScope newOuterScope) {
		if (outerScope != null && outerScope != newOuterScope) {
			throw new DecompilationException(String.format(
					"Outer scope already has been set (old: %s, new: %s)",
					outerScope, newOuterScope
			));
		}

		outerScope = newOuterScope;
	}

	public void checkCyclicReference() {
		if (outerScope == null)
			return;

		synchronized (this) {
			if (cyclicReferenceChecking) {
				throw new DecompilationException("Detected cycling reference in lambda methods: " + this);
			}

			cyclicReferenceChecking = true;
			outerScope.checkCyclicReference();
			cyclicReferenceChecking = false;
		}
	}

	/** @return приоритет, в котором должен вызываться метод {@link #initVariables(List)} */
	public int getVariablesInitPriority() {
		return outerScope == null ? 0 : outerScope.getVariablesInitPriority() - 1;
	}

	@Override
	protected Optional<Variable> findVarByName(String name) {
		return outerScope == null ?
				super.findVarByName(name) :
				super.findVarByName(name).or(() -> outerScope.findVarByName(name));
	}

	/**
	 * @return {@code true}, если {@link MethodScope} содержит только
	 * {@link ReturnValueOperation}, которая возвращает {@link RecordInvokedynamicOperation}.
	 */
	public boolean isRecordInvokedynamic() {
		return  operations.size() == 1 &&
				operations.get(0) instanceof ReturnValueOperation returnOperation &&
				returnOperation.getValue() instanceof RecordInvokedynamicOperation;
	}

	@Override
	public String toString() {
		return String.format("%s(%s, %d - %d)", getClass().getSimpleName(),
				methodContext.getDescriptor(), getStartChunk().getId(), getEndChunk().getId());
	}
}
