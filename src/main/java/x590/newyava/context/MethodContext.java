package x590.newyava.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.code.CodeStack;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.function.Predicate;

/**
 * Предоставляет доступ к основным свойствам метода, стеку и локальным переменным.
 * Используется при декомпиляции кода.
 */
@Getter
public class MethodContext extends ContextProxy {

	private final MethodDescriptor descriptor;

	private final int modifiers;

	private final CodeStack stack = new CodeStack();

	@Setter
	@Getter(AccessLevel.NONE)
	private Chunk currentChunk;

	public MethodContext(Context context, MethodDescriptor descriptor, int modifiers) {
		super(context);
		this.descriptor = descriptor;
		this.modifiers = modifiers;
	}

	public boolean isConstructor() {
		return descriptor.isConstructor();
	}

	public boolean isStaticInitializer() {
		return descriptor.isStaticInitializer();
	}

	public VariableReference getVarRef(int slotId) {
		return currentChunk.getVarRef(slotId);
	}

	public Operation popAs(Type requiredType) {
		return stack.popAs(requiredType);
	}

	public Operation popAs(TypeSize size) {
		return stack.popAs(size);
	}

	public Operation peek() {
		return stack.peek();
	}

	public boolean isStackEmpty() {
		return stack.isEmpty();
	}

	/** Если операция на вершине стека равна {@code operation},
	 * то убирает её со стека и возвращает {@code true}.
	 * Иначе возвращает {@code false}. */
	public boolean popIfSame(Operation operation) {
		return popIf(op -> op == operation) != null;
	}

	/**
	 * Если стек не пуст и предикат возвращает {@code true},
	 * то убирает верхнюю операцию со стека и возвращает её.
	 * Иначе возвращает {@code null}.
	 * @param predicate предикат, в который передаётся операция, лежащая на вершине стека.
	 * @return операцию, которая была убрана со стека или {@code null}
	 */
	public @Nullable Operation popIf(Predicate<Operation> predicate) {
		var stack = this.stack;

		if (!stack.isEmpty() && predicate.test(stack.peek())) {
			return stack.pop();
		}

		return null;
	}
}
