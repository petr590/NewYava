package x590.newyava.example.feature;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class ConstantsExample {
	private final int INT_CONSTANT = 12345;
	private final long LONG_CONSTANT = 112233445566778899L;
	private final float FLOAT_CONSTANT = 3.14f;
	private final double DOUBLE_CONSTANT = 3.333;
	private final double DOUBLE_CONSTANT_2 = Double.NaN;
	private final double DOUBLE_CONSTANT_3 = Double.NaN;
	private final String STRING_CONSTANT = "S1";
	private final String STRING_CONSTANT_2 = "S2";
	private final String STRING_CONSTANT_3 = STRING_CONSTANT_2;

	@Test
	public void run() {
		Main.run(this);
	}

	public void intConstants() {
		System.out.println(Integer.MIN_VALUE);
		System.out.println(Integer.MAX_VALUE);
		System.out.println(INT_CONSTANT);
	}

	public void longConstants() {
		System.out.println(Long.MIN_VALUE);
		System.out.println(Long.MAX_VALUE);
		System.out.println(LONG_CONSTANT);
	}

	public void floatConstants() {
		System.out.println(Float.MIN_VALUE);
		System.out.println(Float.MAX_VALUE);
		System.out.println(Float.MIN_NORMAL);
		System.out.println(Float.NaN);
		System.out.println(Float.POSITIVE_INFINITY);
		System.out.println(Float.NEGATIVE_INFINITY);
		System.out.println(FLOAT_CONSTANT);
	}

	public void doubleConstants() {
		System.out.println(Double.MIN_VALUE);
		System.out.println(Double.MAX_VALUE);
		System.out.println(Double.MIN_NORMAL);
		System.out.println(Double.NaN);
		System.out.println(Double.POSITIVE_INFINITY);
		System.out.println(Double.NEGATIVE_INFINITY);
		System.out.println(Math.PI);
		System.out.println(Math.E);
		System.out.println(Math.TAU);
		System.out.println(DOUBLE_CONSTANT);
	}

	public void stringConstants() {
		System.out.println(STRING_CONSTANT);
		System.out.println(STRING_CONSTANT_2);
	}
}
