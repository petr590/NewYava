package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.Context;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.Condition;
import x590.newyava.io.DecompilationWriter;

import java.util.List;

public class IfScope extends Scope {

	private final Condition condition;

	public IfScope(Condition condition, @Unmodifiable List<Chunk> chunks) {
		super(chunks);
		this.condition = condition;
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, Context context) {
		out.record("if (").record(condition, context, Priority.ZERO).record(')');
		return true;
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
