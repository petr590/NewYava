package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class StringConcatenation {
	@Test
	public void run() /*throws ClassNotFoundException*/ {
		Main.run(this);
//		Main.run(Class.forName("java.lang.invoke.BoundMethodHandle"));
	}

	public String concat(String s, int x, long l, float f, double d, boolean b, Object o) {
		return s + ",\1\2 " + x + ", " + l + ", " + f + ", " + d + ", " + b + ", " + o + ";";
	}

	public String concat(int x, int y) {
		return "" + x + y;
	}

	public String add(int x, int y) {
		return "" + (x + y);
	}

	public String inst(Object obj) {
		return "" + (obj instanceof Integer);
	}
}
