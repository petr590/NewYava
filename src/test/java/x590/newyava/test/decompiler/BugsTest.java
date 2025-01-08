package x590.newyava.test.decompiler;

import org.junit.Test;
import x590.newyava.example.Main;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;

public class BugsTest {
	@Test
	public void run() {
		Main.run(this);
	}

//	@Bug(State.FIXED)
//	public boolean bug1(boolean b1, boolean b2) {
//		return b1 ^ b2;
//	}
//
//
//	@Bug(State.FIXED)
//	public void bug2(boolean x) {
//		if (x) {
//			float f = 1;
//			System.out.println(f);
//		}
//
//		Object o = new Object();
//		System.out.println(o);
//	}
//
//	@Bug(State.PARTIALLY_FIXED)
//	public Runnable bug3() {
//		return () -> {
//			try (var out = new FileOutputStream("file")) {
//				out.write(0);
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			} finally {
//				System.out.println("finally");
//			}
//		};
//	}
//
//
//	@Bug(State.FIXED)
//	private static BooleanSupplier createRenamer(Path path1, Path path2) {
//		return new BooleanSupplier() {
//
//			public boolean getAsBoolean() {
//				try {
//					Files.move(path1, path2);
//					return true;
//				} catch (IOException ex) {
//					System.err.printf("Failed to rename: %s", ex);
//					return false;
//				}
//			}
//
//			public String toString() {
//				return "rename " + path1 + " to " + path2;
//			}
//		};
//	}

	public volatile boolean a, b;

	@Bug
	public void bug3(boolean x) {
		if (x) {
			while (a) {
				System.out.println("a");
			}
		} else {
			while (b) {
				System.out.println("b");
			}
		}
	}
}
