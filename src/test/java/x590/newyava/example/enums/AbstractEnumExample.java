package x590.newyava.example.enums;

import org.jetbrains.annotations.Nullable;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public enum AbstractEnumExample {
	AX (true) {
		public int choose(int x, int y, int z) { return x; }
	},

	AY {
		public int choose(int x, int y, int z) { return y; }
	},

	AZ (null) {
		public int choose(int x, int y, int z) { return z; }
	};

	private final boolean b;

	AbstractEnumExample(@Nullable Void ignored) {
		this();
	}

	AbstractEnumExample() {
		this(false);
	}

	AbstractEnumExample(boolean b) {
		this.b = b;
	}

	public abstract int choose(int x, int y, int z);

	public static void main(String[] args) {
		Main.runForCaller();
	}
}
