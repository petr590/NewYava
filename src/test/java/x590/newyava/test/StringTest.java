package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.util.Utils;

public class StringTest {
	@Test
	public void testLowerCamelCase() {
		Assert.assertEquals("integer", Utils.toLowerCamelCase("Integer"));
		Assert.assertEquals("html", Utils.toLowerCamelCase("HTML"));
		Assert.assertEquals("htmlDocument", Utils.toLowerCamelCase("HTMLDocument"));
		Assert.assertEquals("htmlDocument", Utils.toLowerCamelCase("HtmlDocument"));
	}

	@Test
	public void testMatchingEnding() {
		Assert.assertEquals("str", Variable.getMatchingEnding("objStr", "intStr"));
		Assert.assertEquals("str", Variable.getMatchingEnding("antStr", "intStr"));
		Assert.assertEquals("str", Variable.getMatchingEnding("objStr", "str"));
		Assert.assertEquals("str", Variable.getMatchingEnding("str", "str"));
		Assert.assertEquals("objStr", Variable.getMatchingEnding("objStr", "objstr"));
		Assert.assertEquals("",    Variable.getMatchingEnding("objstr", "abcstr"));
	}
}
