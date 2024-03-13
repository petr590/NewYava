package x590.newyava.decompilation.instruction;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.invokedynamic.RecordInvokedynamicOperation;
import x590.newyava.decompilation.operation.invokedynamic.StringConcatOperation;
import x590.newyava.descriptor.IncompleteMethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record InvokeDynamicInsn(String name, String descriptor, Handle bootstrapHandle, Object[] bootstrapArgs)
	implements Instruction {


	@Override
	public int getOpcode() {
		return Opcodes.INVOKEDYNAMIC;
	}

	@Override
	public Operation toOperation(MethodContext context) {
		if (bootstrapHandle.equals(InvokeDynamicUtils.STRING_CONCAT_BOOTSTRAP)) {
			var methodDescriptor = IncompleteMethodDescriptor.of(name, descriptor);

			if (name.equals("makeConcatWithConstants") &&
				methodDescriptor.returnType().equals(ClassType.STRING)) {

				if (bootstrapArgs.length >= 1 &&
					Arrays.stream(bootstrapArgs).allMatch(arg -> arg instanceof String)) {

					List<String> bootstrapArgsList = Arrays.stream(bootstrapArgs).map(arg -> (String)arg).collect(Collectors.toList());
					return new StringConcatOperation(context, bootstrapArgsList, methodDescriptor.arguments());
				}

				throw new DecompilationException(
						"Wrong bootstrapArgs for \"makeConcatWithConstants\" method: " +
								Arrays.toString(bootstrapArgs)
				);
			}

		} else if (bootstrapHandle.equals(InvokeDynamicUtils.RECORD_BOOTSTRAP)) {

			var thisType = context.getThisType();
			var methodDescriptor = IncompleteMethodDescriptor.of(name, descriptor);

			if (methodDescriptor.equals("hashCode", PrimitiveType.INT, List.of(thisType))) {
				context.popAs(thisType);
				return RecordInvokedynamicOperation.HASH_CODE;
			}

			if (methodDescriptor.equals("equals", PrimitiveType.BOOLEAN, List.of(thisType, ClassType.OBJECT))) {
				context.popAs(ClassType.OBJECT);
				context.popAs(thisType);
				return RecordInvokedynamicOperation.EQUALS;
			}

			if (methodDescriptor.equals("toString", ClassType.STRING, List.of(thisType))) {
				context.popAs(thisType);
				return RecordInvokedynamicOperation.TO_STRING;
			}
		}

		throw new DecompilationException(String.format(
				"Cannot resolve invokedynamic: name = \"%s\", descriptor = \"%s\", " +
						"bootstrapHandle = \"%s\", bootstrapArgs = \"%s\"",
				name, descriptor, bootstrapHandle, Arrays.toString(bootstrapArgs)
		));
	}
}
