package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class ImplicitCastExample {

	private static final long LONG_CONSTANT = 1;
	private static final float FLOAT_CONSTANT = 1;
	private static final double DOUBLE_CONSTANT = 1;

	private static final long[] LONG_ARRAY = { 1 };
	private static final float[] FLOAT_ARRAY = { 1 };
	private static final double[] DOUBLE_ARRAY = { 1 };

	private final long longConstant = 1;
	private final float floatConstant = 1;
	private final double doubleConstant = 1;

	@Test
	public void run() {
		Main.run(this);
	}

	void foo() {
		long l = 1;
		float f = 1;
		double d = 1;

		Boolean b = true;
		Byte b2 = 1;
		Short s2 = 1;
		Character c2 = 1;
		Integer i2 = 1;
		Long l2 = 1L;
		Float f2 = 1F;
		Double d2 = 1D;

		l *= 2;
		f *= 2;
		d *= 2;

		long[] la = { 1 };
		float[] fa = { 1 };
		double[] da = { 1 };
	}
}
