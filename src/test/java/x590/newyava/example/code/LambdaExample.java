package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.construction.InterfaceExample;
import x590.newyava.example.Main;
import x590.newyava.test.decompiler.Bug;
import x590.newyava.test.decompiler.State;

import java.util.function.*;

@SuppressWarnings("all")
public class LambdaExample implements InterfaceExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().ignoreVariableTable(false).build());
	}

	@Bug(State.FIXED)
	private void bug(boolean flag) {
		boolean[] aboolean = {false};

		double d0 = 0,
				d1 = 1;

		int i = 4;

		if (flag) {
			wrapScreenError(() -> {
				aboolean[0] = mouseClicked(d0, d1, i);
			}, "mouseClicked event handler", aboolean.getClass());
		} else {
			wrapScreenError(() -> {
				aboolean[0] = mouseReleased(d0, d1, i);
			}, "mouseReleased event handler", aboolean.getClass());
		}
	}

	private static void wrapScreenError(Runnable runnable, String s, Class<?> c) {}

	private boolean mouseClicked(double a, double b, int c) {
		return a == b;
	}

	private boolean mouseReleased(double a, double b, int c) {
		return a == b;
	}


	@Override
	public int interfaceMethod(int x) {
		return 0;
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
		return InterfaceExample.super::defaultMethod;
	}

	// Не должна заменяться ссылкой на метод
	public IntSupplier superInterfaceLambda3() {
		return () -> InterfaceExample.super.defaultMethod(2);
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

	private static int get(IntSupplier supplier) {
		return supplier.getAsInt();
	}

	private static int get(IntUnaryOperator operator) {
		return operator.applyAsInt(1);
	}

	private int x = 10;

	public int complexLambda() {
		long outer = 1;

		return get(f -> {
			Object o = null;
			System.out.println(o);
			System.out.println(outer);

			if (o != null) {
				int v = x;
				return v;
			}

			return 20;
		});
	}

	public int inTheScope(int x) {
		if (x > 0) {
			return get(() -> {
				int y = x / 2;
				return y;
			});
		}

		return -1;
	}
}
