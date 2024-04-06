package x590.newyava.decompilation.variable;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.Type;

/**
 * Ссылка на переменную. Несколько ссылок могут указывать на одну и ту же переменную.
 * Используется, когда сами переменные ещё не восстановлены.
 * Изменяемый класс.
 */
@Getter
public class VariableReference {

	private Type type;

	private final @Nullable String initialName;

	private final int start, end;

	private Variable variable;

	private @Nullable VariableReference binded;

	public VariableReference(Type type, int start, int end) {
		this(type, null, start, end);
	}

	public VariableReference(Type type, @Nullable String initialName, int start, int end) {
		this.type = type;
		this.initialName = initialName;
		this.start = start;
		this.end = end;
	}

	/** Обновляет тип переменной */
	public void updateType(Type newType) {
		type = newType;
	}

	/** Преобразует тип переменной к требуемому с проверкой совместимости */
	public void assignType(Type requiredType) {
		updateType(Type.assign(type, requiredType));
	}


	public void bind(VariableReference ref) {
		if (binded != null && binded != ref) {
			throw new DecompilationException("VariableReference " + this + " already binded to " + binded);
		}

		this.binded = ref;
	}

	public Variable getVariable() {
//		if (binded != null)
//			System.out.println(binded + ", " + this);

		return binded != null ? binded.getVariable() : variable;
	}

	/** @return Имя переменной. Доступно только после связывания ссылки с самой переменной */
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
		return String.format("VariableReference(%d - %d, %s %s)", start, end, type, initialName);
	}
}
