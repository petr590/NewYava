package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.*;
import java.lang.runtime.ObjectMethods;

public class InvokeDynamicUtils {
	private InvokeDynamicUtils() {}

	public static final Handle RECORD_BOOTSTRAP = new Handle(
			Opcodes.H_INVOKESTATIC,
			InvokeDynamicUtils.classDescriptor(ObjectMethods.class),
			"bootstrap",
			InvokeDynamicUtils.methodDescriptor(Object.class,
					MethodHandles.Lookup.class, String.class, TypeDescriptor.class,
					Class.class, String.class, MethodHandle[].class
			),
			false
	);

	public static final Handle STRING_CONCAT_BOOTSTRAP = new Handle(
			Opcodes.H_INVOKESTATIC,
			InvokeDynamicUtils.classDescriptor(StringConcatFactory.class),
			"makeConcatWithConstants",
			InvokeDynamicUtils.methodDescriptor(CallSite.class,
					MethodHandles.Lookup.class, String.class,
					MethodType.class, String.class, Object[].class
			),
			false
	);


	/**
	 * @return Строку, представляющую дескриптор метода
	 */
	public static String methodDescriptor(Class<?> returnType, Class<?>... argTypes) {
		var str = new StringBuilder("(");

		for (Class<?> argType : argTypes) {
			str.append(argType.descriptorString());
		}

		return str.append(')').append(returnType.descriptorString()).toString();
	}

	public static String classDescriptor(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			throw new IllegalArgumentException("Class must not be primitive");
		}

		return clazz.getName().replace('.', '/');
	}
}
