package x590.newyava.example.code.scope;

import x590.newyava.example.Main;

import java.util.function.BooleanSupplier;

@SuppressWarnings("unused")
public class LambdaInFieldExample {
	public static void main(String[] args) {
		Main.runForCaller();
	}

	private final BooleanSupplier supplier = () -> false;

	public LambdaInFieldExample() {}

	public LambdaInFieldExample(int x) {}
}
