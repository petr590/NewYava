package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class CastExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void foo(byte x, short y, char z) {}

	// Обязательно указывать (byte) и (short)
	public void bar() {
		foo((byte)1, (short)2, '\3');
	}

	public double calc(double x, float y, int a) {
		return x * y + a + 1;
	}

	public int calc(int x, short y, byte a) {
		return x * y + a + 1;
	}

	public double calc(float x, float y, float a) {
		return (double)x * y + a + 1;
	}

	// y неявно кастуется к int
	public int shift(int x, long y) {
		return x << y;
	}

	// y неявно кастуется к int
	public int shift(int x, Long y) {
		return x << y;
	}
}
