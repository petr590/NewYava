package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.InterfaceExample;
import x590.newyava.example.Main;

import java.util.function.*;

@SuppressWarnings("all")
public class LambdaExample implements InterfaceExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(true).build());
	}

	public Function<Integer, String> lambdaReference() {
		return String::valueOf;
	}

	public IntFunction<String> lambdaWithCode() {
		return i -> Integer.toHexString(i) + "h";
	}

	public IntFunction<String> lambdaWithCapturedVars(int x, long l) {
		return i -> Integer.toHexString(i) + "h (" + x + ", " + l + ")";
	}

	public IntFunction<String> nestedLambda(int x, long l, double d) {
		return i -> Integer.toHexString(i) + x + l + d + get(() -> "" + x + l);
	}

	public Supplier<String> objectLambda(Object obj) {
		return obj::toString;
	}

	public Function<Object, String> staticLambda() {
		return Object::toString;
	}

	public Supplier<String> thisLambda() {
		return this::toString;
	}

	public Supplier<String> superLambda() {
		return super::toString;
	}

	public Supplier<String> superInterfaceLambda() {
		return InterfaceExample.super::toString;
	}

	public IntUnaryOperator superInterfaceLambda2() {
		return InterfaceExample.super::exampleMethod2;
	}

	// Не должна заменяться ссылкой на метод
	public IntSupplier superInterfaceLambda3() {
		return () -> InterfaceExample.super.exampleMethod2(2);
	}

	@Override
	public int exampleMethod1(int x) {
		return 0;
	}

	private String get(Supplier<String> supplier) {
		return supplier.get();
	}

	public Function<String, String> lambdaNew() {
		return String::new;
	}

	public Function<Integer, int[]> lambdaNewArray() {
		return int[]::new;
	}

	public Function<Integer, int[][]> lambdaNewMatrix() {
		return int[][]::new;
	}
}
