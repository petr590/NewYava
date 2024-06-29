package x590.newyava.decompilation.operation.variable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Представляет исключение, которое кладётся на стек в начале блока catch.
 */
@NoArgsConstructor
public class CatchOperation implements Operation {
	/** id чанка, на котором оканчивается catch. */
	@Getter
	@Setter
	private int endId = -1;

	private List<ClassType> exceptionTypes = new ArrayList<>();

	/** Добавляет тип исключения в список. */
	public void add(ClassType exceptionType) {
		exceptionTypes.add(exceptionType);
	}

	/** Завершает инициализацию, делая {@link #exceptionTypes} неизменяемым. */
	public void finalizeExceptionTypesInit() {
		exceptionTypes = Collections.unmodifiableList(exceptionTypes);
	}

	private VariableReference varRef;

	/** Инициализирует переменную, в которую сохраняется исключение. */
	void initVarRef(VariableReference varRef) {
		if (this.varRef != null)
			throw new IllegalStateException("Variable reference already initialized");

		this.varRef = varRef;
	}

	@Override
	public VarUsage getVarUsage(int slotId) {
		return slotId == varRef.getSlotId() ? VarUsage.STORE : VarUsage.NONE;
	}

	@Override
	public void inferType(Type requiredType) {
		for (ClassType type : exceptionTypes) {
			varRef.assignUp(type);
		}
	}

	@Override
	public boolean declareVariables() {
		return varRef.attemptDeclare() | Operation.super.declareVariables();
	}

	@Override
	public Type getReturnType() {
		return ClassType.THROWABLE;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImports(exceptionTypes);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(exceptionTypes, context, " | ").space().record(varRef.getName());
	}
}
