package x590.newyava.test;

import lombok.Getter;
import org.junit.Test;
import x590.newyava.Main;

public class LombokTest {

	@Getter(lazy = true)
	private final int x = calcX();

	private static int calcX() {
		return (int)(Math.random() * 10000);
	}

	@Test
	public void test() {
//		System.out.println(x);
//		System.out.println(getX());
//		System.out.println(x);
		Main.run(this);
	}
}
