package x590.newyava.decompilation.scope;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtil;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.GotoOperation;
import x590.newyava.decompilation.operation.condition.Role;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

import java.util.*;
import java.util.function.ObjIntConsumer;

public class SwitchScope extends Scope {
	private Operation value;

	private Type requiredType = PrimitiveType.INT;


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

		for (CaseScope caseScope : cases) {
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


	private Type returnType = PrimitiveType.VOID;

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	protected void onEnd() {
		if (arrowStyle && cases.stream().allMatch(CaseScope::canUseInExpression)) {
			assert getParent() != null;
			getParent().operations.remove(this);

			cases.forEach(CaseScope::usePushed);
			getEndChunk().getPushedOperations().push(this);

			returnType = cases.stream()
					.map(CaseScope::getReturnType)
					.filter(type -> type != PrimitiveType.VOID)
					.reduce(Types.ANY_TYPE, (type1, type2) -> Type.assign(type1.wideUp(), type2.wideUp()));
		}
	}

	private @Nullable Int2ObjectMap<FieldDescriptor> enumMap;

	@Override
	public void afterDecompilation(MethodContext context) {
		super.afterDecompilation(context);

		var valueAndEnumMap = OperationUtil.getEnumMap(context, value);

		if (valueAndEnumMap != null) {
			value = valueAndEnumMap.first();
			enumMap = valueAndEnumMap.second();
			requiredType = enumMap.values().iterator().next().type();
		}
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		value.inferType(requiredType);
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

		private final boolean last;

		@Getter
		@ToString.Exclude
		private SwitchScope switchScope;

		protected Deque<Operation> pushedOperations;

		@Getter
		private Type returnType = PrimitiveType.VOID;

		public CaseScope(@Unmodifiable List<Chunk> chunks, @Nullable Collection<IntConstant> constants, boolean last) {
			super(chunks);
			this.constants = constants;
			this.last = last;
		}

		@Override
		protected void onEnd() {
			pushedOperations = getEndChunk().getPushedOperations();

			if (!pushedOperations.isEmpty()) {
				returnType = pushedOperations.peek().getReturnType();
			}

			if (last && (operations.isEmpty() || !(operations.get(0) instanceof GotoOperation))) {
				var gotoOp = new GotoOperation(null);
				gotoOp.initRole(Role.breakScope(switchScope));
				operations.add(gotoOp);
			}
		}

		private boolean canUseInExpression() {
			return !pushedOperations.isEmpty() || getEndChunk().isTerminal();
		}

		private void usePushed() {
			if (!pushedOperations.isEmpty()) {
				initYield(switchScope, pushedOperations);
			}
		}

		@Override
		public void postDecompilation(MethodContext context) {
			super.postDecompilation(context);

//			int lastIndex = operations.size() - 1;
//
//			if (switchScope.arrowStyle && lastIndex >= 0 &&
//				operations.get(lastIndex) instanceof GotoOperation gotoOp &&
//				gotoOp.getRole().isBreakOf(switchScope)) {
//
//				operations.remove(lastIndex);
//			}
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

			var operation = operations.get(0);

			if (operation instanceof GotoOperation gotoOp && gotoOp.getRole().isYieldOf(switchScope)) {
				operation = gotoOp.getRole().getYieldValue();
			}

			out.space().record(operation, context, Priority.ZERO).record(';').decIndent();
		}

		@Override
		protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
			if (constants == null) {
				out.record("default").record(switchScope.arrowStyle ? " ->" : ":");
				return true;
			}

			ObjIntConsumer<IntConstant> writer;

			if (switchScope.enumMap == null) {
				var constantWriteContext = new ConstantWriteContext(context, PrimitiveType.INT, false);
				writer = (constant, index) -> constant.write(out, constantWriteContext);

			} else {
				var enumMap = switchScope.enumMap;
				writer = (constant, index) -> out.record(enumMap.get(constant.getValue()).name());
			}

			if (switchScope.arrowStyle) {
				out .record("case ")
					.record(constants, ", ", writer)
					.record(" ->");

			} else {
				out.record(constants, " ", (constant, index) -> {
					out.record("case ");
					writer.accept(constant, index);
					out.record(':');
				});
			}

			return true;
		}
	}
}
