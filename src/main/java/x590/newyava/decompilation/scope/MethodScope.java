package x590.newyava.decompilation.scope;


import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.ReturnVoidOperation;

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
}
