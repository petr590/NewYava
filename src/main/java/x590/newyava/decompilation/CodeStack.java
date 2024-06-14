package x590.newyava.decompilation;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.Stack;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.ProxyOperation;
import x590.newyava.exception.EmptyStackException;
import x590.newyava.exception.TypeSizeNotMatchesException;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.*;
import java.util.stream.Collectors;

public class CodeStack implements Stack<Operation> {
	private Deque<Operation> stack = new LinkedList<>();

	/** Преобразует каждую операцию в {@link ProxyOperation}.
	 * @return неизменяемый список из преобразованных операций. */
	@SuppressWarnings("unchecked")
	public @Unmodifiable List<ProxyOperation> makeProxyOperations() {
		if (stack.isEmpty())
			return List.of();

		stack = stack.stream()
				.map(operation -> operation instanceof ProxyOperation proxy ? proxy : new ProxyOperation(operation))
				.collect(Collectors.toCollection(LinkedList::new));

		return List.copyOf((Collection<ProxyOperation>)(Collection<?>) stack);
	}

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

	/**
	 * Снимает со стека операцию и проверяет её возвращаемый тип на совместимость с требуемым типом.
	 * @throws EmptyStackException если стек пуст.
	 * @throws x590.newyava.exception.TypeCastException если возвращаемый тип операции не совместим с требуемым.
	 */
	public Operation popAs(Type requiredType) {
		var operation = pop();
		Type.assignDown(operation.getReturnType(), requiredType); // FIRST_TYPE_ASSIGNMENT
		return operation;
	}

	/**
	 * Снимает со стека операцию и проверяет размер её типа.
	 * @throws EmptyStackException если стек пуст.
	 * @throws TypeSizeNotMatchesException если размер типа операции не совпадает с требуемым.
	 */
	public Operation popAs(TypeSize requiredSize) {
		var operation = pop();

		var size = operation.getReturnType().getSize();
		if (size != requiredSize) throw new TypeSizeNotMatchesException(size, requiredSize);

		return operation;
	}

	/**
	 * Снимает со стека одну операцию, если размер её типа равен переданному.
	 * Если передан {@link TypeSize#LONG} и на стеке две операции размером {@link TypeSize#WORD},
	 * то снимает со стека две операции.
	 * @return одну или две операции, снятые со стека.
	 * @throws EmptyStackException если стек пуст.
	 * @throws TypeSizeNotMatchesException если операции на стеке не соответствуют переданному размеру.
	 */
	public Pair<Operation, @Nullable Operation> popOneOrTwo(TypeSize size) {
		var operation1 = pop();
		var size1 = operation1.getReturnType().getSize();

		if (size1 == size) {
			return Pair.of(operation1, null);
		}

		if (size1 == TypeSize.WORD && size == TypeSize.LONG) {
			return Pair.of(operation1, popAs(TypeSize.WORD));
		}

		throw new TypeSizeNotMatchesException(size1, size);
	}

	/**
	 * Если вторая операция не равна {@code null}, то кладёт её на стек.
	 * Затем кладёт первую операцию.
	 */
	public void pushOneOrTwo(Pair<Operation, @Nullable Operation> pair) {
		if (pair.second() != null)
			push(pair.second());

		push(pair.first());
	}

	@Override
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	@Override
	public Operation top() {
		return peek();
	}

	public Operation peek() {
		return stack.peek();
	}

	/** Не поддерживается */
	@Override
	@Deprecated
	public Operation peek(int i) {
		return Stack.super.peek(i);
	}


	@Override
	public String toString() {
		return stack.toString();
	}
}
