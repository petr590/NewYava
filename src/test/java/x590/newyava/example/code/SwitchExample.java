package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class SwitchExample {
	@Test
	public void run() {
		Main.run(this);
	}

//	public void emptySwitch(int x) { // Компилятор выдал просто инструкцию pop вместо switch, класс!
//		switch (x) {}
//	}
//
//	public void switchInt(int x) {
//		switch (x) {
//			case 0 -> System.out.println("Zero");
//			case 1 -> System.out.println("One");
//			case 2 -> System.out.println("Two");
//			case 3 -> System.out.println("Three");
//			case 4 -> {
//				System.out.println("Four");
//				return;
//			}
//			case 5 -> {
//				return;
//			}
//			case 6 -> throw null;
//			default -> System.out.println(x);
//		}
//	}
//
//	public void switchWithIf(int x, int y) {
//		switch (x) {
//			case 0 -> {
//				if (y == 0) {
//					System.out.println("Z");
//				}
//			}
//		}
//	}
//
//	public void switchWithLoop1(int x, int y) {
//		switch (x) {
//			case 0 -> {
//				while (y == 0) {
//					System.out.println("Z");
//				}
//			}
//		}
//	}
//
//	public void switchWithLoop2(int x, int y) {
//		while (y == 0) {
//			switch (x) {
//				case 0 -> System.out.println("Z");
//			}
//		}
//	}
//
//	public void switchString(String str) {
//		switch (str) {
//			case "Zero" -> System.out.println(0);
//			case "One" -> System.out.println(1);
//			case "Two" -> System.out.println(2);
//			case "Three" -> System.out.println(3);
//			case "Four" -> {
//				System.out.println(4);
//				return;
//			}
//			case "Five" -> {
//				return;
//			}
//			case "Six" -> throw null;
//			default -> System.out.println(str);
//		}
//	}
//
//	public void switchString2(String v1) { // Emulation
//		String v2 = v1;
//		int v3 = -1;
//
//		switch (v2.hashCode()) {
//			case 2781896:
//				if (v2.equals("Zero")) {
//					v3 = 0;
//				}
//		}
//
//		switch (v3) {
//			case 0:
//				System.out.println(0);
//		}
//	}
//
//	public void switchEnum(RetentionPolicy policy, ElementType type) {
//		switch (policy) {
//			case RUNTIME -> System.out.println("runtime");
//			case CLASS -> {
//				System.out.println("class");
//				return;
//			}
//			case SOURCE -> {
//				return;
//			}
//			default -> throw null;
//		}
//
//		switch (type) {
//			case FIELD -> System.out.println("field");
//			case METHOD -> System.out.println("method");
//			default -> System.out.println("another");
//		}
//	}

	public String switchExpression(int x) {
		return switch (x) {
			case -1 -> throw new IllegalArgumentException();
			case 0 -> "Zero";
			case 1 -> "One";
			default -> {
				if (x > 0) {
					System.out.println("Another");
					yield "gg";
				}

				yield Math.random() > 0.5 ? "Other" : "Another";
			}
		};
	}
}
