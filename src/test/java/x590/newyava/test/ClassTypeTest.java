package x590.newyava.test;


import org.junit.Assert;
import org.junit.Test;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.ClassType;

import java.util.List;

public class ClassTypeTest {

	@Test
	public void testNames() {
		ClassType stringType = ClassType.valueOf("java/lang/String");

		Assert.assertEquals(stringType.getBinName(), "java/lang/String");
		Assert.assertEquals(stringType.getName(), "java.lang.String");
		Assert.assertEquals(stringType.getSimpleName(), "String");
		Assert.assertEquals(stringType.getPackageName(), "java.lang");

		Assert.assertThrows(DecompilationException.class,
				() -> ClassType.checkOrUpdateNested("java/lang/String", "java/lang/Object"));


		ClassType innerType = ClassType.valueOf("Outer$Middle$Inner");

		Assert.assertEquals(innerType.getPackageName(), "");
		Assert.assertEquals(innerType.getBinName(),    "Outer$Middle$Inner");
		Assert.assertEquals(innerType.getName(),       "Outer$Middle$Inner");
		Assert.assertEquals(innerType.getSimpleName(), "Outer$Middle$Inner");

		ClassType.checkOrUpdateNested("Outer$Middle$Inner", "Outer$Middle");

		Assert.assertEquals(innerType.getSimpleName(), "Inner");
		Assert.assertEquals(innerType.getName(), "Outer$Middle.Inner");

		ClassType.checkOrUpdateNested("Outer$Middle", "Outer");

		Assert.assertEquals(innerType.getName(), "Outer.Middle.Inner");
	}

	@Test
	public void testNested() {
		//x590/newyava/Main
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle",              "x590/newyava/Main");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner",        "x590/newyava/Main$Middle");
		ClassType.checkOrUpdateNested("x590/newyava/Main$1InMethod",           "x590/newyava/Main", true);
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner$1",      "x590/newyava/Main$Middle$Inner", true);
		ClassType.checkOrUpdateNested("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles");

		//x590/newyava/Main$1InMethod
		ClassType.checkOrUpdateNested("x590/newyava/Main$1InMethod", "x590/newyava/Main", true);

		//x590/newyava/Main$Middle
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle",       "x590/newyava/Main");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner", "x590/newyava/Main$Middle");

			//x590/newyava/Main$Middle$Inner
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle",         "x590/newyava/Main");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner",   "x590/newyava/Main$Middle");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner$1", "x590/newyava/Main$Middle$Inner", true);

		//x590/newyava/Main$Middle$Inner$1
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle", "x590/newyava/Main");
	}
}
