package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

@SuppressWarnings("all")
public class Arrays {

	@Test
	public void run() {
		Main.run(this);
	}

	public void foo(int a, int b) {
		var arr1 = new int[a];
		var arr2 = new int[a][b];
		var arr3 = new int[a][b][8];

		arr1[0] = 10;
		arr2[0] = arr1;
		arr3[0] = arr2;
	}
}
