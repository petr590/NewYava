package x590.newyava.descriptor;

import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.IClassArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.IClassType;
import x590.newyava.type.Type;

public record FieldDescriptor(IClassType hostClass, String name, Type type)
		implements ContextualWritable, Importable {

	public ClassType baseHostClass() {
		return hostClass.base();
	}

	public static FieldDescriptor of(String className, String name, String typeName) {
		return new FieldDescriptor(ClassType.valueOf(className), name, Type.valueOf(typeName));
	}

	public static FieldDescriptor of(ClassType hostClass, String name, String typeName) {
		return new FieldDescriptor(hostClass, name, Type.valueOf(typeName));
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(type);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(type, context).space().record(name);
	}


	public boolean equals(IClassArrayType hostClass, String name, Type type) {
		return  this.hostClass.equals(hostClass) &&
				this.name.equals(name) &&
				this.type.equals(type);
	}

	public boolean baseEquals(FieldDescriptor other) {
		return  this.hostClass.equals(other.hostClass) &&
				this.name.equals(other.name) &&
				this.type.baseEquals(other.type);
	}


	@Override
	public String toString() {
		return String.format("%s %s.%s", type, hostClass, name);
	}
}
