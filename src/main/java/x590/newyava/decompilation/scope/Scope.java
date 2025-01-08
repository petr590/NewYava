package x590.newyava.decompilation.scope;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Log;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.emptyscope.EmptyableScopeOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.variable.DeclareOperation;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.*;
import java.util.function.Predicate;

/**
 * Область видимости, ограниченная, как правило, фигурными скобками
 */
public non-sealed class Scope implements EmptyableScopeOperation, Comparable<Scope> {

	/** Первый чанк, принадлежащий scope-у. */
	@Getter
	private final Chunk startChunk;

	/** Последний чанк, принадлежащий scope-у. */
	@Getter
	private Chunk endChunk;

	/** Список всех чанков, принадлежащих scope-у. */
	@Getter
	private @Unmodifiable List<Chunk> chunks;

	/** Индекс операции, на которой начинается scope. <b>Не равен</b> {@code startChunk.getId()}.<br>
	 * Необходим для правильной сортировки, так как иногда scope-ы могут
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

	private final @UnmodifiableView List<Scope> scopesView = Collections.unmodifiableList(scopes);

	public @UnmodifiableView List<Scope> getScopes() {
		return scopesView;
	}


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
	 * @param chunks список чанков, которые захватил этот scope.
	 * @param startIndexOffset смещение, которое прибавляется к начальному индексу первого чанка,
	 *                         чтобы получить значение {@link #startIndex}. Как правило, равно 0 или -1.
	 */
	public Scope(@Unmodifiable List<Chunk> chunks, int startIndexOffset) {
		if (chunks.isEmpty()) {
			throw new IllegalArgumentException("Empty chunk list");
		}

		this.startChunk = chunks.get(0);
		this.endChunk = Utils.getLast(chunks);
		this.chunks = chunks;
		this.startIndex = startChunk.getStartIndex() + startIndexOffset;
	}

	public boolean isEmpty() {
		return operations.isEmpty();
	}


	/** Удаляет последнюю операцию, если она соответствует предикату.
	 * Если список операций пуст, ничего не делает.
	 * @return {@code true}, если операция была удалена. */
	public boolean removeLastOperationIf(Predicate<Operation> predicate) {
		if (Utils.isLast(operations, predicate)) {
			Utils.removeLast(operations);
			return true;
		}

		return false;
	}


	private final Int2ObjectMap<VarUsage> usagesCache = new Int2ObjectArrayMap<>();

	/** Если необходимо изменить алгоритм вычисления VarUsage, переопределите метод {@link #computeVarUsage(int)} */
	@Override
	public final VarUsage getVarUsage(int slotId) {
		return usagesCache.computeIfAbsent(slotId, this::computeVarUsage);
	}

	protected VarUsage computeVarUsage(int slotId) {
		return EmptyableScopeOperation.super.getVarUsage(slotId);
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
		return headerOperation == null ? operationsView :
				Utils.addBefore(headerOperation, operationsView);
	}

	/** @return операцию в заголовке scope или {@code null}, если её нет.
	 * По умолчанию возвращает {@code null}. */
	protected @Nullable Operation getHeaderOperation() {
		return null;
	}


	/** Можно ли к данному scope-у применить {@code break} */
	public boolean isBreakable() {
		return false;
	}


	/** Можно ли к данному scope-у применить {@code continue} */
	public boolean isContinuable() {
		return false;
	}


	private @Nullable String labelName;

	public String getLabelName(LabelNameGenerator generator) {
		return labelName != null ? labelName :
				(labelName = generator.nextLabelName());
	}



	/** Добавляет операции из чанка в список операций. */
	public void addOperationsFromChunk(Chunk chunk) {
		operations.addAll(chunk.getOperations());
	}


	/** Если {@code true}, то уменьшает границу scope до границ внешнего scope. */
	public boolean canShrink() {
		return true;
	}


	/**
	 * Начинает все scope-ы, до которых дошла очередь.
	 * Если вложенный scope выходит за границы текущего и метод {@link #canShrink()} возвращает {@code true},
	 * то вложенный scope будет уменьшен до этих границ.
	 * @param scopeQueue очередь {@link Scope}-ов, должна быть отсортирована по возрастанию.
	 * @param currentId текущий id чанка.
	 */
	public final Scope startScopes(Queue<Scope> scopeQueue, int currentId) {
		var scope = scopeQueue.peek();

		if (scope != null && scope.getStartChunk().getId() == currentId) {
			scopeQueue.poll();

			operations.add(scope);
			scopes.add(scope);

			scope.parent = this;

			int diff = scope.endChunk.getId() - this.endChunk.getId();

			if (diff > 0 && scope.canShrink()) {
				scope.chunks = scope.chunks.subList(0, scope.chunks.size() - diff);
				scope.endChunk = this.endChunk;
			}

			scope.onStart();

			return scope.startScopes(scopeQueue, currentId);
		}

		return this;
	}

	/** Завершает все текущие scope-ы, если они достигли конца */
	public final Scope endIfReached(int currentId, @Unmodifiable List<Chunk> allChunks) {
		if (currentId >= endChunk.getId() && parent != null) {
			if (currentId > endChunk.getId()) {
				var newChunks = allChunks.subList(endChunk.getId() + 1, currentId + 1);
				chunks = allChunks.subList(startChunk.getId(), currentId + 1);
				endChunk = Utils.getLast(chunks);
				onExpand(newChunks);
			}

			onEnd();
			return parent.endIfReached(currentId, allChunks);
		}

		return this;
	}


	/**
	 * Вызывается, при расширении текущего scope, до {@link #onEnd()}.
	 * При этом {@link #chunks} и {@link #endChunk} обновляются до вызова этого метода.
	 * @param newChunks список добавленных чанков.
	 */
	protected void onExpand(@Unmodifiable List<Chunk> newChunks) {}

	/** Вызывается, когда текущий scope начинается. */
	protected void onStart() {}


	/** Вызывается, когда текущий scope достигает конца. */
	protected void onEnd() {}


	@Data
	protected static final class VarOwner {
		/** Индекс инструкций начала и конца */
		private final int start, end;

		/** Scope, которому принадлежит переменная */
		private final Scope scope;

		@Getter(AccessLevel.NONE)
		@Setter(AccessLevel.NONE)
		private @Nullable Variable variable;

		public Variable getOrCreateVariable(VariableReference ref) {
			if (variable != null)
				return variable;

			return variable = new Variable(ref, false);
		}
	}


	/**
	 * Ищет для каждой переменной своего "владельца" - т.е. scope,
	 * в котором эта переменная должна быть объявлена.
	 * @param ownersMap карта: ключ - слот переменной, значение - список всех
	 *                  владельцев этой переменной (они не должны пересекаться).
	 */
	protected void findVarsOwners(Int2ObjectMap<List<VarOwner>> ownersMap) {
		scopes.forEach(scope -> scope.findVarsOwners(ownersMap));

		for (int slotId : ownersMap.keySet()) {
			var owners = ownersMap.get(slotId);

			int currentIndex = startChunk.getStartIndex();

			// id начала и конца переменной
			int startIndex = currentIndex;
			int endIndex = currentIndex;

			// Если true, то данную переменную необходимо объявить в текущем scope
			boolean needDefine = false;

			for (Operation operation : getNestedOperations()) {
				var scope = operation instanceof Scope ? (Scope) operation : null;

				if (scope != null) {
					currentIndex = scope.getStartChunk().getStartIndex();
				}

				var usage = operation.getVarUsage(slotId);

//				if (slotId == 2) {
//					System.out.printf("%d %d..%d, %s, %s\n",
//							currentIndex, startIndex, endIndex, usage, operation); // DEBUG
//				}

				switch (usage) {
					case LOAD -> needDefine = true;

					case STORE -> {
						if (needDefine) {
							addOwner(owners, startIndex, endIndex);
						}

						// Сбрасываем всё
						startIndex = endIndex = currentIndex;
						needDefine = scope == null;
					}
				}

				if (scope != null) {
					currentIndex = scope.getEndChunk().getEndIndex();
				}

				if (usage == VarUsage.LOAD) {
					endIndex = currentIndex;
				}
			}

			if (needDefine) {
//				endIndex = endChunk.getEndIndex();
				addOwner(owners, startIndex, endIndex);
			}
		}

//		System.out.println(); // DEBUG
	}

	/** Удаляет вложенных владельцев и добавляет себя как владельца переменной. */
	private void addOwner(List<VarOwner> owners, int startIndex, int endIndex) {
		owners.removeIf(owner -> owner.start >= startIndex && owner.end <= endIndex);
		owners.add(new VarOwner(startIndex, endIndex, this));
	}

	/** Добавляет {@code null} в {@link #variables}, если его размер меньше {@code size}.
	 * Выполняет это также для всех вложенных scope-ов. */
	protected void addNullVariableIfLess(int size) {
		if (variables.size() < size)
			variables.add(null);

		scopes.forEach(scope -> scope.addNullVariableIfLess(size));
	}

	/** Добавляет переменную в {@link #variables} */
	protected void addVariable(@Nullable Variable variable) {
		variables.add(variable);
		scopes.forEach(scope -> scope.addVariable(variable));
	}

	/** Инициализирует имена всех переменных так, чтобы они не повторялись */
	public void initVariableNames() {
		for (Variable variable : variables) {
			if (variable != null && variable.getName() == null) {
				variable.setName(getNameFor(variable));
			}
		}

		scopes.forEach(Scope::initVariableNames);
	}

	private String getNameFor(Variable variable) {
		String baseName = variable.getBaseName();

		Optional<Variable> sameNameVar =
				findVarByName(baseName).or(() -> findVarByName(addNum(baseName, 1)));

		if (sameNameVar.isPresent()) {
			var var = sameNameVar.get();

			if (baseName.equals(var.getName()) && !var.isNameFixed()) {
				sameNameVar.get().setName(addNum(baseName, 1));
			}

			String name = addNum(baseName, 2);

			for (int i = 3; findVarByName(name).isPresent(); i++) {
				name = addNum(baseName, i);
			}

			return name;
		}

		return baseName;
	}

	private String addNum(String base, int num) {
		// Если имя класса заканчивается цифрой, то нужно отделить его от номера переменной
		return Character.isDigit(base.charAt(base.length() - 1)) ?
				base + "_" + num :
				base + num;
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


	/** Удаляет лишние операции, такие как {@code return} в конце void-метода,
	 * а также распознаёт специальные конструкции, сгенерированные компилятором,
	 * например, {@code switch(enum)}.
	 * Вызывается <i>сразу</i> после декомпиляции данного метода. */
	@MustBeInvokedByOverriders
	public void postDecompilation(MethodContext context) {
		scopes.forEach(scope -> scope.postDecompilation(context));
	}

	/** Завершает то, что невозможно было сделать в {@link #postDecompilation(MethodContext)}.
	 * Вызывается после декомпиляции <i>всех</i> классов. */
	@MustBeInvokedByOverriders
	public void afterDecompilation(MethodContext context) {
		scopes.forEach(scope -> scope.afterDecompilation(context));

		var operations = this.operations;

		for (int i = 1; i < operations.size(); ) {
			if (operations.get(i).canUnite(context, operations.get(i - 1))) {
				operations.remove(i - 1);
			} else {
				i++;
			}
		}
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

				if (!operation.usesVariable(variable)) {
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
		return result | EmptyableScopeOperation.super.declareVariables();
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	/** Удаляет последнюю операцию {@code continue}, если она соответствует {@code loop}.
	 * @return {@code true}, если можно продолжить вызывать этот метод у предыдущих scope-ов. */
	public boolean removeLastContinueOfLoop(LoopScope loop) {
		return false;
	}


	@Override
	public void initYield(Scope switchScope, Deque<Operation> ignored) {
		for (var iter = new ReverseListIterator<>(operations); iter.hasNext(); ) {
			iter.next().initYield(switchScope, getEndChunk().getPushedOperations());
		}
	}


	@Override
	@MustBeInvokedByOverriders
	public void addImports(ClassContext context) {
		context.addImportsFor(operations);
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
				writeFooter(out, context, true);
				return;
			}

			if (operations.size() == 1 && !operations.get(0).needWrapWithBrackets()) {
				writeHeader(out, context);

				out .incIndent().ln().indent()
					.record(operations.get(0), context, Priority.ZERO).record(';')
					.decIndent();

				writeFooter(out, context, true);
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

		writeFooter(out, context, false);
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
				if (prev != null && (prev.needEmptyLinesAround() || operation.needEmptyLinesAround())) {
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


	private boolean shouldJoin(Operation current, @Nullable Operation prev) {
		return  current instanceof ElseScope && prev instanceof IfScope ||
				current instanceof CatchScope && (prev instanceof TryScope || prev instanceof CatchScope);
	}


	private boolean realOmitBrackets(Context context) { // Oh, rly?
		return canOmitBrackets() && context.getConfig().canOmitBrackets() &&
				(operations.isEmpty() || operations.size() == 1 && !operations.get(0).needWrapWithBrackets());
	}

	/**
	 * Записывает заголовок scope.
	 * @return {@code true}, если заголовок записан.
	 * В таком случае после заголовка будет записан пробел при наличии фигурных скобок.
	 */
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		return false;
	}

	/**
	 * Записывает заголовок после тела scope.
	 * @param bracketsOmitted {@code true}, если фигурные скобки были опущены, иначе {@code false}.
	 */
	protected void writeFooter(DecompilationWriter out, MethodWriteContext context, boolean bracketsOmitted) {}

	/**
	 * Можно ли опустить фигурные скобки, если scope содержит ноль или одну операцию.
	 * По умолчанию {@code false}.
	 */
	protected boolean canOmitBrackets() {
		return false;
	}


	private static final int
			INSIDE = 1,
			OUTSIDE = -1;

	@Override
	public int compareTo(Scope other) {
		int diff = startIndex - other.startIndex;
		if (diff != 0) return diff;

		diff = other.endChunk.getId() - endChunk.getId();
		if (diff != 0) return diff;

		// CaseScope всегда должен быть внутри SwitchScope, к которому он принадлежит
		// и снаружи любого другого scope
		if (this instanceof SwitchScope.CaseScope caseScope) {
			return caseScope.getSwitchScope() == other ? INSIDE : OUTSIDE;
		} else if (other instanceof SwitchScope.CaseScope caseScope) {
			return caseScope.getSwitchScope() == this ? OUTSIDE : INSIDE;
		}

		// TryCatchScope должен быть внутри JoiningTryCatchScope, к которому он принадлежит
		// и снаружи любого другого scope
		if (this instanceof TryCatchScope tryCatch) {
			return tryCatch.getJoiningScope() == other ? INSIDE : OUTSIDE;
		} else if (other instanceof TryCatchScope tryCatch) {
			return tryCatch.getJoiningScope() == this ? OUTSIDE : INSIDE;
		}

		Log.warn("Undefined sorting order for scopes %s and %s", this, other);
		return diff;
	}

	@Override
	public String toString() {
		return String.format("%s(%d - %d)", getClass().getSimpleName(),
				startChunk.getId(), endChunk.getId());
	}
}
