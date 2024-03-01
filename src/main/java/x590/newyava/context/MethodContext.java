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

	public ReferenceType getThisType() {
		return classContext.getThisType();
	}

	public ClassType getSuperType() {
		return classContext.getSuperType();
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
}
