package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

@RequiredArgsConstructor
public class FieldOperation implements Operation {
	private final FieldDescriptor descriptor;

	private final @Nullable Operation instance, value;

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
