package x590.newyava.example;

import org.junit.Test;

@SuppressWarnings("all")
public class ClassConstExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void foo() {
		System.out.println(Object.class);
		System.out.println(ClassConstExample.class);
		System.out.println(void.class);
		System.out.println(byte.class);
		System.out.println(short.class);
		System.out.println(char.class);
		System.out.println(int.class);
		System.out.println(long.class);
		System.out.println(float.class);
		System.out.println(double.class);
		System.out.println(boolean.class);
		System.out.println(Object[].class);
		System.out.println(byte[].class);
		System.out.println(short[].class);
		System.out.println(char[].class);
		System.out.println(int[].class);
		System.out.println(long[].class);
		System.out.println(float[].class);
		System.out.println(double[].class);
		System.out.println(boolean[].class);
	}
}
