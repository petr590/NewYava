package x590.newyava.decompilation.operation.variable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
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
import java.util.Objects;

/**
 * Представляет исключение, которое кладётся на стек в начале блока catch.
 */
@EqualsAndHashCode
@NoArgsConstructor
public class CatchOperation implements Operation {
	/** id чанка, на котором оканчивается catch. */
	@Getter
	@Setter
	private int endId = -1;

	@Getter
	private List<ClassType> exceptionTypes = new ArrayList<>();

	private @Nullable VariableReference varRef;


	/** Добавляет тип исключения в список. */
	public void add(ClassType exceptionType) {
		exceptionTypes.add(Objects.requireNonNull(exceptionType));
	}

	/** Завершает инициализацию, делая {@link #exceptionTypes} неизменяемым. */
	public void finalizeExceptionTypesInit() {
		exceptionTypes = Collections.unmodifiableList(exceptionTypes);
	}

	public boolean isFinally() {
		return exceptionTypes.isEmpty();
	}

	public VariableReference getVarRef() {
		return Objects.requireNonNull(varRef);
	}

	/** Инициализирует переменную, в которую сохраняется исключение.
	 * @throws IllegalStateException если переменная уже инициализирована */
	void initVarRef(VariableReference varRef) {
		if (this.varRef != null)
			throw new IllegalStateException("Variable reference already initialized");

		this.varRef = varRef;
	}

	@Override
	public VarUsage getVarUsage(int slotId) {
		return slotId == getVarRef().getSlotId() ? VarUsage.STORE : VarUsage.NONE;
	}

	@Override
	public void inferType(Type requiredType) {
		for (ClassType type : exceptionTypes) {
			getVarRef().assignUp(type);
		}
	}

	@Override
	public boolean declareVariables() {
		return getVarRef().attemptDeclare() | Operation.super.declareVariables();
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
		out.record(exceptionTypes, context, " | ").space().record(getVarRef().getName());
	}

	@Override
	public String toString() {
		return String.format("CatchOperation(endId = %d, exceptionTypes = %s)",
				endId, exceptionTypes);
	}
}
