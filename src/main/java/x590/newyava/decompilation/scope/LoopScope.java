package x590.newyava.decompilation.scope;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.Condition;
import x590.newyava.decompilation.operation.condition.ConstCondition;
import x590.newyava.decompilation.operation.condition.JumpOperation;
import x590.newyava.io.DecompilationWriter;

import java.util.List;
import java.util.Objects;

public class LoopScope extends Scope {

	private final @NotNull Condition condition;

	public LoopScope(@Unmodifiable List<Chunk> chunks) {
		super(chunks);

		Chunk first = chunks.get(0);
		Chunk last = chunks.get(chunks.size() - 1);
		Chunk firstConditional = first.getConditionalChunk();

		if (firstConditional != null && firstConditional.getId() == last.getId() + 1) {
			this.condition = first.requireCondition().opposite();
			first.initRole(JumpOperation.Role.LOOP_CONDITION);

		} else {
			this.condition = Objects.requireNonNullElse(last.getCondition(), ConstCondition.TRUE);
		}

		for (Chunk chunk : chunks) {
			if (chunk.canTakeRole()) {
				Chunk conditional = chunk.getConditionalChunk();

				if (conditional == first) {
					chunk.initRole(JumpOperation.Role.CONTINUE);

				} else if (conditional != null && conditional.getId() == last.getId() + 1) {
					chunk.initRole(JumpOperation.Role.BREAK);
				}
			}
		}
	}

	@Override
	public void removeRedundantOperations(MethodContext context) {
		super.removeRedundantOperations(context);

		var operations = this.operations;
		int last = operations.size() - 1;

		if (last >= 0 && operations.get(last) instanceof JumpOperation jumpOperation &&
			jumpOperation.getRole() == JumpOperation.Role.CONTINUE) {

			operations.remove(last);
		}
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(condition);
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, ClassContext context) {
		out.record("while (").record(condition, context, Priority.ZERO).record(')');
		return true;
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
