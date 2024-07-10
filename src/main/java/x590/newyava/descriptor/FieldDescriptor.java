package x590.newyava.descriptor;

import x590.newyava.io.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

public record FieldDescriptor(ClassType hostClass, String name, Type type)
		implements ContextualWritable, Importable {

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
		out.recordSp(type, context).record(name);
	}


	public boolean equals(ReferenceType hostClass, String name, Type type) {
		return  this.hostClass.equals(hostClass) &&
				this.name.equals(name) &&
				this.type.equals(type);
	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) return true;
//		if (!(obj instanceof FieldDescriptor other)) return false;
//		if (!hostClass.equals(other.hostClass)) return false;
//		if (!name.equals(other.name)) return false;
//		if (!type.equals(other.type)) return false;
//		return true;
//	}


	@Override
	public String toString() {
		return String.format("%s %s.%s", type, hostClass, name);
	}
}
