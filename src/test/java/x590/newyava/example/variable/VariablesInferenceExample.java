package x590.newyava.example.variable;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class VariablesInferenceExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void testUpAssignment() {
		byte a = 1;
		short b = 1;
		char c = 1;
		int d = 1;
		boolean e = true;

		useByte(a);
		useShort(a);
		useInt(a);

		useShort(b);
		useInt(b);

		useChar(c);
		useInt(c);

		useInt(d);

		useBoolean(e);
	}

	public void testDownAssignment() {
		byte a = getByte();

		short b = getByte();
		b = getShort();

		char c = getChar();

		int d = getByte();
		d = getShort();
		d = getChar();
		d = getInt();

		boolean e = getBoolean();
	}

	private byte getByte() {
		return 1;
	}

	private short getShort() {
		return 1;
	}

	private char getChar() {
		return 1;
	}

	private int getInt() {
		return 1;
	}

	private boolean getBoolean() {
		return true;
	}

	private void useByte(byte x) {}

	private void useShort(short x) {}

	private void useChar(char x) {}

	private void useInt(int x) {}

	private void useBoolean(boolean x) {}
}
