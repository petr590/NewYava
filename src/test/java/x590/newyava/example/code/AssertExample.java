package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

import java.util.List;

@SuppressWarnings("all")
public class AssertExample {
	@Test
	public void run() {
		Main.run(this);
	}

	private boolean a, b, c, d, e, f, g, h;
	private List<String> list;

	private void foo() {
		assert a;
		assert b : true;
		assert c : 'A';
		assert d : 1;
		assert e : "gg";
		assert f : null;
		assert list.isEmpty() : list;
		assert a && b;
	}
}
