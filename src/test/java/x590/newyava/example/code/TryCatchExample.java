package x590.newyava.example.code;

import org.junit.Test;
import x590.newyava.example.Main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

@SuppressWarnings("all")
public class TryCatchExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void tryCatch() {
		try {
			InputStream in = new FileInputStream("/tmp/x");
			in.read();
		} catch (UncheckedIOException | IOException ex) {
			ex.printStackTrace();
		} catch (Throwable ex) {
			System.err.println(ex);
			throw ex;
		}
	}

	public void tryCatchWithIf(boolean x) {
		try {
			if (x) {
				InputStream in = new FileInputStream("/tmp/x");
				in.read();
			}

			return;
		} catch (Throwable ex) {
			ex.printStackTrace();
		}

		System.out.println("gg");
	}

//	public void tryCatchFinally() {
//		try {
//			InputStream in = new FileInputStream("/tmp/x");
//		} catch (IOException ex) {
////			throw new UncheckedIOException(ex);
//			ex.printStackTrace();
//		} finally {
//			System.out.println("FINALLY!");
//		}
//	}
}
