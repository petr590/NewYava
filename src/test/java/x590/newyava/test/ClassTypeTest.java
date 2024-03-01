package x590.newyava.test;


import org.junit.Assert;
import org.junit.Test;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.ClassType;

import java.util.List;

public class ClassTypeTest {

	@Test
	public void test1() {
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
	public void test2() {
		//x590/newyava/Main
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle", "x590/newyava/Main");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner", "x590/newyava/Main$Middle");
		ClassType.checkOrUpdateNested("x590/newyava/Main$1InMethod", null);
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner$1", null);
		ClassType.checkOrUpdateNested("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles");

		//x590/newyava/Main$1InMethod
		ClassType.checkOrUpdateNested("x590/newyava/Main$1InMethod", null);

		//x590/newyava/Main$Middle
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle", "x590/newyava/Main");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner", "x590/newyava/Main$Middle");

			//x590/newyava/Main$Middle$Inner
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle", "x590/newyava/Main");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner", "x590/newyava/Main$Middle");
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle$Inner$1", null);

		//x590/newyava/Main$Middle$Inner$1
		ClassType.checkOrUpdateNested("x590/newyava/Main$Middle", "x590/newyava/Main");
	}

	@Test
	public void test3() {
		List<String> strings = List.of("a", "b", "c", "", "d");

		System.out.println(strings.stream().filter(str -> !str.isEmpty()).peek(System.out::println).count());
	}
}
