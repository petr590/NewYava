package x590.newyava.example;

import org.junit.Test;
import x590.newyava.Main;

@SuppressWarnings("all")
public class NewExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void newString() {
		var str1 = new String();
		var str2 = new String(new char[] { 'a' }, 0, 1);
		var str3 = new String(new byte[] { 'a' });
	}

	public void newObject() {
		var obj = new Object();
	}
}
