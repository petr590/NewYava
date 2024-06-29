package x590.newyava.decompilation;

import x590.newyava.io.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.VariableTableView;

public interface ReadonlyCode extends ContextualWritable, Importable {
	MethodScope getMethodScope();

	VariableTableView getVarTable();
}
