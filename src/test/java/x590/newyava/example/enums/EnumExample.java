package x590.newyava.example.enums;

import x590.newyava.example.Main;

@SuppressWarnings("all")
public enum EnumExample {
	A,
	B(1),
	C(2);

	EnumExample() {}

	EnumExample(int x) {}

	public static void main(String[] args) {
		Main.run(EnumExample.class);
	}

	@Override
	public String toString() {
		return switch (this) {
			case A -> "a";
			case B -> "b";
			case C -> "c";
		};
	}
}
