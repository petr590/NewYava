package x590.newyava.decompilation.operation.monitor;

import lombok.Getter;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.variable.StoreOperation;

public final class MonitorEnterOperation extends MonitorOperation {
	@Getter
	private final Operation value;

	public MonitorEnterOperation(MethodContext context) {
		super(context);
		this.value = object instanceof StoreOperation storeOp ? storeOp.requireValue() : object;
	}
}
