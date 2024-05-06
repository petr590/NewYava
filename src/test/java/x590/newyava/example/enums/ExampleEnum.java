package x590.newyava.example.enums;

import x590.newyava.Main;

@SuppressWarnings("all")
public enum ExampleEnum {
	A,
	B(1),
	C(2);

	ExampleEnum() {}

	ExampleEnum(int x) {}

	public static void main(String[] args) {
		Main.run(ExampleEnum.class);
	}
}
