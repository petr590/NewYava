package x590.newyava.decompilation.operation.invoke;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;

public class InvokeSpecialOperation extends InvokeNonstaticOperation {

	private enum MethodType {
		PLAIN, CONSTRUCTOR, THIS, SUPER, SUPER_INTERFACE
	}

	private final MethodType methodType;

	public InvokeSpecialOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);

		if (descriptor.isConstructor()) {
			if (object.isThisRef()) {
				var hostClass = descriptor.hostClass();

				this.methodType =
						hostClass.equals(context.getThisType()) ? MethodType.THIS :
						hostClass.equals(context.getSuperType()) ? MethodType.SUPER : MethodType.SUPER_INTERFACE;

			} else {
				this.methodType = MethodType.CONSTRUCTOR;
			}

		} else {
			this.methodType = MethodType.PLAIN;
		}
	}

	@Override
	public boolean isDefaultConstructor() {
		return (methodType == MethodType.SUPER || methodType == MethodType.THIS) && arguments.isEmpty();
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		switch (methodType) {
			case PLAIN -> super.write(out, context);
			case CONSTRUCTOR -> out.record(object, context, getPriority());
			case THIS -> out.record("this");
			case SUPER -> out.record("super");
			case SUPER_INTERFACE -> out.record(descriptor.hostClass(), context).record(".super");
		}

		if (methodType != MethodType.PLAIN) {
			out.record('(').record(arguments, context, Priority.ZERO, ", ").record(')');
		}
	}
}
