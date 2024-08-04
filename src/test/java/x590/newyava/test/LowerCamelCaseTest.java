package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.util.Utils;

public class LowerCamelCaseTest {
	@Test
	public void test() {
		Assert.assertEquals("integer", Utils.toLowerCamelCase("Integer"));
		Assert.assertEquals("html", Utils.toLowerCamelCase("HTML"));
		Assert.assertEquals("htmlDocument", Utils.toLowerCamelCase("HTMLDocument"));
		Assert.assertEquals("htmlDocument", Utils.toLowerCamelCase("HtmlDocument"));
	}
}
