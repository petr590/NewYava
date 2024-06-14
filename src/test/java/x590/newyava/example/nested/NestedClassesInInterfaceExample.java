package x590.newyava.example.nested;

import x590.newyava.example.Main;

@SuppressWarnings("unused")
public interface NestedClassesInInterfaceExample {

	static void main(String[] args) {
		Main.run(NestedClassesInInterfaceExample.class);
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

		public static class Inner2 {}
	}
}
