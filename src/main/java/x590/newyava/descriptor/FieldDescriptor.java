package x590.newyava.descriptor;

import x590.newyava.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

public record FieldDescriptor(ClassType hostClass, String name, Type type)
		implements ContextualWritable, Importable {

	public static FieldDescriptor of(String className, String name, String typeName) {
		return new FieldDescriptor(ClassType.valueOf(className), name, Type.valueOf(typeName));
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(type);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.recordSp(type, context).record(name);
	}


	@Override
	public String toString() {
		return String.format("%s %s.%s", type, hostClass, name);
	}
}
