package x590.newyava.test.exploring;

import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import x590.newyava.ReflectField;
import x590.newyava.ReflectMethod;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

public class GenericTypesTest {
	@Test
	public void test() {
		var method = new ReflectMethod(TestInterface.class.getDeclaredMethods()[0]);
		var field = new ReflectField(TestInterface.class.getDeclaredFields()[0]);

		System.out.println(method.getVisibleDescriptor());
		System.out.println(field.getVisibleDescriptor());
		var r = ((TypeVariable<?>) TestInterface.class.getDeclaredMethods()[0].getGenericReturnType()).getBounds()[0];
		System.out.println(r);
		System.out.println(r.getClass());
	}

	@SuppressWarnings("unused")
	private abstract static class TestInterface<T extends Comparable<T> & Serializable> {
		abstract <U extends Comparable<? extends U> & Serializable> U add(Map<? super Object, U> map, U[][] us);

		private List<?> @Nullable[] list;
	}
}
