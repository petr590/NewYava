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
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.List;

public class LoopScope extends Scope {

	private final Condition condition;

	private final boolean isDoWhile;

	public static LoopScope create(@Unmodifiable List<Chunk> allChunks, int startId, int endId) {
		var chunks = allChunks.subList(startId, endId);

		Chunk first = chunks.get(0);
		Chunk last = Utils.getLast(chunks);
		Chunk firstConditional = first.getConditionalChunk();

		// Предусловие
		if (firstConditional != null && firstConditional.getId() >= last.getId() + 1) {
			// Последняя граница цикла определяется по его заголовку.
			// Не во всех случаях работает.
//			var loopChunks = allChunks.subList(startId, firstConditional.getId());
//			var condition = first.requireCondition().opposite();
//			first.initRole(Role.LOOP_CONDITION);
//			return new LoopScope(loopChunks, condition, false);

			var condition = first.requireCondition().opposite();
			first.initRole(Role.LOOP_CONDITION);
			return new LoopScope(chunks, condition, false);
		}

		Chunk lastConditional = last.getConditionalChunk();

		// Постусловие
		if (lastConditional != null && lastConditional.getId() == first.getId()) {
			var condition = last.requireCondition();
			last.initRole(Role.LOOP_CONDITION);
			return new LoopScope(chunks, condition, true);
		}

		// Бесконечный цикл
		return new LoopScope(chunks, ConstCondition.TRUE, false);
	}

	private LoopScope(@Unmodifiable List<Chunk> chunks, Condition condition, boolean isDoWhile) {
		super(chunks);
		this.condition = condition;
		this.isDoWhile = isDoWhile;

		addRolesTo(chunks);
	}

	@Override
	public boolean canShrink() {
		return false;
	}

	@Override
	protected void onExpand(@Unmodifiable List<Chunk> newChunks) {
		addRolesTo(newChunks);
	}

	private void addRolesTo(@Unmodifiable List<Chunk> chunks) {
		var first = getStartChunk();
		var last = getEndChunk();
		var firstConditional = getStartChunk().getConditionalChunk();

		for (Chunk chunk : chunks) {
			if (chunk.canTakeRole()) {
				Chunk conditional = chunk.getConditionalChunk();

				if (conditional == first) {
					chunk.initRole(Role.continueScope(this));

				} else if (conditional != null && (isDoWhile ?
						conditional.getId() > last.getId() :
						conditional == firstConditional)
				) {
					chunk.initRole(Role.breakScope(this));
				}
			}
		}
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
	protected VarUsage computeVarUsage(int slotId) {
		var usage = super.computeVarUsage(slotId);
		return isDoWhile ? usage : usage.maybe();
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
		if (isDoWhile) {
			out.record("do");
		} else {
			writeWhile(out, context);
		}

		return true;
	}

	@Override
	protected void writeFooter(DecompilationWriter out, MethodWriteContext context, boolean bracketsOmitted) {
		if (isDoWhile) {
			writeWhile(bracketsOmitted ? out.ln().indent() : out.space(), context);
			out.record(';');
		}
	}

	private void writeWhile(DecompilationWriter out, MethodWriteContext context) {
		out.record("while (").record(condition, context, Priority.ZERO).record(')');
	}

	@Override
	protected boolean canOmitBrackets() {
		return true;
	}
}
