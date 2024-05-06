package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

@SuppressWarnings("all")
public class Arrays {

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
		arr3[0] = arr2;
	}

	public void initializing() {
		int[] arr1 = { 0, 0, 1 };
		int[][] arr2 = { { 0, 1, 0 } };
		int[][][] arr3 = { { { 0, 1, 1 } } };
	}

	public void booleanArr() {
		var arr = new boolean[] { true, false, Boolean.TRUE, Boolean.FALSE };
	}

	public void charArr() {
		var arr = new char[] { 'a', 'b', 'c', '\0', '\'', '"' };
	}

	public void stringArr() {
		var arr = new String[] { "foo", "bar", "baz", null, (String)new Object() };
		(new String[] { "foo" })[0] = null;
	}
}
