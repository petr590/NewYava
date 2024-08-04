package x590.newyava.decompilation.operation.emptyscope;

import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.type.PrimitiveType;

public class EmptySwitchScopeOperation extends EmptyScopeOperation {

	public EmptySwitchScopeOperation(Operation value) {
		super("switch", value, PrimitiveType.INT);
	}

	@Override
	public void beforeVariablesInit(MethodScope methodScope) {
		super.beforeVariablesInit(methodScope);
		assert value != null;

		var valueAndEnumMap = OperationUtils.getEnumMap(methodScope.getMethodContext(), value);

		if (valueAndEnumMap != null) {
			value = valueAndEnumMap.first();
			requiredType = valueAndEnumMap.second().values().iterator().next().type();
		}
	}
}
