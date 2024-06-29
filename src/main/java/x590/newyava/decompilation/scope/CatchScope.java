package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.variable.CatchOperation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class CatchScope extends Scope {
	private final CatchOperation catchOperation;

	public CatchScope(@Unmodifiable List<Chunk> chunks, CatchOperation catchOperation) {
		super(chunks);
		this.catchOperation = catchOperation;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		catchOperation.inferType(PrimitiveType.VOID);
	}

	@Override
	protected @Nullable Operation getHeaderOperation() {
		return catchOperation;
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(catchOperation);
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("catch (").record(catchOperation, context, Priority.ZERO).record(')');
		return true;
	}
}
