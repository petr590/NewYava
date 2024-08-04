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
 * Переменная. Содержит тип и имя. Изменяемый класс.
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

	/** Добавляет потенциальное имя переменной.
	 * Если имя неизменяемо или {@code name == null}, то ничего не делает. */
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


	public static String getMatchingEnding(String str1, String str2) {
		int p1 = str1.length() - 1,
			p2 = str2.length() - 1;

		int capacity = Math.min(p1, p2) + 1;
		if (capacity == 0)
			return "";

		var builder = new StringBuilder(capacity);

		for (; p1 >= 0 && p2 >= 0; p1--, p2--) {
			char c1 = str1.charAt(p1),
				 c2 = str2.charAt(p2);

			if (c1 == Character.toUpperCase(c2)) {
				builder.append(c1);

			} else if (Character.toUpperCase(c1) == c2) {
				builder.append(c2);

			} else {
				break;
			}
		}

		return Utils.toLowerCamelCase(builder.reverse().toString());
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
