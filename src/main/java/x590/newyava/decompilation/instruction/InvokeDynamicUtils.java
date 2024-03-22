package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;

import java.lang.invoke.*;
import java.lang.runtime.ObjectMethods;

public class InvokeDynamicUtils {
	private InvokeDynamicUtils() {}

	static final String STRING_CONCAT_METHOD = "makeConcatWithConstants";

	static final Handle STRING_CONCAT_BOOTSTRAP = new Handle(
			Opcodes.H_INVOKESTATIC,
			classDescriptor(StringConcatFactory.class),
			STRING_CONCAT_METHOD,
			methodDescriptor(CallSite.class,
					MethodHandles.Lookup.class, String.class,
					MethodType.class, String.class, Object[].class
			),
			false
	);

	static final Handle RECORD_BOOTSTRAP = new Handle(
			Opcodes.H_INVOKESTATIC,
			classDescriptor(ObjectMethods.class),
			"bootstrap",
			methodDescriptor(Object.class,
					MethodHandles.Lookup.class, String.class, TypeDescriptor.class,
					Class.class, String.class, MethodHandle[].class
			),
			false
	);


	static final Handle LAMBDA_BOOTSTRAP = new Handle(
			Opcodes.H_INVOKESTATIC,
			classDescriptor(LambdaMetafactory.class),
			"metafactory",
			methodDescriptor(CallSite.class,
					MethodHandles.Lookup.class, String.class, MethodType.class,
					MethodType.class, MethodHandle.class, MethodType.class
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
