package x590.newyava.decompilation.scope;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.*;
import x590.newyava.Log;
import x590.newyava.RemoveIfNotUsed;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtil;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.variable.DeclareOperation;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.*;
import java.util.function.Predicate;

/**
 * Область видимости, ограниченная, как правило, фигурными скобками
 */
public class Scope implements Operation, Comparable<Scope> {

	/** Первый чанк, принадлежащий scope-у. */
	@Getter
	private final Chunk startChunk;

	/** Последний чанк, принадлежащий scope-у. */
	@Getter
	private Chunk endChunk;

	/** Необходим для правильной сортировки, так как иногда scope-ы могут
	 * совпадать по чанкам, но при этом они не должны совпадать по индексам операций. */
	private final int startIndex;


	@Getter
	private @Nullable Scope parent;


	protected final List<Operation> operations = new ArrayList<>();

	private final @UnmodifiableView List<Operation> operationsView = Collections.unmodifiableList(operations);

	public @UnmodifiableView List<Operation> getOperations() {
		return operationsView;
	}

	private final List<Scope> scopes = new ArrayList<>();

	/** Переменные, которые содержит этот scope.
	 * Если переменная равна {@code null}, значит в этом слоте нет переменных. */
	protected final List<@Nullable Variable> variables = new ArrayList<>();

	private final @UnmodifiableView List<@Nullable Variable> variablesView = Collections.unmodifiableList(variables);

	public @UnmodifiableView List<@Nullable Variable> getVariables() {
		return variablesView;
	}


	/** Переменные, которые должны быть объявлены в этом scope. */
	protected final Set<Variable> variablesToDeclare = new HashSet<>();


	public Scope(@Unmodifiable List<Chunk> chunks) {
		this(chunks, 0);
	}

	/**
	 * @param chunks список чанков, которые захватил этот scope
	 * @param startIndexOffset смещение, которое прибавляется к начальному индексу первого чанка,
	 *                         чтобы получить значение {@link #startIndex}. Как правило, равно 0 или -1.
	 */
	public Scope(@Unmodifiable List<Chunk> chunks, int startIndexOffset) {
		this.startChunk = chunks.get(0);
		this.endChunk = chunks.get(chunks.size() - 1);
		this.startIndex = startChunk.getStartIndex() + startIndexOffset;
	}

	public boolean isEmpty() {
		return operations.isEmpty();
	}


	private final Int2ObjectMap<VarUsage> usagesCache = new Int2ObjectArrayMap<>();

	@Override
	public VarUsage getVarUsage(int slotId) {
		return usagesCache.computeIfAbsent(slotId, Operation.super::getVarUsage);
	}

	@Override
	@MustBeInvokedByOverriders
	public void inferType(Type ignored) {
		operations.forEach(operation -> operation.inferType(PrimitiveType.VOID));
	}

	/** @return все операции внутри scope, включая операцию в заголовке. */
	@Override
	public final @UnmodifiableView List<? extends Operation> getNestedOperations() {
		var headerOperation = getHeaderOperation();
		return headerOperation == null ? operations :
				OperationUtil.addBefore(headerOperation, operations);
	}

	/** @return операцию в заголовке scope или {@code null}, если её нет.
	 * По умолчанию возвращает {@code null}. */
	protected @Nullable Operation getHeaderOperation() {
		return null;
	}


	/** Можно ли к нему применить {@code break} */
	public boolean isBreakable() {
		return false;
	}


	/** Можно ли к нему применить {@code continue} */
	public boolean isContinuable() {
		return false;
	}


	private @Nullable String labelName;

	public String getLabelName(LabelNameGenerator generator) {
		return labelName != null ? labelName :
				(labelName = generator.nextLabelName());
	}



	/**
	 * Добавляет операции из чанка в список операций и вызывает
	 * {@link #onAdd(Operation, Chunk)}, если список не пустой.
	 */
	public void addOperationsFromChunk(Chunk chunk) {
		var chunkOperations = chunk.getOperations();

		operations.addAll(chunkOperations);

		if (!chunkOperations.isEmpty()) {
			onAdd(chunkOperations.get(chunkOperations.size() - 1), chunk);
		}
	}


	/**
	 * Вызывается при добавлении группы операций (в том числе и {@link Scope}) в {@link #operations}.
	 * @param lastOperation последняя операция из добавленных.
	 * @param lastChunk чанк, из которого была добавлена группа операций
	 *                  или последний чанк {@link Scope}-а.
	 */
	@RemoveIfNotUsed
	protected void onAdd(Operation lastOperation, Chunk lastChunk) {}


