package x590.newyava.decompilation.code;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.Stack;
import org.jetbrains.annotations.Nullable;
import x590.newyava.decompilation.operation.other.DummyOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.other.ProxyOperation;
import x590.newyava.exception.TypeSizeNotMatchesException;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class CodeStack implements Stack<Operation> {
	/** Состояние стека */
	private Deque<Operation> stack = new LinkedList<>();

	/** Операции, которые были созданы при вызове метода {@link #pop()} на пустом стеке. */
	private List<ProxyOperation> popped = new ArrayList<>();


	/** Сбрасывает поле {@link #stack} и возвращает его предыдущее состояние. */
	Deque<Operation> getAndResetPushedOperations() {
		var result = stack;
		stack = new LinkedList<>();
		return result;
	}


	/** Сбрасывает поле {@link #popped} и возвращает его предыдущее состояние. */
	List<ProxyOperation> getAndResetPoppedOperations() {
		var result = popped;
		popped = new ArrayList<>();
		return result;
	}


	@Override
	public void push(Operation operation) {
		stack.push(operation);
	}

	/**
	 * Если стек не пуст, то снимает операцию с вершины стека и возвращает её.
	 * Иначе создаёт новую операцию, добавляет её в {@link #popped} и возвращает.
	 */
	@Override
	public Operation pop() {
		if (!stack.isEmpty()) {
			return stack.pop();
		}

		var operation = new ProxyOperation(DummyOperation.INSTANCE);
		popped.add(operation);
		return operation;
	}

	/**
	 * Снимает со стека операцию и проверяет её возвращаемый тип на совместимость с требуемым типом.
	 * @throws x590.newyava.exception.TypeCastException если возвращаемый тип операции не совместим с требуемым.
	 */
	public Operation popAs(Type requiredType) {
		var operation = pop();
		Type.assignDown(operation.getReturnType(), requiredType); // FIRST_TYPE_ASSIGNMENT
		return operation;
	}

	/**
	 * Снимает со стека операцию и проверяет размер её типа.
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
	public @Nullable Operation top() {
		return peek();
	}

	public @Nullable Operation peek() {
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
