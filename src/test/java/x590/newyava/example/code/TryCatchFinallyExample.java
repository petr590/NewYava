package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

import java.io.*;

@SuppressWarnings("all")
public class TryCatchFinallyExample {
	@Test
	public void run() {
		Main.run(this);
	}

//	public void tryCatch() {
//		try {
//			InputStream in = new FileInputStream("/tmp/x");
//			in.read();
//		} catch (UncheckedIOException | IOException ex) {
//			ex.printStackTrace();
//		} catch (Throwable ex) {
//			System.err.println(ex);
//			throw ex;
//		}
//	}
//
//	public void tryCatchWithIf(boolean x) {
//		try {
//			if (x) {
//				InputStream in = new FileInputStream("/tmp/x");
//				in.read();
//			}
//
//			return;
//		} catch (Throwable ex) {
//			ex.printStackTrace();
//		}
//
//		System.out.println("gg");
//	}
//
//	public void tryCatchFinally(boolean x) {
//		try {
//			if (x) {
//				return;
//			}
//
//			InputStream in = new FileInputStream("/tmp/x");
//
//		} catch (IOException ex) {
//			throw new UncheckedIOException(ex);
////			ex.printStackTrace();
//		} finally {
//			System.out.println("FINALLY!");
//		}
//	}

	public void tryCatchInLoop(boolean x) {
		for (;;) {
			try {
				InputStream in = new FileInputStream("/tmp/x");
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		}
	}

	public void loopInTryCatch(boolean x) {
		try {
			for (;;) {
				InputStream in = new FileInputStream("/tmp/x");
			}
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

//	public void tryFinally() {
//		int a;
//		try {
//			a = 0;
//		} finally {
//			try {
//				a = 0;
//			} finally {
//				a = 0;
//			}
//		}
//	}

//	public void tryWithResources(boolean x) throws IOException {
//		try (var out = new FileOutputStream("/tmp/abc")) {
//			if (x) {
////				return;
//			}
//		}
//	}
}
