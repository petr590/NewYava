package x590.newyava.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import x590.newyava.decompilation.Chunk;
import x590.newyava.decompilation.CodeStack;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

@Getter
@RequiredArgsConstructor
public class MethodContext {

	private final MethodDescriptor descriptor;

	private final ClassContext classContext;

	private final int modifiers;

	private final CodeStack stack = new CodeStack();

	@Setter
	@Getter(AccessLevel.NONE)
	private Chunk currentChunk;

	public int getClassModifiers() {
		return classContext.getClassModifiers();
	}

	public ReferenceType getThisType() {
		return classContext.getThisType();
	}

	public ClassType getSuperType() {
		return classContext.getSuperType();
	}

	public boolean isConstructor() {
		return descriptor.isConstructor();
	}

	public VariableReference getVariable(int slotId) {
		return currentChunk.getVariableRef(slotId);
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
	 * Иначе возвращает {@code false} */
	public boolean popIfSame(Operation operation) {
		if (!stack.isEmpty() && stack.peek() == operation) {
			stack.pop();
			return true;
		}

		return false;
	}
}
