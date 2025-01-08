package x590.newyava;

import lombok.Getter;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.IClassArrayType;
import x590.newyava.type.Type;

import java.lang.reflect.Method;
import java.util.Arrays;

@Getter
public class ReflectMethod implements IMethod {
	private final int modifiers;
	private final MethodDescriptor descriptor, visibleDescriptor;

	public ReflectMethod(Method method) {
		this.modifiers = method.getModifiers();

		this.descriptor = new MethodDescriptor(
				IClassArrayType.valueOf(method.getDeclaringClass()),
				method.getName(),
				Type.valueOf(method.getReturnType()),
				Arrays.stream(method.getParameterTypes()).map(Type::valueOf).toList()
		);

		this.visibleDescriptor = new MethodDescriptor(
				descriptor.hostClass(),
				method.getName(),
				Type.valueOf(method.getGenericReturnType()),
				Arrays.stream(method.getGenericParameterTypes()).map(Type::valueOf).toList()
		);
	}
}
