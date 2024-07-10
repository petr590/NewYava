package x590.newyava.decompilation.operation.invoke;

import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.CastOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;

import java.util.List;
import java.util.Set;

/**
 * Объединяет операции invokevirtual и invokeinterface
 */
public class InvokeVIOperation extends InvokeNonstaticOperation {
	private static final List<MethodDescriptor> wrapperDescriptors = List.of(
			new MethodDescriptor(ClassType.INTEGER,   "intValue",     PrimitiveType.INT),
			new MethodDescriptor(ClassType.LONG,      "longValue",    PrimitiveType.LONG),
			new MethodDescriptor(ClassType.FLOAT,     "floatValue",   PrimitiveType.FLOAT),
			new MethodDescriptor(ClassType.DOUBLE,    "doubleValue",  PrimitiveType.DOUBLE),
			new MethodDescriptor(ClassType.BOOLEAN,   "booleanValue", PrimitiveType.BOOLEAN),
			new MethodDescriptor(ClassType.BYTE,      "byteValue",    PrimitiveType.BYTE),
			new MethodDescriptor(ClassType.SHORT,     "shortValue",   PrimitiveType.SHORT),
			new MethodDescriptor(ClassType.CHARACTER, "charValue",    PrimitiveType.CHAR)
	);

	public static Operation invokeVirtual(MethodContext context, MethodDescriptor descriptor) {
		for (MethodDescriptor wrapperDescriptor : wrapperDescriptors) {
			if (descriptor.equals(wrapperDescriptor)) {
				return CastOperation.wide(context, descriptor.hostClass(), descriptor.returnType());
			}
		}

		return new InvokeVIOperation(context, descriptor);
	}

	public static Operation invokeInterface(MethodContext context, MethodDescriptor descriptor) {
		return new InvokeVIOperation(context, descriptor);
	}


	private static final Set<MethodDescriptor> firstIntAsCharDescriptors = Set.of(
			new MethodDescriptor(ClassType.STRING, "indexOf", PrimitiveType.INT, List.of(PrimitiveType.INT)),
			new MethodDescriptor(ClassType.STRING, "indexOf", PrimitiveType.INT, List.of(PrimitiveType.INT, PrimitiveType.INT)),
			new MethodDescriptor(ClassType.STRING, "lastIndexOf", PrimitiveType.INT, List.of(PrimitiveType.INT)),
			new MethodDescriptor(ClassType.STRING, "lastIndexOf", PrimitiveType.INT, List.of(PrimitiveType.INT, PrimitiveType.INT))
	);


	private final boolean intAsChar;

	private InvokeVIOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
		this.intAsChar = firstIntAsCharDescriptors.contains(descriptor);
	}

	@Override
	protected void writeNameAndArgs(DecompilationWriter out, MethodWriteContext context) {
		if (intAsChar) {
			out.record(descriptor.name()).record('(');

			var arguments = this.arguments;
			int size = arguments.size();

			if (size > 0) {
				arguments.get(0).writeIntAsChar(out, context);

				if (size > 1) {
					out.record(", ").record(arguments.subList(1, size), context, Priority.ZERO, ", ");
				}
			}

			out.record(')');
		} else {
			super.writeNameAndArgs(out, context);
		}
	}
}
