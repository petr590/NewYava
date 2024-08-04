package x590.newyava.example.code;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class TernaryOperatorExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public String ternary(boolean a) {
		return a ? "1" : "2";
	}

	public String nestedTernary1(boolean a, boolean b) {
		return a ? b ? "1" : "2" : "3";
	}

	public String nestedTernary2(boolean a, boolean b) {
		return a ? "1" : b ? "2" : "3";
	}

	public String nestedTernary3(boolean a, boolean b, boolean c) {
		return a ?
				b ? "1" : "2" :
				c ? "3" : "4";
	}

	public boolean complexCondition(boolean a, boolean b, boolean c) {
		return a && b || c;
	}

	public boolean isNull(@Nullable Object obj) {
		return obj == null;
	}

	private void bar(int x) {}

	public void bug1(boolean x) {
		bar(x ? 5 : 10);
	}

	public boolean andCondition(double x, double y, @Nullable Object o) {
		return x > y ? isNull(o) : false;
	}

	public boolean orCondition(double x, double y, @Nullable Object o) {
		return x > y ? true : isNull(o);
	}
}
