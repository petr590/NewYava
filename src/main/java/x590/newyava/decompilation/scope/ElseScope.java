package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.TernaryOperator;
import x590.newyava.io.DecompilationWriter;

import java.util.List;

public final class ElseScope extends IfElseScope {

	public ElseScope(@Unmodifiable List<Chunk> chunks) {
		super(chunks, 0);
	}

	@Override
	public void onEnd() {
		super.onEnd();

		if (getParent() != null && leftOperation != null) {
			var operations = getParent().getOperations();
			int size = operations.size();

			if (size >= 2 &&
				operations.get(size - 1) == this &&
				operations.get(size - 2) instanceof IfScope ifScope &&
				ifScope.getEndChunk().getId() + 1 == this.getStartChunk().getId()) {

				var ifLeftOperation = ifScope.getLeftOperation();

				if (ifLeftOperation != null) {
					leftOperation.setOperation(new TernaryOperator(
							ifScope.getCondition(),
							ifLeftOperation.getOperation(),
							leftOperation.getOperation()
					));

					operations.remove(size - 1);
					operations.remove(size - 2);
				}
			}
		}
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("else");
		return true;
	}
}
