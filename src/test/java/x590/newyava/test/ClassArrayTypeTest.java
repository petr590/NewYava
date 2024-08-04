package x590.newyava.test;


import org.junit.Assert;
import org.junit.Test;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.ArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;

public class ClassArrayTypeTest {

	@Test
	public void testNames() {
		ClassType stringType = ClassType.valueOf(String.class);

		Assert.assertEquals("java.lang.String",   stringType.getCanonicalBinName());
		Assert.assertEquals("java/lang/String",   stringType.getClassBinName());
		Assert.assertEquals("Ljava/lang/String;", stringType.getBinName());
		Assert.assertEquals("java.lang.String",   stringType.getName());
		Assert.assertEquals("String",             stringType.getSimpleName());
		Assert.assertEquals("java.lang",          stringType.getPackageName());


		ArrayType stringArray = ArrayType.valueOf(String[].class);

		Assert.assertEquals("[Ljava.lang.String;", stringArray.getCanonicalBinName());
		Assert.assertEquals("[Ljava/lang/String;", stringArray.getClassBinName());
		Assert.assertEquals("[Ljava/lang/String;", stringArray.getBinName());


		ArrayType intArray = ArrayType.valueOf(int[].class);

		Assert.assertEquals("[I", intArray.getCanonicalBinName());
		Assert.assertEquals("[I", intArray.getClassBinName());
		Assert.assertEquals("[I", intArray.getBinName());
	}

	@Test
	public void testNestedNames() {
		Assert.assertThrows(DecompilationException.class,
				() -> ClassType.checkOrUpdateNested("java/lang/String", "java/lang/Object"));


		ClassType innerType = ClassType.valueOf("Outer$Middle$Inner");

		Assert.assertEquals("", innerType.getPackageName());
		Assert.assertEquals("Outer$Middle$Inner", innerType.getClassBinName());
		Assert.assertEquals("Outer$Middle$Inner", innerType.getName());
		Assert.assertEquals("Outer$Middle$Inner", innerType.getSimpleName());

		ClassType.checkOrUpdateNested("Outer$Middle$Inner", "Outer$Middle");

		Assert.assertEquals("Inner",              innerType.getSimpleName());
		Assert.assertEquals("Outer$Middle.Inner", innerType.getName());

		ClassType.checkOrUpdateNested("Outer$Middle", "Outer");

		Assert.assertEquals("Outer.Middle.Inner", innerType.getName());
	}
	
	@Test
	public void testClassFinding() throws ClassNotFoundException {
		Class.forName(ClassType.OBJECT.getCanonicalBinName());
		Class.forName(ArrayType.forType(ClassType.OBJECT).getCanonicalBinName());
		Class.forName(ArrayType.forType(PrimitiveType.INT).getCanonicalBinName());
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
