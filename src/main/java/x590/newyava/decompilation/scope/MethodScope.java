package x590.newyava.decompilation.scope;


import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.invokedynamic.RecordInvokedynamicOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ReturnVoidOperation;

import java.util.List;

public class MethodScope extends Scope {

	public MethodScope(@Unmodifiable List<Chunk> chunks) {
		super(chunks);
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

	/**
	 * @return {@code true}, если {@link MethodScope} содержит только
	 * {@link ReturnValueOperation}, которая возвращает {@link RecordInvokedynamicOperation}.
	 */
	public boolean isRecordInvokedynamic() {
		return  operations.size() == 1 &&
				operations.get(0) instanceof ReturnValueOperation returnOperation &&
				returnOperation.getValue() instanceof RecordInvokedynamicOperation;
	}
}
