package x590.newyava.decompilation.operation.invoke;

import x590.newyava.Modifiers;
import x590.newyava.context.MethodContext;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.operation.NewOperation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public class InvokeSpecialOperation extends InvokeNonstaticOperation {

	private enum InvokeType {
		PLAIN, NEW, THIS, SUPER, SUPER_INTERFACE
	}

	private final InvokeType invokeType;

	private final Type returnType;

	public InvokeSpecialOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);

		this.invokeType = getInvokeType(context, descriptor);

		this.returnType = invokeType == InvokeType.NEW ?
				object.getReturnType() :
				descriptor.returnType();
	}

	private InvokeType getInvokeType(MethodContext context, MethodDescriptor descriptor) {
		if (descriptor.isConstructor()) {
			if (object.isThisRef()) {
				var hostClass = descriptor.hostClass();

				return  hostClass.equals(context.getThisType()) ? InvokeType.THIS :
						hostClass.equals(context.getSuperType()) ? InvokeType.SUPER : InvokeType.SUPER_INTERFACE;

			} else if (object instanceof NewOperation newOperation && context.popIfSame(newOperation)) {
				return InvokeType.NEW;
			}

		}

		return InvokeType.PLAIN;
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public boolean isDefaultConstructor(MethodContext context) {
		return (invokeType == InvokeType.SUPER || invokeType == InvokeType.THIS) &&
				(arguments.isEmpty() || isDefaultEnumConstructor(context));
	}

	private boolean isDefaultEnumConstructor(MethodContext context) {
		return (context.getClassModifiers() & Modifiers.ACC_ENUM) != 0 &&
				context.isConstructor() &&
				// Enum.<init>(String, int)
				descriptor.equals(ClassType.ENUM, MethodDescriptor.INIT, PrimitiveType.VOID,
						List.of(ClassType.STRING, PrimitiveType.INT));
	}

	public boolean isNew() {
		return invokeType == InvokeType.NEW;
	}

	@Override
	public void write(DecompilationWriter out, WriteContext context) {
		switch (invokeType) {
			case PLAIN -> super.write(out, context);
			case NEW -> out.record(object, context, Priority.ZERO);
			case THIS -> out.record("this");
			case SUPER -> out.record("super");
			case SUPER_INTERFACE -> out.record(descriptor.hostClass(), context).record(".super");
		}

		if (invokeType != InvokeType.PLAIN) {
			out.record('(').record(arguments, context, Priority.ZERO, ", ").record(')');
		}
	}
}
