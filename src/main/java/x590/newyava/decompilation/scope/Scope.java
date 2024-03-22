package x590.newyava.decompilation.scope;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Config;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
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
		this.startChunk = chunks.get(0);
		this.endChunk = chunks.get(chunks.size() - 1);
	}

	public void addOperations(Collection<Operation> operations) {
		this.operations.addAll(operations);
	}

	public boolean isEmpty() {
		return operations.isEmpty();
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


	/** Инициализирует {@link #variables} из {@link VariableReference}, которые хранятся в чанках */
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
					curRef.initVariable(new Variable(curRef.getType(), getNameFor(curRef)));
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

	private String getNameFor(VariableReference ref) {
		String name = ref.getInitialName();

		if (name != null) return name;

		String baseName = ref.getType().getVarName();
		name = baseName;

		Optional<Variable> sameNameVar =
				findVarWithName(baseName).or(() -> findVarWithName(baseName + '1'));

		if (sameNameVar.isPresent()) {
			if (sameNameVar.get().getName().equals(baseName))
				sameNameVar.get().setName(baseName + '1');

			name = baseName + '2';

			for (int i = 3; findVarWithName(name).isPresent(); i++) {
				name = baseName + i;
			}
		}

		return name;
	}

	private Optional<Variable> findVarWithName(String name) {
		return variables.stream().filter(var -> var != null && var.getName().equals(name)).findAny()
				.or(() -> parent != null ? parent.findVarWithName(name) : Optional.empty());
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
	public void write(DecompilationWriter out, WriteContext context) {
		if (canOmitBrackets() && !Config.getConfig().isAlwaysWriteBrackets()) {
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
			out.recordsp();
		}

		out.record('{').incIndent();

		writeBody(out, context);

		out.decIndent();

		if (!operations.isEmpty()) {
			out.ln().indent();
		}

		out.record('}');
	}


	protected void writeBody(DecompilationWriter out, WriteContext context) {
		Operation prev = null;

		for (var operation : operations) {
			if (operation instanceof ElseScope) {
				if (prev instanceof Scope scope && scope.realOmitBrackets()) {
					out.ln().indent();
				} else {
					out.recordsp();
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


	private boolean realOmitBrackets() { // Oh, rly?
		return canOmitBrackets() && !Config.getConfig().isAlwaysWriteBrackets() &&
				(operations.isEmpty() || operations.size() == 1 && !operations.get(0).isScopeLike());
	}

	/**
	 * Записывает заголовок scope.
	 * @return {@code true}, если заголовок записан.
	 * После него будет записан пробел, если есть фигурные скобки.
	 */
	protected boolean writeHeader(DecompilationWriter out, WriteContext context) {
		return false;
	}

	/** Можно ли опустить фигурные скобки,
	 * если scope содержит 0 или 1 операцию */
	protected boolean canOmitBrackets() {
		return false;
	}

	@Override
	public int compareTo(@NotNull Scope other) {
		int d = startChunk.getId() - other.startChunk.getId();
		return d != 0 ? d : other.endChunk.getId() - endChunk.getId();
	}

	@Override
	public String toString() {
		return String.format("%s(%d - %d)",
				getClass().getSimpleName(), startChunk.getId(), endChunk.getId());
	}
}
