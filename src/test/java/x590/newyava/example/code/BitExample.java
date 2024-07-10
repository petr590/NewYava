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

	public short bitAnd(short x, short y) {
		return (short)(x & y);
	}

	public short bitOr(short x, short y) {
		return (short)(x | y);
	}

	public short bitXor(short x, short y) {
		return (short)(x ^ y);
	}


	public boolean bitAnd(boolean x, boolean y) {
		return x & y;
	}

	public boolean bitOr(boolean x, boolean y) {
		return x | y;
	}

	public boolean bitXor(boolean x, boolean y) {
		return x ^ y;
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
