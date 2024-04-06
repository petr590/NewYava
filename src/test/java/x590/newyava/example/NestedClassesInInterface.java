package x590.newyava.example;

import x590.newyava.Main;

@SuppressWarnings("unused")
public interface NestedClassesInInterface {

	static void main(String[] args) {
		Main.run(NestedClassesInInterface.class);
	}

	final class Nested {}

	enum EmptyEnum {}

	enum JustEnum {
		A, B, C
	}

	enum NestedEnum {
		A, B, C;

		private static final int X = (int)(Math.PI * 100);

		NestedEnum() {}

		NestedEnum(int x) {}

		static class Inner1 {}

		static class Inner2 {}
	}
}
