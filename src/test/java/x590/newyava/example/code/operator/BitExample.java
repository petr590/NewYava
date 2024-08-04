package x590.newyava.example.code.operator;

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

	public boolean bitAnd(boolean x) {
		return x & true;
	}

	public boolean bitOr(boolean x) {
		return x | false;
	}

	public boolean bitXor(boolean x) {
		return x ^ true;
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

	public long shl(long x) {
		return x << 5;
	}

	public long shr(long x) {
		return x >> 5;
	}

	public long ushr(long x) {
		return x >>> 5;
	}

	public int and(int x) {
		return x & 0x7F00FF;
	}

	public int or(int x) {
		return x | 0x7F00FF;
	}

	public int xor(int x) {
		return x ^ 0x7F00FF;
	}
}
