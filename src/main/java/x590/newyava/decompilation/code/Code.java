package x590.newyava.decompilation.code;

import x590.newyava.Importable;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.VariableTableView;
import x590.newyava.io.ContextualWritable;

/** Код метода */
public interface Code extends ContextualWritable, Importable {
	/** @return {@code true}, если код находится в валидном состоянии и может быть декомпилирован и записан. */
	boolean isValid();

	/** @return {@code true}, если произошло исключение при декомпиляции. */
	boolean caughtException();

	/** @return {@link MethodScope} данного кода.
	 * @throws UnsupportedOperationException если {@link #isValid()} возвращает {@code false}. */
	MethodScope getMethodScope();

	/** @return таблицу переменных данного кода.
	 * @throws UnsupportedOperationException если {@link #isValid()} возвращает {@code false}. */
	VariableTableView getVarTable();
}