	/**
	 * Начинает все scope-ы, до которых дошла очередь.
	 * Если {@link IfScope}, {@link ElseScope} или {@link CatchScope} выходит за
	 * границы текущего scope, то он будет уменьшен до этих границ.
	 * @param scopeQueue очередь {@link Scope}-ов, должна быть отсортирована по возрастанию.
	 * @param currentId текущий id чанка.
	 */
	public final Scope startScopes(Queue<Scope> scopeQueue, int currentId) {
		var scope = scopeQueue.peek();

		if (scope != null && scope.getStartChunk().getId() == currentId) {
			scopeQueue.poll();

			operations.add(scope);
			scopes.add(scope);
			onAdd(scope, scope.endChunk);

			scope.parent = this;

			if ((scope instanceof IfScope || scope instanceof ElseScope || scope instanceof CatchScope)
					&& scope.endChunk.getId() > this.endChunk.getId()) {
				scope.endChunk = this.endChunk;
			}

			return scope.startScopes(scopeQueue, currentId);
		}

		return this;
	}

	/** Завершает все текущие scope-ы, если они достигли конца */
	public final Scope endIfReached(int currentId) {
		if (currentId >= endChunk.getId() && parent != null) {
			onEnd();
			return parent.endIfReached(currentId);
		}

		return this;
	}


	/** Вызывается, когда текущий scope достигает конца */
	protected void onEnd() {}


	@Data
	protected static final class VarOwner {
		/** id чанков начала и конца */
		private final int start, end;

		/** Scope, которому принадлежит переменная */
		private final Scope scope;

		@Getter(AccessLevel.NONE)
		private @Nullable Variable variable;

		public Variable getOrCreateVariable(VariableReference ref) {
			if (variable != null)
				return variable;

			return variable = new Variable(ref, false);
		}
	}


	/**
	 * Ищет для каждой переменной своего "владельца" - т.е. scope, в котором
	 * эта переменная должна быть объявлена.
	 */
	protected void findVarsHosts(Int2ObjectMap<List<VarOwner>> hostsMap) {
		scopes.forEach(scope -> scope.findVarsHosts(hostsMap));

		for (int slotId : hostsMap.keySet()) {
			boolean usesLoad = false,
					usesStore = false;

			Loop: for (Operation operation : getNestedOperations()) {
				switch (operation.getVarUsage(slotId)) {
					case LOAD -> {
						usesLoad = true;
						if (usesStore)
							break Loop;
					}

					case STORE -> {
						usesStore = true;
						if (usesLoad)
							break Loop;
					}
				}
			}

			// Если в данном scope мы и читаем, и записываем переменную,
			// то она должна быть объявлена здесь (или в одном из родителей)
			if (usesLoad & usesStore) {
				var hosts = hostsMap.get(slotId);

				int startId = startChunk.getStartIndex(),
					endId = endChunk.getEndIndex();

				// Удаляем вложенных владельцев
				hosts.removeIf(owner -> owner.start >= startId && owner.end <= endId);

				// Добавляем себя как владельца переменной
				hosts.add(new VarOwner(startId, endId, this));
			}
		}
	}

	/** Добавляет {@code null} в {@link #variables}, если его размер меньше {@code size} */
	protected void addNullVariableIfLess(int size) {
		if (variables.size() < size)
			variables.add(null);

		scopes.forEach(scope -> scope.addNullVariableIfLess(size));
	}

	/** Добавляет переменную в {@link #variables} */
	protected void addVariable(Variable variable) {
		variables.add(variable);
		scopes.forEach(scope -> scope.addVariable(variable));
	}

	public void initVariableNames() {
		for (Variable variable : variables) {
			if (variable != null && variable.getName() == null) {
				variable.setName(getNameFor(variable));
			}
		}

		scopes.forEach(Scope::initVariableNames);
	}

	private String getNameFor(Variable variable) {
		{
			Set<String> names = variable.getNames();

			if (names.size() == 1)
				return names.iterator().next();
		}

		String baseName = variable.getType().getVarName();
		String name = baseName;

		Optional<Variable> sameNameVar =
				findVarByName(baseName).or(() -> findVarByName(baseName + '1'));

		if (sameNameVar.isPresent()) {
			var var = sameNameVar.get();

			if (baseName.equals(var.getName()) && !var.isNameFixed()) {
				sameNameVar.get().setName(baseName + '1');
			}

			name = baseName + '2';

			for (int i = 3; findVarByName(name).isPresent(); i++) {
				name = baseName + i;
			}
		}

		return name;
	}

	/** @return переменную с указанным именем в текущем scope-е или внешних,
	 * или пустой Optional, если такая переменная не найдена. */
	protected Optional<Variable> findVarByName(String name) {
		return variables.stream().filter(namePredicate(name))
				.findAny().or(() -> parent != null ? parent.findVarByName(name) : Optional.empty());
	}

