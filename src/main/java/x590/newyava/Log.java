package x590.newyava;

import lombok.experimental.UtilityClass;
import org.intellij.lang.annotations.PrintFormat;

import java.io.PrintStream;

@UtilityClass
public final class Log {
	private record Printer(PrintStream out, String level) {

		public void print(String message) {
			out.print(level);
			out.println(message);
		}

		public void print(@PrintFormat String message, Object[] args) {
			out.print(level);
			out.printf(message, args);
			out.println();
		}

		public void print(Object obj) {
			out.print(level);
			out.println(obj);
		}

		public void print(Object first, Object[] objects) {
			out.print(level);
			out.print(first);

			for (Object object : objects) {
				out.print(", ");
				out.print(object);
			}

			out.println();
		}
	}

	private static final Printer
			DEBUG = new Printer(System.out, "DEBUG "),
			WARN  = new Printer(System.err, "WARN  "),
			ERROR = new Printer(System.err, "ERROR ");

	public static void debug(String message) {
		DEBUG.print(message);
	}

	public static void debug(@PrintFormat String message, Object... args) {
		DEBUG.print(message, args);
	}

	public static void debug(Object obj) {
		DEBUG.print(obj);
	}

	public static void debug(Object first, Object... objects) {
		DEBUG.print(first, objects);
	}


	public static void warn(String message) {
		WARN.print(message);
	}

	public static void warn(@PrintFormat String message, Object... args) {
		WARN.print(message, args);
	}

	public static void warn(Object obj) {
		WARN.print(obj);
	}

	public static void warn(Object first, Object... objects) {
		WARN.print(first, objects);
	}


	public static void error(String message) {
		ERROR.print(message);
	}

	public static void error(@PrintFormat String message, Object... args) {
		ERROR.print(message, args);
	}

	public static void error(Object obj) {
		ERROR.print(obj);
	}

	public static void error(Object first, Object... objects) {
		ERROR.print(first, objects);
	}
}
