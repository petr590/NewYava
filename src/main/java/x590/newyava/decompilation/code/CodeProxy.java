package x590.newyava.decompilation.code;

import lombok.*;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.VariableTableView;
import x590.newyava.io.DecompilationWriter;

@Data
@AllArgsConstructor
public class CodeProxy extends Code {
	private Code code;

	@Override
	public void addImports(ClassContext context) {
		code.addImports(context);
	}

	@Override
	public boolean isValid() {
		return code.isValid();
	}

	@Override
	public boolean caughtException() {
		return code.caughtException();
	}

	@Override
	public MethodScope getMethodScope() {
		return code.getMethodScope();
	}

	@Override
	public VariableTableView getVarTable() {
		return code.getVarTable();
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		code.write(out, context);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Code other && code.equals(other);
	}
}
