package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class BitExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public short bitNot(short x) {
		return (short)~x;
	}

	public int bitNot(int x) {
		return ~x;
	}

	public long bitNot(long x) {
		return ~x;
	}

	public int doubleNeg(int x) {
		return ~-~-x;
	}

	public long doubleNeg(long x) {
		return ~-~-x;
	}
}
