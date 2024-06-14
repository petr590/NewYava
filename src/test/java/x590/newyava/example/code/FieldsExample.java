package x590.newyava.example.code;

import x590.newyava.example.Main;

@SuppressWarnings("all")
public class FieldsExample {
	public static void main(String[] args) {
		Main.run(FieldsExample.class);
	}

//	@Getter @Setter
	private int x;

//	@Getter @Setter
	private static int y;

	private static final boolean BOOL1 = true;
	private static final char CHAR1 = '\0';


	private static boolean BOOL2;
	private static char CHAR2;

	static {
		boolean b = true;
		char c = '\0';

		BOOL2 = b;
		CHAR2 = c;
	}
}
