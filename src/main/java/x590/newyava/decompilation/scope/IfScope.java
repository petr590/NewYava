package x590.newyava.decompilation.scope;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.condition.Condition;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.List;

public final class IfScope extends IfElseScope {

	@Getter
	private final Condition condition;

	public IfScope(Condition condition, @Unmodifiable List<Chunk> chunks) {
		super(chunks, -1);
		this.condition = condition;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);
		condition.inferType(PrimitiveType.BOOLEAN);
	}

	@Override
	public void addImports(ClassContext context) {
		super.addImports(context);
		context.addImportsFor(condition);
	}

	@Override
	protected @Nullable Operation getHeaderOperation() {
		return condition;
	}

	@Override
	protected boolean writeHeader(DecompilationWriter out, MethodWriteContext context) {
		out.record("if (").record(condition, context, Priority.ZERO).record(')');
		return true;
	}
}
