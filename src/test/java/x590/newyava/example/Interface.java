package x590.newyava.example;

import x590.newyava.Main;

@SuppressWarnings("all")
public interface Interface {

	static void main(String[] args) {
		Main.run(Interface.class);
	}

	int exampleMethod(int x);

	default int exampleMethod2(int x) {
		return x;
	}
}
