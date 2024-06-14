package x590.newyava.test;

import org.junit.Assert;
import org.junit.Test;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.IntMultiType;
import x590.newyava.type.PrimitiveType;

import java.util.function.Supplier;

public class EqualsTest {
	@Test
	public void testDescriptors() {
		checkEquals(() -> new MethodDescriptor(ClassType.OBJECT, "method", ArrayType.forType(PrimitiveType.INT, 2)));
	}

	@Test
	public void testType() {
		checkEquals(() -> ArrayType.forType(PrimitiveType.INT, 2));
		checkEquals(() -> ClassType.valueOf("java/util/List"));
		checkEquals(() -> IntMultiType.valueOf(IntMultiType.BYTE_FLAG | IntMultiType.SHORT_FLAG));
	}

	private static <T> void checkEquals(Supplier<T> creator) {
		Assert.assertEquals(creator.get(), creator.get());
	}
}
