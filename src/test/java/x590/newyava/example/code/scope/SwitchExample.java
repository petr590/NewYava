package x590.newyava.example.code.scope;

import org.junit.Test;
import x590.newyava.example.Main;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("all")
public class SwitchExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void emptySwitch(int x) { // Компилятор выдал просто инструкцию pop вместо switch, класс!
		switch (x) {}
	}

	public void switchInt(int x) {
		switch (x) {
			case 0 -> System.out.println("Zero");
			case 1 -> System.out.println("One");
			case 2 -> System.out.println("Two");
			case 3 -> System.out.println("Three");
			case 4 -> {
				System.out.println("Four");
				return;
			}
			case 5 -> {
				return;
			}
			case 6 -> throw null;
			default -> System.out.println(x);
		}

		System.out.println("after switch");
	}

	public void switchWithIf(int x, int y) {
		switch (x) {
			case 0 -> {
				if (y == 0) {
					System.out.println("Z");
				}
			}
		}
	}

	public void switchWithLoop1(int x, int y) {
		switch (x) {
			case 0 -> {
				while (y == 0) {
					System.out.println("Z");
				}
			}
		}
	}

	public void switchWithLoop2(int x, int y) {
		while (y == 0) {
			switch (x) {
				case 0 -> System.out.println("Z");
			}
		}
	}

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

	public void switchEnum(RetentionPolicy policy, ElementType type) {
		switch (type) {
			case FIELD -> System.out.println("field");
			case METHOD -> System.out.println("method");
			default -> System.out.println("another");
		}

		switch (policy) {
			case RUNTIME -> System.out.println("runtime");
			case CLASS -> {
				System.out.println("class");
				return;
			}
			case SOURCE -> {
				return;
			}
			default -> throw null;
		}
	}

	private enum E {
		A, B, C;

		@Override
		public String toString() {
			return switch (this) {
				case A -> "a";
				case B -> "b";
				case C -> "c";
			};
		}
	}

//	public String switchExpression(int x) {
//		return switch (x) {
//			case -1 -> throw new IllegalArgumentException();
//			case 0 -> "Zero";
//			case 1 -> "One";
//			default -> {
//				if (x > 0) {
//					System.out.println("Another");
//					yield "gg";
//				}
//
//				yield Math.random() > 0.5 ? "Other" : "Another";
//			}
//		};
//	}
//
//	public boolean switchWithIf(E x, boolean y, boolean z) {
//		if (y) {
//			switch (x) {
//				case A:
//					if (z) {
//						System.out.println("y");
//						return false;
//					}
//				default:
//					return true;
//			}
//		}
//
//		return false;
//	}
//
//	public boolean bug(int x, int y) {
//		switch (x) {
//			case 0:
//				if (!foo()) {
//					return false;
//				}
//
//				System.out.println("gg");
//
//			default:
//				return true;
//
//			case 9:
//				bar();
//				return true;
//		}
//	}
//
//	private boolean foo() {
//		return false;
//	}
//
//	private void bar() {}
}
