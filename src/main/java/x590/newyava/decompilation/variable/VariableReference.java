package x590.newyava.decompilation.variable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.Type;

/**
 * Ссылка на переменную. Несколько ссылок могут указывать на одну и ту же переменную.
 * Используется, когда сами переменные ещё не восстановлены.
 * Изменяемый класс.
 */
@Getter
@RequiredArgsConstructor
public class VariableReference {

	/** Изначальный тип переменной. Может отличаться от итогового типа. */
	private final Type initialType;

	private final @Nullable String initialName;

	private final int start, end;

	private Variable variable;

	private @Nullable VariableReference binded;

	public VariableReference(Type initialType, int start, int end) {
		this(initialType, null, start, end);
	}


	public void bind(VariableReference ref) {
		if (binded != null && binded != ref) {
			throw new DecompilationException("VariableReference " + this + " already binded to " + binded);
		}

		this.binded = ref;
	}

	public Variable getVariable() {
		return binded != null ? binded.getVariable() : variable;
	}

	/** Преобразует тип переменной к требуемому с расширением вниз.
	 * Доступно только после связывания ссылки переменной. */
	public void assignUp(Type requiredType) {
		getVariable().assignUp(requiredType);
	}

	/** Преобразует тип переменной к требуемому с расширением вверх.
	 * Доступно только после связывания ссылки переменной. */
	public void assignDown(Type requiredType) {
		getVariable().assignDown(requiredType);
	}

	/** Объявляет переменную. Доступно только после связывания ссылки с самой переменной.
	 * @return {@code true}, если переменная была не объявлена */
	public boolean attemptDeclare() {
		return getVariable().attemptDeclare();
	}

	/** @return тип переменной. До связывания ссылки с переменной возвращает {@link #initialType}. */
	public Type getType() {
		var variable = getVariable();
		return variable == null ? initialType : variable.getType();
	}

	/** @return имя переменной. Доступно только после связывания ссылки с самой переменной. */
	public String getName() {
		return getVariable().getName();
	}

	/**
	 * Связывает ссылку с переменной.
	 * @throws IllegalStateException если ссылка уже связана с другой переменной.
	 */
	public void initVariable(Variable variable) {
		if (binded != null) {
			binded.initVariable(variable);
			return;
		}

		if (this.variable != null && this.variable != variable) {
			throw new IllegalStateException(
					"Reinitialized variable is not matches: " + this.variable + ", " + variable
			);
		}

		this.variable = variable;
	}

	@Override
	public String toString() {
		return String.format("VariableReference(%d - %d, %s %s)", start, end, initialType, initialName);
	}
}
