package x590.newyava.decompilation;

import x590.newyava.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.decompilation.scope.MethodScope;

public interface ReadonlyCode extends ContextualWritable, Importable {

	MethodScope getMethodScope();
}
