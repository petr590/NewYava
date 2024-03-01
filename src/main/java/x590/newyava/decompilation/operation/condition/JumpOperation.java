package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Label;
import x590.newyava.decompilation.operation.SpecialOperation;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

/**
 * Операция условного/безусловного перехода на другой чанк
 */
public abstract class JumpOperation extends SpecialOperation {

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


	@RequiredArgsConstructor
	public enum Role {
		UNKNOWN (false),
		LOOP_CONDITION (false),
		IF_BRANCH (false),
		ELSE_BRANCH (false),
		BREAK (true),
		CONTINUE (true);

		private final boolean canWrite;
	}

	@Getter
	private @NotNull Role role = Role.UNKNOWN;

	/**
	 * Инициализирует роль операции.
	 * @throws IllegalStateException если роль уже инициализирована другим значением
	 */
	public void initRole(Role role) {
		if (this.role != Role.UNKNOWN && this.role != role)
			throw new IllegalStateException(
					"Reinitialized role is not matches: " + this.role + ", " + role
			);

		this.role = role;
	}

	public boolean roleInitialized() {
		return role != Role.UNKNOWN;
	}

	public boolean canWrite() {
		return role.canWrite;
	}
}
