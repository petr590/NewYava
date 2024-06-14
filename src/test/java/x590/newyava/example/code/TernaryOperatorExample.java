package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class TernaryOperatorExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public String b2s(boolean a) {
		return a ? "1" : "2";
	}

	public String b2s(boolean a, boolean b) {
		return a ? b ? "1" : "2" : "3";
	}

	public String b2s(boolean a, boolean b, boolean c) {
		return a ?
				b ? "1" : "2" :
				c ? "3" : "4";
	}

	public boolean isNull(Object obj) {
		return obj == null;
	}
}
