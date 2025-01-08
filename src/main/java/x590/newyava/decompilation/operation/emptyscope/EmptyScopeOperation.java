package x590.newyava.decompilation.operation.emptyscope;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.Objects;

/** Представляет собой пустой scope */
public non-sealed class EmptyScopeOperation implements EmptyableScopeOperation {
	private final String literal;

	protected @Nullable Operation value;
	protected @Nullable Type requiredType;

	public EmptyScopeOperation(String literal) {
		this.literal = literal;
		this.value = null;
		this.requiredType = null;
	}

	public EmptyScopeOperation(String literal, Operation value, Type requiredType) {
		this.literal = literal;
		this.value = Objects.requireNonNull(value);
		this.requiredType = Objects.requireNonNull(requiredType);
	}


	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void inferType(Type ignored) {
		if (value != null) {
			assert requiredType != null;
			value.inferType(requiredType);
		}
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(literal).space();

		if (value != null) {
			out.record('(').record(value, context, Priority.ZERO).record(')').space();
		}

		out.record("{}");
	}
}
