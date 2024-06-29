package x590.newyava.decompilation.operation.variable;

import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

public class DeclareOperation implements Operation {
	private final Variable variable;

	public DeclareOperation(Variable variable) {
		variable.attemptDeclare();
		this.variable = variable;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(variable.getType());
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.recordSp(variable.getType(), context).record(variable.getName());
	}
}
