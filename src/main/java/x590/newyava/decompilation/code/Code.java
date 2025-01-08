package x590.newyava.decompilation.code;

import x590.newyava.Importable;
import x590.newyava.context.Context;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.VariableTableView;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;

/** Код метода */
public abstract class Code implements ContextualWritable, Importable {
	/** @return {@code true}, если код находится в валидном состоянии и может быть декомпилирован и записан. */
	public abstract boolean isValid();

	/** @return {@code true}, если произошло исключение при декомпиляции. */
	public abstract boolean caughtException();

	/** @return {@link MethodScope} данного кода.
	 * @throws UnsupportedOperationException если {@link #isValid()} возвращает {@code false}. */
	public abstract MethodScope getMethodScope();

	/** @return таблицу переменных данного кода.
	 * @throws UnsupportedOperationException если {@link #isValid()} возвращает {@code false}. */
	public abstract VariableTableView getVarTable();

	/** Записывает код, если {@link #isValid()} возвращает {@code true},
	 * иначе записывает исключение, если оно есть. */
	@Override
	public abstract void write(DecompilationWriter out, Context context);

	/** Если {@code other} является экземпляром {@link CodeProxy}, то сравнивает его реализацию с {@code this}. */
	@Override
	public boolean equals(Object other) {
		return this == other || other instanceof CodeProxy proxy && equals(proxy.getCode());
	}
}
