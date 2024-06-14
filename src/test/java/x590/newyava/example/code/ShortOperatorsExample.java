package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class ShortOperatorsExample {
	@Test
	public void run() {
		Main.run(this);
	}

	private void f(int x) {}
	private void f(float x) {}
	private void f(double x) {}

	private int i;
	private float f;
	private double d;

	public void intFields() {
		f(++i);
		f(--i);
		f(i++);
		f(i--);
	}

	public void floatFields() {
		f(++f);
		f(--f);
		f(f++);
		f(f--);
	}

	public void doubleFields() {
		f(++d);
		f(--d);
		f(d++);
		f(d--);
	}

	public void intVar(int x) {
		f(++x);
		f(--x);
		f(x++);
		f(x--);
	}

	public void floatVar(float x) {
		f(++x);
		f(--x);
		f(x++);
		f(x--);
	}

	public void doubleVar(double x) {
		f(++x);
		f(--x);
		f(x++);
		f(x--);
	}

	public int bitNot() {
		return i = ~i;
	}

	public int xorM1() {
		return i ^= -1;
	}

	public int xor1() {
		return i ^= 1;
	}

	public int bitNot(int i) {
		return i = ~i;
	}

	public int xorM1(int i) {
		return i ^= -1;
	}

	public int xor1(int i) {
		return i ^= 1;
	}


	public void testArr(int[] arr) {
		f(++arr[0]);
		f(--arr[0]);
		f(arr[0]++);
		f(arr[0]--);
		f(arr[0] *= 2);
		f(arr[0] /= 2);
	}

	public void testArr(double[] arr) {
		f(++arr[0]);
		f(--arr[0]);
		f(arr[0]++);
		f(arr[0]--);
		f(arr[0] *= 2);
		f(arr[0] /= 2);
	}

	public void test2DArr(int[][] arr) {
		f(++arr[0][0]);
		f(--arr[0][0]);
		f(arr[0][0]++);
		f(arr[0][0]--);
		f(arr[0][0] *= 2);
		f(arr[0][0] /= 2);
	}

	public void test2DArr(double[][] arr) {
		f(++arr[0][0]);
		f(--arr[0][0]);
		f(arr[0][0]++);
		f(arr[0][0]--);
		f(arr[0][0] *= 2);
		f(arr[0][0] /= 2);
	}
}
