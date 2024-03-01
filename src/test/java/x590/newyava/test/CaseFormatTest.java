package x590.newyava.test;

import com.google.common.base.CaseFormat;
import org.junit.Test;

public class CaseFormatTest {

	@Test
	public void test() {
		System.out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, "SomeClassName"));
		System.out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, "Some_Class_Name"));
		System.out.println(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, "HTMLFormat"));
	}
}
