package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;

import java.util.List;

public class MethodDescriptorTest {
	@Test
	public void test() {
		var refT = ClassType.STRING;
		var longT = PrimitiveType.LONG;

		var method1 = new MethodDescriptor(refT, "method", refT, List.of(refT));

		Assert.assertEquals(0, method1.indexBySlot(0));
		Assert.assertThrows(IllegalArgumentException.class, () -> method1.indexBySlot(1));
		Assert.assertThrows(IllegalArgumentException.class, () -> method1.indexBySlot(-1));

		var method2 = new MethodDescriptor(refT, "method", refT, List.of(refT, longT, refT));

		Assert.assertEquals(0, method2.indexBySlot(0));
		Assert.assertEquals(1, method2.indexBySlot(1));
		Assert.assertEquals(2, method2.indexBySlot(3));
		Assert.assertThrows(IllegalArgumentException.class, () -> method2.indexBySlot(2));
		Assert.assertThrows(IllegalArgumentException.class, () -> method2.indexBySlot(4));
	}
}
