package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import org.objectweb.asm.Label;
import x590.newyava.context.ClassContext;
import x590.newyava.decompilation.operation.other.SpecialOperation;
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
	private Role role = Role.UNKNOWN;

	/**
	 * Инициализирует роль операции.
	 * @throws IllegalStateException если роль уже инициализирована другим значением.
	 */
	public void initRole(Role role) {
		if (this.role != Role.UNKNOWN && this.role != role) {
			throw new IllegalStateException(String.format(
					"Reinitialized role for %s is not matches: %s, %s",
					this, this.role, role
			));
		}

		this.role = role;
	}

	/**
	 * Изменяет роль операции.
	 * @throws IllegalStateException если роль ещё не инициализирована.
	 */
	public void changeRole(Role role) {
		if (this.role == Role.UNKNOWN) {
			throw new IllegalStateException("Role yet not initialized");
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
	public void addImports(ClassContext context) {
		context.addImportsFor(role);
	}

	@Override
	public void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {
		role.resolveLabelNames(currentScope, generator);
	}
}
