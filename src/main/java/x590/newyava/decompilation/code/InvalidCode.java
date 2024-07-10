package x590.newyava.decompilation.code;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.VariableTableView;
import x590.newyava.io.DecompilationWriter;

@RequiredArgsConstructor
public final class InvalidCode implements Code {
	public static final InvalidCode EMPTY = new InvalidCode(null);

	/** Сообщение об исключении */
	private final @Nullable String exceptionMessage;

	@Override
	public void addImports(ClassContext context) {}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public boolean caughtException() {
		return exceptionMessage != null;
	}

	@Override
	public MethodScope getMethodScope() {
		throw new UnsupportedOperationException();
	}

	@Override
	public VariableTableView getVarTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (exceptionMessage != null) {
			out.record("/* ").record(exceptionMessage).record(" */");
		}
	}
}
