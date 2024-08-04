package x590.newyava.decompilation.scope;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.RemoveIfNotUsed;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.terminal.ThrowOperation;
import x590.newyava.decompilation.operation.variable.CatchOperation;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

/**
 * Представляет блок catch или finally, в зависимости от исключения.
 */
public class CatchScope extends Scope {
	@Getter
	private final CatchOperation catchOperation;

	@RemoveIfNotUsed
	private final TryScope tryScope;

	public CatchScope(@Unmodifiable List<Chunk> chunks, CatchOperation catchOperation, TryScope tryScope) {
		super(chunks);
		this.catchOperation = catchOperation;
		this.tryScope = tryScope;
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
	public void postDecompilation(MethodContext context) {
		super.postDecompilation(context);

		if (catchOperation.isFinally()) {
			removeLastOperationIf(
					operation -> operation instanceof ThrowOperation throwOp &&
							throwOp.getException() instanceof ILoadOperation iload &&
							iload.getSlotId() == catchOperation.getVarRef().getSlotId()
			);
		}
	}

	@Override
	public boolean removeLastContinueOfLoop(LoopScope loop) {
		OperationUtils.removeLastContinueOfLoop(this, loop);
		return true;
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(catchOperation);
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		if (catchOperation.isFinally()) {
			out.record("finally");
		} else {
			out.record("catch (").record(catchOperation, context, Priority.ZERO).record(')');
		}

		return true;
	}
}
