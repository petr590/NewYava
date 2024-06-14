package x590.newyava.example;

@SuppressWarnings("unused")
public interface InterfaceExample {
	int CONSTANT = 1;

	static void main(String[] args) {
		Main.run(InterfaceExample.class);
	}

	int exampleMethod1(int x);

	default int exampleMethod2(int x) {
		return x;
	}

	static void exampleStaticMethod1() {}

	private static void exampleStaticMethod2() {}
}
