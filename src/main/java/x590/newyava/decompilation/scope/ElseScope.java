package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.io.DecompilationWriter;

import java.util.List;

public class ElseScope extends Scope {

	public ElseScope(@Unmodifiable List<Chunk> chunks) {
		super(chunks);
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, ClassContext context) {
		out.record("else");
		return true;
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
