package x590.newyava.constant;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.translate.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

@UtilityClass
public final class JavaEscapeUtils {
	public static final AggregateTranslator ESCAPE_JAVA_STRING = new AggregateTranslator(
			new LookupTranslator(Map.of("\"", "\\\"",  "\\", "\\\\")),
			new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
			new JavaOctalUnicodeEscaper()
	);

	public static final AggregateTranslator ESCAPE_JAVA_CHAR = new AggregateTranslator(
			new LookupTranslator(Map.of("'", "\\'",  "\\", "\\\\")),
			new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
			new JavaOctalUnicodeEscaper()
	);

	public static String escapeString(String str) {
		return ESCAPE_JAVA_STRING.translate(str);
	}

	public static String escapeChar(char ch) {
		return ESCAPE_JAVA_CHAR.translate(String.valueOf(ch));
	}

	public static String wrapChar(char ch) {
		return "'" + escapeChar(ch) + "'";
	}


	public static class JavaOctalUnicodeEscaper extends CharSequenceTranslator {

		@Override
		public int translate(CharSequence input, int index, Writer writer) throws IOException {
			int codePoint = Character.codePointAt(input, index);

			if (codePoint >= 32 && codePoint <= 127)
				return 0;

			if (codePoint <= '\377' &&
				(index + 1 >= input.length() || !Character.isDigit(input.charAt(index + 1)))) {

				writer.write('\\');

				if ((codePoint & 0x7 << 6) != 0)
					writer.write(((codePoint >> 6) & 0x7) + '0');

				if ((codePoint & (0x3F << 3)) != 0)
					writer.write(((codePoint >> 3) & 0x7) + '0');

				writer.write((codePoint & 0x7) + '0');
				return 1;

			} else if (codePoint > 0xFFFF) {
				char[] surrogatePair = Character.toChars(codePoint);
				write(surrogatePair[0], writer);
				write(surrogatePair[1], writer);

			} else {
				write(codePoint, writer);
			}

			return 1;
		}

		private static int hexDigit(int n) {
			return n >= 10 ? n + ('A' - 10) : n + '0';
		}

		private static void write(int codePoint, Writer writer) throws IOException {
			writer.write("\\u");
			writer.write(hexDigit((codePoint >> 12) & 0xF));
			writer.write(hexDigit((codePoint >>  8) & 0xF));
			writer.write(hexDigit((codePoint >>  4) & 0xF));
			writer.write(hexDigit((codePoint)       & 0xF));
		}
	}
}
