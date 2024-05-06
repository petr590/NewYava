package x590.newyava.decompilation.scope;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Log;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableSlotView;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.*;

/**
 * Область видимости, ограниченная, как правило, фигурными скобками
 */
public class Scope implements Operation, Comparable<Scope> {

	@Getter
	private final Chunk startChunk;

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

	private final List<@Nullable Variable> variables = new ArrayList<>();

	private final @UnmodifiableView List<Variable> variablesView = Collections.unmodifiableList(variables);

	public @UnmodifiableView List<Variable> getVariables() {
		return variablesView;
	}

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

	public void addOperations(Collection<Operation> operations) {
		this.operations.addAll(operations);
	}

	public boolean isEmpty() {
		return operations.isEmpty();
	}


	@Override
	public void inferType(Type ignored) {
		operations.forEach(operation -> operation.inferType(PrimitiveType.VOID));
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return operations;
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
	 * Начинает все scope-ы, до которых дошла очередь.
	 * @param scopes очередь {@link Scope}-ов, должна быть отсортирована по возрастанию.
	 * @param currentId текущий id чанка.
	 */
	public Scope startScopes(Queue<Scope> scopes, int currentId) {
		var scope = scopes.peek();

		if (scope != null && scope.getStartChunk().getId() == currentId) {
			scopes.poll();

			operations.add(scope);
			this.scopes.add(scope);
			scope.parent = this;

			if (scope instanceof IfScope && scope.endChunk.getId() > this.endChunk.getId()) {
				scope.endChunk = this.endChunk;
			}

			return scope.startScopes(scopes, currentId);
		}

		return this;
	}

	/** Завершает все текущие scope-ы, если они достигли конца */
	public Scope endIfReached(int currentId) {
		return currentId >= endChunk.getId() && parent != null ?
				parent.endIfReached(currentId) :
				this;
	}

	/** Связывает с каждым {@link VariableReference} определённый {@link Variable} */
	public void initVariables(@Unmodifiable List<Chunk> chunks) {
		int startChunkId = startChunk.getId(),
			endChunkId = endChunk.getId();

		for (VariableSlotView slot : startChunk.getVarSlots()) {
			Optional<VariableReference> ref =
					parent == null ? Optional.empty() : parent.findVariable(slot.getId());

			for (int id = startChunkId; id <= endChunkId; id++) {
				VariableReference curRef = slot.get(chunks.get(id).getStartIndex());

				if (curRef == null)
					continue;

				if (ref.isPresent()) {
					curRef.initVariable(ref.get().getVariable());

				} else {
					if (curRef.getVariable() == null) {
						curRef.initVariable(new Variable(curRef));
					}

					ref = Optional.of(curRef);
				}
			}

			variables.add(ref.map(VariableReference::getVariable).orElse(null));
		}

		scopes.forEach(scope -> scope.initVariables(chunks));
	}

	private Optional<VariableReference> findVariable(int slotId) {
		return Optional.ofNullable(startChunk.getVarSlots().get(slotId).get(startChunk.getId()))
				.or(() -> parent == null ? Optional.empty() : parent.findVariable(slotId));
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

	protected Optional<Variable> findVarByName(String name) {
		return variables.stream().filter(var -> var != null && name.equals(var.getName())).findAny()
				.or(() -> parent != null ? parent.findVarByName(name) : Optional.empty());
	}


	/** Удаляет лишние операции, такие как {@code return} в конце void-метода или вызов пустого суперконструктора. */
	public void removeRedundantOperations(MethodContext context) {
		scopes.forEach(scope -> scope.removeRedundantOperations(context));
	}


	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operations);
	}

	@Override
	public boolean isScopeLike() {
		return true;
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (canOmitBrackets() && !context.getConfig().isAlwaysWriteBrackets()) {
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
			out.recordSp();
		}

		out.record('{').incIndent();

		writeBody(out, context);

		out.decIndent();

		if (!operations.isEmpty()) {
			out.ln().indent();
		}

		out.record('}');
	}


	protected void writeBody(DecompilationWriter out, Context context) {
		Operation prev = null;

		for (var operation : operations) {
			if (operation instanceof ElseScope) {
				if (prev instanceof Scope scope && scope.realOmitBrackets(context)) {
					out.ln().indent();
				} else {
					out.recordSp();
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


	private boolean realOmitBrackets(Context context) { // Oh, rly?
		return canOmitBrackets() && !context.getConfig().isAlwaysWriteBrackets() &&
				(operations.isEmpty() || operations.size() == 1 && !operations.get(0).isScopeLike());
	}

	/**
	 * Записывает заголовок scope.
	 * @return {@code true}, если заголовок записан.
	 * После него будет записан пробел при наличии фигурных скобок.
	 */
	protected boolean writeHeader(DecompilationWriter out, Context context) {
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
