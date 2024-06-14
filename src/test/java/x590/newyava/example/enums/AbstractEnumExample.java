package x590.newyava.example.enums;

import x590.newyava.example.Main;

@SuppressWarnings("unused")
public enum AbstractEnumExample {
	AX(true) {
		public int choose(int x, int y, int z) { return x; }
	},

	AY {
		public int choose(int x, int y, int z) { return y; }
	},

	AZ(true) {
		public int choose(int x, int y, int z) { return z; }
	};

	private final boolean b;

	AbstractEnumExample() {
		this(false);
	}

	AbstractEnumExample(boolean b) {
		this.b = b;
	}

	public abstract int choose(int x, int y, int z);

	public static void main(String[] args) {
		Main.runForCaller();
//		Main.run(AbstractEnum.class);
	}
}
