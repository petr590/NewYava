package x590.newyava.decompilation.scope;


import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.Condition;
import x590.newyava.decompilation.operation.condition.ConstCondition;
import x590.newyava.decompilation.operation.condition.Role;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.List;
import java.util.Objects;

public class LoopScope extends Scope {

	private final Condition condition;

	public LoopScope(@Unmodifiable List<Chunk> chunks) {
		super(chunks);

		Chunk first = chunks.get(0);
		Chunk last = Utils.getLast(chunks);
		Chunk firstConditional = first.getConditionalChunk();

		if (firstConditional != null && firstConditional.getId() == last.getId() + 1) {
			this.condition = first.requireCondition().opposite();
			first.initRole(Role.LOOP_CONDITION);

		} else {
			this.condition = Objects.requireNonNullElse(last.getCondition(), ConstCondition.TRUE);
		}

		for (Chunk chunk : chunks) {
			if (chunk.canTakeRole()) {
				Chunk conditional = chunk.getConditionalChunk();

				if (conditional == first) {
					chunk.initRole(Role.continueScope(this));

				} else if (conditional != null && conditional.getId() == last.getId() + 1) {
					chunk.initRole(Role.breakScope(this));
				}
			}
		}
	}

	@Override
	public boolean canShrink() {
		return false;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		condition.inferType(PrimitiveType.BOOLEAN);
	}

	@Override
	protected @Nullable Operation getHeaderOperation() {
		return condition;
	}

	@Override
	public boolean isBreakable() {
		return true;
	}

	@Override
	public boolean isContinuable() {
		return true;
	}

	@Override
	public void postDecompilation(MethodContext context) {
		super.postDecompilation(context);
		OperationUtils.removeLastContinueOfLoop(this, this);
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(condition);
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("while (").record(condition, context, Priority.ZERO).record(')');
		return true;
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
