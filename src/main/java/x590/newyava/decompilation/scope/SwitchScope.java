package x590.newyava.decompilation.scope;

import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.GotoOperation;
import x590.newyava.decompilation.operation.condition.Role;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.*;

public class SwitchScope extends Scope {
	private final Operation value;

	@Getter
	private final @Unmodifiable List<CaseScope> cases;

	private final boolean arrowStyle;

	public SwitchScope(Operation value, List<CaseScope> cases, @Unmodifiable List<Chunk> chunks) {
		super(chunks, -1);

		this.value = value;

		int pastTheLastId = chunks.get(chunks.size() - 1).getId() + 1;

		this.arrowStyle = cases.subList(0, cases.size() - 1) // Пропускаем последний
				.stream().allMatch(caseScope -> canUseArrowStyle(caseScope, pastTheLastId));

		cases.forEach(caseScope -> caseScope.switchScope = this);

		this.cases = Collections.unmodifiableList(cases);

		for (var caseScope : cases) {
			var endChunk = caseScope.getEndChunk();

			if (endChunk.canTakeRole()) {
				var conditional = endChunk.getConditionalChunk();

				if (conditional != null && conditional.getId() == pastTheLastId) {
					endChunk.initRole(Role.breakScope(this));
				}
			}
		}
	}

	private static boolean canUseArrowStyle(CaseScope caseScope, int pastTheLastId) {
		Chunk endChunk = caseScope.getEndChunk();

		if (endChunk.isTerminal() || endChunk.hasRole())
			return true;

		Chunk conditional = endChunk.getConditionalChunk();
		return conditional != null && conditional.getId() == pastTheLastId;
	}


	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		value.inferType(PrimitiveType.INT);
	}

	@Override
	protected @Nullable Operation getHeaderOperation() {
		return value;
	}

	@Override
	public boolean isBreakable() {
		return true;
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(value);
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("switch (").record(value, context, Priority.ZERO).record(')');
		return true;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		writeHeader(out, context);

		out .record(" {").incIndent().ln().indent()
			.record(cases, context, Priority.ZERO, "\n" + out.getIndent(), CaseScope::write)
			.decIndent().ln().indent().record('}');
	}

	@ToString(includeFieldNames = false, callSuper = true)
	public static class CaseScope extends Scope {
		private final @Nullable Collection<IntConstant> constants;

		@Getter
		@ToString.Exclude
		private SwitchScope switchScope;

		public CaseScope(@Unmodifiable List<Chunk> chunks, @Nullable Collection<IntConstant> constants) {
			super(chunks);
			this.constants = constants;
		}

		@Override
		public void removeRedundantOperations(MethodContext context) {
			super.removeRedundantOperations(context);

			int lastIndex = operations.size() - 1;

			if (switchScope.arrowStyle && lastIndex >= 0 &&
				operations.get(lastIndex) instanceof GotoOperation gotoOperation &&
				gotoOperation.getRole().isBreakOf(switchScope)) {

				operations.remove(lastIndex);
			}
		}

		public void write(DecompilationWriter out, MethodWriteContext context) {
			writeHeader(out, context);

			out.incIndent();

			if (switchScope.arrowStyle) {
				if (operations.isEmpty()) {
					out.record(" {}").decIndent();
					return;
				}

				if (operations.size() > 1 || operations.get(0).isScopeLike() || operations.get(0).isReturn()) {
					out.record(" {");
					writeBody(out, context);
					out.decIndent().ln().indent().record('}');
					return;
				}
			}

			writeBody(out, context);
			out.decIndent();
		}

		@Override
		protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
			if (constants == null) {
				out.record("default").record(switchScope.arrowStyle ? " ->" : ":");

			} else {
				var constantWriteContext = new ConstantWriteContext(context, PrimitiveType.INT, false);

				if (switchScope.arrowStyle) {
					out .record("case ")
						.record(constants, ", ", (constant, index) -> constant.write(out, constantWriteContext))
						.record(" ->");

				} else {
					out.record(constants, " ", (constant, index) -> {
						out.record("case ");
						constant.write(out, constantWriteContext);
						out.record(':');
					});
				}
			}

			return true;
		}
	}
}
