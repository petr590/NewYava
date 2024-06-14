package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.constant.JavaEscapeUtils;

public class EscapingTest {
	@Test
	public void testChar() {
		Assert.assertEquals("a", JavaEscapeUtils.escapeChar('a'));
		Assert.assertEquals("\\t", JavaEscapeUtils.escapeChar('\t'));
		Assert.assertEquals("\\\\", JavaEscapeUtils.escapeChar('\\'));
		Assert.assertEquals("\\'", JavaEscapeUtils.escapeChar('\''));
		Assert.assertEquals("\"", JavaEscapeUtils.escapeChar('"'));
		Assert.assertEquals("\\0", JavaEscapeUtils.escapeChar('\0'));
		Assert.assertEquals("\\377", JavaEscapeUtils.escapeChar('\377'));
		Assert.assertEquals("\\5", JavaEscapeUtils.escapeChar('\005'));
		Assert.assertEquals("\\35", JavaEscapeUtils.escapeChar('\035'));
		Assert.assertEquals("\\235", JavaEscapeUtils.escapeChar('\235'));
	}

	@Test
	public void testString() {
		Assert.assertEquals("a", JavaEscapeUtils.escapeString("a"));
		Assert.assertEquals("\\t", JavaEscapeUtils.escapeString("\t"));
		Assert.assertEquals("\\\\", JavaEscapeUtils.escapeString("\\"));
		Assert.assertEquals("'", JavaEscapeUtils.escapeString("'"));
		Assert.assertEquals("\\\"", JavaEscapeUtils.escapeString("\""));
		Assert.assertEquals("\\0", JavaEscapeUtils.escapeString("\0"));
		Assert.assertEquals("\\377", JavaEscapeUtils.escapeString("\377"));
		Assert.assertEquals("\\2", JavaEscapeUtils.escapeString("\2"));
		Assert.assertEquals("\\u00027", JavaEscapeUtils.escapeString("\u00027"));
		Assert.assertEquals("\\uFFFF", JavaEscapeUtils.escapeString("\uFFFF"));
		Assert.assertEquals("\\305", JavaEscapeUtils.escapeString("\305"));
	}
}
