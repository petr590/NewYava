package x590.newyava.decompilation;

import it.unimi.dsi.fastutil.Stack;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.exception.EmptyStackException;
import x590.newyava.exception.TypeSizeNotMatchesException;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class CodeStack implements Stack<Operation> {

	private final Deque<Operation> stack = new LinkedList<>();

	@Override
	public void push(Operation operation) {
		stack.push(operation);
	}

	@Override
	public Operation pop() {
		try {
			return stack.pop();
		} catch (NoSuchElementException ex) {
			throw new EmptyStackException();
		}
	}

	public Operation popAs(Type requiredType) {
		var operation = pop();
		operation.updateReturnType(Type.assign(operation.getReturnType(), requiredType));
		return operation;
	}

	public Operation popAs(TypeSize requiredSize) {
		var operation = pop();

		var size = operation.getReturnType().getSize();
		if (size != requiredSize) throw new TypeSizeNotMatchesException(size, requiredSize);

		return operation;
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public Operation top() {
		return stack.peek();
	}

	/** Не поддерживается */
	@Override
	@Deprecated
	public Operation peek(int i) {
		return Stack.super.peek(i);
	}
}
