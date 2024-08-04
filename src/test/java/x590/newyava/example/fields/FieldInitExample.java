package x590.newyava.example.fields;

import x590.newyava.example.Main;

@SuppressWarnings("unused")
public class FieldInitExample {
	private static int x;

	static {
		System.out.println(x);
		x = 0;
		System.out.println(x);
		x = 1;
		f();
	}

	private static void f() {
		System.out.println(gg);
	}

	private static final String gg = "gg" + Math.random() * Double.MAX_VALUE;

	static {
		System.out.println(gg);
	}

	private final int y = 5;
	private final int z;

	public FieldInitExample() {
		this.z = 2;
	}

	public FieldInitExample(int z) {
		this.z = z;
	}

	public static void main(String[] args) {
		Main.runForCaller();
	}
}
