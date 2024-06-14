package x590.newyava.decompilation.operation.invoke;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.CastOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class InvokeStaticOperation extends InvokeOperation {

	private static final String NAME = "valueOf";

	private static final List<MethodDescriptor> wrapperDescriptors = List.of(
			new MethodDescriptor(ClassType.INTEGER,   NAME, ClassType.INTEGER,   List.of(PrimitiveType.INT)),
			new MethodDescriptor(ClassType.LONG,      NAME, ClassType.LONG,      List.of(PrimitiveType.LONG)),
			new MethodDescriptor(ClassType.FLOAT,     NAME, ClassType.FLOAT,     List.of(PrimitiveType.FLOAT)),
			new MethodDescriptor(ClassType.DOUBLE,    NAME, ClassType.DOUBLE,    List.of(PrimitiveType.DOUBLE)),
			new MethodDescriptor(ClassType.BOOLEAN,   NAME, ClassType.BOOLEAN,   List.of(PrimitiveType.BOOLEAN)),
			new MethodDescriptor(ClassType.BYTE,      NAME, ClassType.BYTE,      List.of(PrimitiveType.BYTE)),
			new MethodDescriptor(ClassType.SHORT,     NAME, ClassType.SHORT,     List.of(PrimitiveType.SHORT)),
			new MethodDescriptor(ClassType.CHARACTER, NAME, ClassType.CHARACTER, List.of(PrimitiveType.CHAR))
	);

	public static Operation valueOf(MethodContext context, MethodDescriptor descriptor) {
		if (descriptor.name().equals(NAME)) {
			for (MethodDescriptor wrapperDescriptor : wrapperDescriptors) {
				if (descriptor.equals(wrapperDescriptor)) {
					return CastOperation.wide(context, descriptor.arguments().get(0), descriptor.returnType());
				}
			}
		}

		return new InvokeStaticOperation(context, descriptor);
	}


	private InvokeStaticOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);
	}


	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(descriptor.hostClass());
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (!canOmitClass(context)) {
			out.record(descriptor.hostClass(), context).record('.');
		}

		writeNameAndArgs(out, context);
	}

	private boolean canOmitClass(MethodWriteContext context) {
		return context.getConfig().canOmitThisAndClass() &&
				descriptor.hostClass().equals(context.getThisType());
	}

	@Override
	public String toString() {
		return String.format("InvokeStaticOperation %08x(%s %s.%s(%s))",
				hashCode(), descriptor.returnType(), descriptor.hostClass(), descriptor.name(),
				arguments.stream().map(Objects::toString).collect(Collectors.joining(" ")));
	}
}
