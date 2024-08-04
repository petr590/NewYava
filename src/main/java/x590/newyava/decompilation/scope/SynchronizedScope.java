package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.monitor.MonitorExitOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;

import java.util.List;

public class SynchronizedScope extends Scope {
	private final Operation value;

	public SynchronizedScope(@Unmodifiable List<Chunk> chunks, Operation value) {
		super(chunks);
		this.value = value;
	}

	@Override
	protected void onStart() {
		for (var chunk : getChunks()) {
			chunk.getOperations().removeIf(operation -> operation instanceof MonitorExitOperation);
		}
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("synchronized (").record(value, context, Priority.ZERO).record(')');
		return true;
	}
}
