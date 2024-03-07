package x590.newyava.example;

import x590.newyava.Main;

@SuppressWarnings("unused")
public interface Interface {
	int CONSTANT = 1;

	static void main(String[] args) {
		Main.run(Interface.class);
	}

	int exampleMethod1(int x);

	default int exampleMethod2(int x) {
		return x;
	}

	static void exampleStaticMethod1() {}

	private static void exampleStaticMethod2() {}
}