	/** @return {@code true}, если в текущем или дочерних scope-ах есть переменная с указанным именем. */
	public boolean hasVarByNameInDepth(String name) {
		return variables.stream().anyMatch(namePredicate(name)) ||
				scopes.stream().anyMatch(scope -> scope.hasVarByNameInDepth(name));
	}

	private Predicate<Variable> namePredicate(String name) {
		return var -> var != null && name.equals(var.getName());
	}


	/** Удаляет лишние операции, такие как {@code return} в конце void-метода или вызов пустого суперконструктора. */
	public void removeRedundantOperations(MethodContext context) {
		scopes.forEach(scope -> scope.removeRedundantOperations(context));
	}


	@Override
	public boolean declareVariables() {
		var headerOperation = getHeaderOperation();
		boolean result = headerOperation != null && headerOperation.declareVariables();

		var operations = this.operations;

		for (var variable : variablesToDeclare) {
			if (variable.isDeclared()) {
				continue;
			}

			for (int i = 0, size = operations.size(); i < size; i++) {
				Operation operation = operations.get(i);

				if (operation.getVarUsage(variable.getSlotId()) == VarUsage.NONE) {
					continue;
				}

				if (!(operation instanceof Scope)) {
					operation.declareVariables();
				}

				if (!variable.isDeclared()) {
					operations.add(i, new DeclareOperation(variable));
				}

				result = true;
				break;
			}
		}

		// Одиночный "or", так как супер-метод должен вызываться всегда.
		return result | Operation.super.declareVariables();
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}


	@Override
	@MustBeInvokedByOverriders
	public void addImports(ClassContext context) {
		context.addImportsFor(operations);
	}

	@Override
	public boolean isScopeLike() {
		return true;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (labelName != null) {
			out.record(labelName).record(": ");
		}

		if (canOmitBrackets() && context.getConfig().canOmitBrackets()) {
			if (operations.isEmpty()) {
				writeHeader(out, context);
				out.record(';');
				return;
			}

			if (operations.size() == 1 && !operations.get(0).isScopeLike()) {
				writeHeader(out, context);
				out .incIndent().ln().indent()
					.record(operations.get(0), context, Priority.ZERO).record(';')
					.decIndent();
				return;
			}
		}

		if (writeHeader(out, context)) {
			out.space();
		}

		out.record('{').incIndent();

		writeBody(out, context);

		out.decIndent();

		if (!operations.isEmpty()) {
			out.ln().indent();
		}

		out.record('}');
	}


	protected void writeBody(DecompilationWriter out, MethodWriteContext context) {
		Operation prev = null;

		for (var operation : operations) {
			if (shouldJoin(operation, prev)) {
				if (prev instanceof Scope scope && scope.realOmitBrackets(context)) {
					out.ln().indent();
				} else {
					out.space();
				}

			} else {
				if (prev != null && (prev.isScopeLike() || operation.isScopeLike())) {
					out.ln().indent();
				}

				out.ln().indent();
			}

			out.record(operation, context, Priority.ZERO);

			if (!operation.isScopeLike())
				out.record(';');

			prev = operation;
		}
	}


	private boolean shouldJoin(Operation current, Operation prev) {
		return  current instanceof ElseScope && prev instanceof IfScope ||
				current instanceof CatchScope && (prev instanceof TryScope || prev instanceof CatchScope);
	}


	private boolean realOmitBrackets(Context context) { // Oh, rly?
		return canOmitBrackets() && context.getConfig().canOmitBrackets() &&
				(operations.isEmpty() || operations.size() == 1 && !operations.get(0).isScopeLike());
	}

	/**
	 * Записывает заголовок scope.
	 * @return {@code true}, если заголовок записан.
	 * После него будет записан пробел при наличии фигурных скобок.
	 */
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		return false;
	}

	/** Можно ли опустить фигурные скобки,
	 * если scope содержит ноль или одну операцию */
	protected boolean canOmitBrackets() {
		return false;
	}

	@Override
	public int compareTo(@NotNull Scope other) {
		int diff = startIndex - other.startIndex;

		if (diff == 0) {
			diff = other.endChunk.getId() - endChunk.getId();
		}

		// CaseScope всегда должен быть внутри switchScope, к которому он принадлежит
		// и снаружи любого другого scope
		if (diff == 0) {
			if (this instanceof SwitchScope.CaseScope caseScope) {
				diff = caseScope.getSwitchScope() == other ? 1 : -1;

			} else if (other instanceof SwitchScope.CaseScope caseScope) {
				diff = caseScope.getSwitchScope() == this ? -1 : 1;
			}
		}

		if (diff == 0) {
			Log.warn("Undefined sorting order for scopes %s and %s", this, other);
		}

		return diff;
	}

	@Override
	public String toString() {
		return String.format("%s(%d - %d)", getClass().getSimpleName(),
				startChunk.getId(), endChunk.getId());
	}
}
