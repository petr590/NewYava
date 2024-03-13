package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.decompilation.instruction.InvokeDynamicUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.TypeDescriptor;
import java.lang.runtime.ObjectMethods;

public class InvokeDynamicUtilsTest {
	@Test
	public void test() {
		Assert.assertEquals(
				"(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/TypeDescriptor;Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/invoke/MethodHandle;)Ljava/lang/Object;",
				InvokeDynamicUtils.methodDescriptor(Object.class, MethodHandles.Lookup.class, String.class, TypeDescriptor.class, Class.class, String.class, MethodHandle[].class)
		);

		Assert.assertEquals(
				"(IJFD)[I",
				InvokeDynamicUtils.methodDescriptor(int[].class, int.class, long.class, float.class, double.class)
		);

		Assert.assertEquals(
				"java/lang/runtime/ObjectMethods",
				InvokeDynamicUtils.classDescriptor(ObjectMethods.class)
		);
	}
}
