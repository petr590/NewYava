package x590.newyava.example.construction;

import x590.newyava.example.Main;

@SuppressWarnings("unused")
public interface InterfaceExample {
	int CONSTANT = 1;

	static void main(String[] args) {
		Main.run(InterfaceExample.class);
	}

	int interfaceMethod(int x);

	default int defaultMethod(int x) {
		return x;
	}

	private void privateMethod(int x) {}

	static void staticMethod() {}

	private static void privateStaticMethod() {}
}
