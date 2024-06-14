package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import x590.newyava.decompilation.operation.SpecialOperation;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

/**
 * Операция условного/безусловного перехода на другой чанк
 */
public abstract class JumpOperation implements SpecialOperation {

	/**
	 * @return условие перехода. Операции безусловного перехода
	 * должны возвращать {@link ConstCondition#TRUE}
	 */
	public abstract Condition getCondition();

	/**
	 * @return Лейбл, указывающий на позицию перехода
	 */
	public abstract Label getLabel();

	@Override
	public Type getReturnType() {
		return PrimitiveType.VOID;
	}


	@Getter
	private @NotNull Role role = Role.UNKNOWN;

	/**
	 * Инициализирует роль операции.
	 * @throws IllegalStateException если роль уже инициализирована другим значением
	 */
	public void initRole(Role role) {
		if (this.role != Role.UNKNOWN && this.role != role) {
			throw new IllegalStateException(
					"Reinitialized role is not matches: " + this.role + ", " + role
			);
		}

		this.role = role;
	}

	public boolean roleInitialized() {
		return role != Role.UNKNOWN;
	}

	public boolean canWrite() {
		return role.canWrite();
	}

	@Override
	public void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {
		role.resolveLabelNames(currentScope, generator);
	}
}
