package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.io.DecompilationWriter;

import java.util.List;

public class TryScope extends Scope {
	public TryScope(@Unmodifiable List<Chunk> chunks) {
		super(chunks);
	}

	@Override
	public boolean canShrink() {
		return false;
	}

	@Override
	public boolean removeLastContinueOfLoop(LoopScope loop) {
		OperationUtils.removeLastContinueOfLoop(this, loop);
		return false;
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("try");
		return true;
	}
}
