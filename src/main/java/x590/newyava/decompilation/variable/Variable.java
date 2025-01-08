package x590.newyava.decompilation.variable;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Переменная. Содержит слот, тип и имя. Изменяемый класс.
 */
@Getter
public final class Variable {
	private final int slotId;

	private Type type;

	/** Потенциальные имена переменной. Если поле равно {@code null}, значит имя переменной неизменяемо */
	@Getter(AccessLevel.NONE)
	private final @Nullable Set<String> possibleNames;

	/** Итоговое имя переменной */
	private @Nullable String name;

	private boolean declared;

	/**
	 * Если у ссылки на переменную есть название, то оно станет окончательным для этой переменной
	 */
	public Variable(VariableReference ref, boolean declared) {
		this.slotId = ref.getSlotId();
		this.type = ref.getType();
		this.name = ref.getInitialName();
		this.declared = declared;
		this.possibleNames = name == null ? new HashSet<>() : null;
	}

	public void assignUp(Type requiredType) {
		type = Type.assignUp(type, requiredType);
	}

	public void assignDown(Type requiredType) {
		type = Type.assignDown(type, requiredType);
	}

	/** @return {@code true}, если имя переменной неизменяемо */
	public boolean isNameFixed() {
		return possibleNames == null;
	}

	/** @throws IllegalStateException если имя переменной неизменяемо */
	public void setName(String name) {
		if (isNameFixed())
			throw new IllegalStateException("Name of " + this + " is fixed");

		this.name = name;
	}

	/** Добавляет потенциальное имя переменной. Если имя переменной неизменяемо
	 * или переданное значение равно {@code null}, то ничего не делает. */
	public void addPossibleName(@Nullable String name) {
		if (possibleNames != null && name != null)
			possibleNames.add(name);
	}

	/**
	 * @return базовое имя переменной (может повторяться у нескольких переменных).
	 */
	public String getBaseName() {
		if (possibleNames != null && !possibleNames.isEmpty()) {
			return possibleNames.stream()
					.reduce(Variable::getMatchingEnding)
					.filter(Predicate.not(String::isEmpty))
					.orElse(type.getVarName());
		}

		return type.getVarName();
	}


	/** @return совпадающие концы строк */
	public static String getMatchingEnding(String str1, String str2) {
		int i1 = str1.length() - 1,
			i2 = str2.length() - 1;

		int capacity = Math.min(i1, i2) + 1;
		if (capacity == 0)
			return "";

		var name = new StringBuilder(capacity);
		var part = new StringBuilder();

		for (; i1 >= 0 && i2 >= 0; i1--, i2--) {
			char c1 = str1.charAt(i1),
				 c2 = str2.charAt(i2);

			if (Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
				break;
			}

			if (Character.isUpperCase(c1)) {
				name.append(part).append(c1);
				part.setLength(0);

			} else if (Character.isUpperCase(c2)) {
				name.append(part).append(c2);
				part.setLength(0);

			} else {
				part.append(c1);
			}
		}

		if (i1 == -1 || i2 == -1) {
			name.append(part);
		}

		return Utils.toLowerCamelCase(name.reverse().toString());
	}


	public boolean attemptDeclare() {
		if (!declared) {
			return declared = true;
		}

		return false;
	}

	@Override
	public String toString() {
		return String.format("Variable(%s %s)", type, name);
	}
}
