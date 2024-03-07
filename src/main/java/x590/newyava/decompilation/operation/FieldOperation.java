package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.ClassConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public class FieldOperation implements Operation {
	private final FieldDescriptor descriptor;

	private final @Nullable Operation instance, value;

	public static Operation getStatic(FieldDescriptor descriptor) {
		if (descriptor.name().equals("TYPE")) {
			var hostClass = descriptor.hostClass();
			
			if (hostClass.equals(ClassType.BYTE))      return new LdcOperation(ClassConstant.BYTE);
			if (hostClass.equals(ClassType.SHORT))     return new LdcOperation(ClassConstant.SHORT);
			if (hostClass.equals(ClassType.CHARACTER)) return new LdcOperation(ClassConstant.CHAR);
			if (hostClass.equals(ClassType.INTEGER))   return new LdcOperation(ClassConstant.INT);
			if (hostClass.equals(ClassType.LONG))      return new LdcOperation(ClassConstant.LONG);
			if (hostClass.equals(ClassType.FLOAT))     return new LdcOperation(ClassConstant.FLOAT);
			if (hostClass.equals(ClassType.DOUBLE))    return new LdcOperation(ClassConstant.DOUBLE);
			if (hostClass.equals(ClassType.BOOLEAN))   return new LdcOperation(ClassConstant.BOOLEAN);
			if (hostClass.equals(ClassType.VOID))      return new LdcOperation(ClassConstant.VOID);
		}

		return new FieldOperation(descriptor, null, null);
	}

	public static Operation putStatic(MethodContext context, FieldDescriptor descriptor, Operation value) {
		if (context.getDescriptor().isStaticInitializer() &&
			descriptor.hostClass().equals(context.getDescriptor().hostClass())) {

			var foundField = context.getClassContext().findField(descriptor);

			if (foundField.isPresent()) {
				if (foundField.get().setInitializer(value)) {
					return null;
				}

				System.out.println(foundField.get() + ", " + foundField.get().keep());

				if (!foundField.get().keep()) {
					return null;
				}
			}
		}

		return new FieldOperation(descriptor, null, value);
	}

	@Override
	public Type getReturnType() {
		return value == null ? descriptor.type() : PrimitiveType.VOID;
	}

	@Override
	public Priority getPriority() {
		return value == null ? Priority.DEFAULT : Priority.ASSIGNMENT;
	}

	@Override
	public void addImports(ClassContext context) {
		if (instance == null) {
			context.addImport(descriptor.hostClass());
		}

		context.addImportsFor(instance).addImportsFor(value);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		if (instance != null) {
			out.record(instance, context, getPriority());
		} else {
			out.record(descriptor.hostClass(), context);
		}

		out.record('.').record(descriptor.name());

		if (value != null) {
			out.record(" = ").record(value, context, Priority.ASSIGNMENT);
		}
	}
}
