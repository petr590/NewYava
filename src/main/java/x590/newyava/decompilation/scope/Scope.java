package x590.newyava.decompilation.scope;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.*;

/**
 * Область видимости, ограниченная, как правило, фигурными скобками
 */
public class Scope implements Operation {
	@Getter
	private final Chunk startChunk, endChunk;

	@Getter
	private @Nullable Scope parent;

	protected final List<Operation> operations = new ArrayList<>();

	private final List<Scope> scopes = new ArrayList<>();

	private final List<Variable> variables = new ArrayList<>();

	private final @Unmodifiable List<Variable> variablesView = Collections.unmodifiableList(variables);

	public @Unmodifiable List<Variable> getVariables() {
		return variablesView;
	}

	public Scope(@Unmodifiable List<Chunk> chunks) {
		this.startChunk = chunks.get(0);
		this.endChunk = chunks.get(chunks.size() - 1);
	}

	public void addOperations(Collection<Operation> operations) {
		this.operations.addAll(operations);
	}

	public Scope startScope(Scope scope) {
		operations.add(scope);
		scopes.add(scope);
		scope.parent = this;
		return scope;
	}

	public Scope endScope(Scope scope) {
		if (scope != this) {
			throw new DecompilationException("Scopes are not matches: " + scope + ", " + this);
		}

		return Objects.requireNonNull(parent);
	}


	/** Инициализирует {@link Variable} из {@link VariableReference} */
	public void initVariables() {
		int startChunkId = startChunk.getId(),
			endChunkId = endChunk.getId();

		for (var slot : startChunk.getLocalVars()) {
			Optional<VariableReference> ref =
					parent == null ? Optional.empty() : parent.findVariable(slot.getId());

			for (int i = startChunkId; i <= endChunkId; i++) {
				VariableReference curRef = slot.get(startChunkId);

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

		scopes.forEach(Scope::initVariables);
	}

	private Optional<VariableReference> findVariable(int slotId) {
		return Optional.ofNullable(startChunk.getLocalVars().get(slotId).get(startChunk.getId()))
				.or(() -> parent == null ? Optional.empty() : parent.findVariable(slotId));
	}

	private String getNameFor(VariableReference ref) {
		String name = ref.getInitialName();

		if (name != null) return name;

		String baseName = ref.getType().getVarName();

		name = baseName;

		for (int i = 1; this.hasVarWithName(name); i++) {
			name = baseName + i;
		}

		return name;
	}

	private boolean hasVarWithName(String name) {
		return variables.stream().anyMatch(var -> var.name().equals(name)) ||
				parent != null && parent.hasVarWithName(name);
	}


	/** Удаляет лишние операции, такие как {@code return} в конце void-метода */
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
	public void write(DecompilationWriter out, ClassContext context) {
		if (canOmitBrackets()) {
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

		out.decIndent();

		if (!operations.isEmpty()) {
			out.ln().indent();
		}

		out.record('}');
	}

	private boolean realOmitBrackets() {
		return canOmitBrackets() &&
				(operations.isEmpty() || operations.size() == 1 && !operations.get(0).isScopeLike());
	}

	/**
	 * Записывает заголовок scope.
	 * @return {@code true}, если заголовок записан.
	 * После него будет записан пробел, если есть фигурные скобки.
	 */
	protected boolean writeHeader(DecompilationWriter out, ClassContext context) {
		return false;
	}

	/** Можно ли опустить фигурные скобки,
	 * если scope содержит 0 или 1 операцию */
	protected boolean canOmitBrackets() {
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s(%d - %d)",
				getClass().getSimpleName(), startChunk.getId(), endChunk.getId());
	}
}
