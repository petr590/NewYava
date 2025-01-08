package x590.newyava.decompilation.operation.emptyscope;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.type.PrimitiveType;

public class EmptySwitchScopeOperation extends EmptyScopeOperation {

	public EmptySwitchScopeOperation(Operation value) {
		super("switch", value, PrimitiveType.INT);
	}

	@Override
	public void beforeVariablesInit(Context context, @Nullable MethodScope methodScope) {
		super.beforeVariablesInit(context, methodScope);
		assert value != null;

		var valueAndEnumMap = OperationUtils.getEnumMap(context, value);

		if (valueAndEnumMap != null) {
			value = valueAndEnumMap.first();
			requiredType = valueAndEnumMap.second().values().iterator().next().type();
		}
	}
}
