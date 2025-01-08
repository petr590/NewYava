package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class ArraysExample {

	private static final Object notArrField = new Object[] { "", 1, true };
	private static final Object[] arrField = { "", 1, true };

	@Test
	public void run() {
		Main.run(this);
	}

	public void foo(int a, int b) {
		var arr1 = new int[a];
		var arr2 = new int[a][b];
		var arr3 = new int[a][b][1];

		arr1[0] = 10;
		arr2[0] = arr1;
		arr3[1] = arr2;
	}

	public void initializing() {
		int[] arr1 = { 0, 0, 1 };
		int[][] arr2 = { { 0, 1, 0 } };
		int[][][] arr3 = { { { 0, 1, 1 } } };
	}

	public void booleanArr() {
		var arr = new boolean[] { true, false, Boolean.TRUE, Boolean.FALSE };
	}

	public void byteArr() {
		var arr = new byte[] { 1, 2, 3, 4, 5 };
	}

	public void shortArr() {
		var arr = new short[] { 1, 2, 3, 4, 5 };
	}

	public void charArr() {
		var arr = new char[] { 'a', 'b', 'c', '\0', '\'', '"' };
	}

	public void stringArr() {
		var arr = new String[] { "intVar", "bar", "baz", null, (String)new Object() };
		new String[] { "intVar" }[0] = null;
	}

	public int[] clone(int[] arr) {
		return arr.clone();
	}

	public int[][] clone(int[][] arr) {
		return arr.clone();
	}
}
