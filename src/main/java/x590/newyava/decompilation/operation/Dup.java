package x590.newyava.decompilation.operation;

import x590.newyava.decompilation.CodeStack;
import x590.newyava.type.TypeSize;

public class Dup {
	private Dup() {}

	public static void dup(CodeStack stack, TypeSize size) {
		var value = stack.popOneOrTwo(size);
		stack.pushOneOrTwo(value);
		stack.pushOneOrTwo(value);
	}

	public static void dupX1(CodeStack stack, TypeSize size1, TypeSize size2) {
		var value1 = stack.popOneOrTwo(size1);
		var value2 = stack.popOneOrTwo(size2);

		stack.pushOneOrTwo(value1);
		stack.pushOneOrTwo(value2);
		stack.pushOneOrTwo(value1);
	}

	public static void dupX2(CodeStack stack) {
		var value1 = stack.popAs(TypeSize.WORD);
		var value2 = stack.popAs(TypeSize.WORD);
		var value3 = stack.popAs(TypeSize.WORD);

		stack.push(value1);
		stack.push(value3);
		stack.push(value2);
		stack.push(value1);
	}
}
