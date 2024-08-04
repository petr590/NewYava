package x590.newyava.decompilation.operation.monitor;

import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.other.SpecialOperation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.Types;

public sealed class MonitorOperation implements SpecialOperation
	permits MonitorEnterOperation, MonitorExitOperation {

	protected final Operation object;

	public MonitorOperation(MethodContext context) {
		this.object = context.popAs(Types.ANY_OBJECT_TYPE);
	}

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record("monitor(").record(object, context, Priority.ZERO).record(")");
	}
}
