package x590.newyava.example.enums;

import x590.newyava.Main;

@SuppressWarnings("unused")
public enum AbstractEnum {
	AX {
		public int choose(int x, int y, int z) { return x; }
	},

	AY {
		public int choose(int x, int y, int z) { return y; }
	},

	AZ {
		public int choose(int x, int y, int z) { return z; }
	};

	public abstract int choose(int x, int y, int z);

	public static void main(String[] args) {
		Main.runForCaller();
//		Main.run(AbstractEnum.class);
	}
}
