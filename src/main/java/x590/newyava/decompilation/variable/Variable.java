package x590.newyava.decompilation.variable;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.type.Type;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Переменная. Содержит тип и имя. Изменяемый класс.
 */
@Getter
public final class Variable {
	private @NotNull Type type;

	/** Потенциальные имена переменной. Если поле равно {@code null}, значит имя переменной неизменяемо */
	@Getter(AccessLevel.NONE)
	private final @Nullable Set<String> names;

	private final @UnmodifiableView Set<String> namesView;

	/** Итоговое имя переменной */
	private @Nullable String name;

	private boolean defined;

	/**
	 * Если у ссылки на переменную есть название, то оно станет окончательным для этой переменной
	 */
	public Variable(VariableReference ref) {
		this.type = ref.getType();
		this.name = ref.getInitialName();

		if (name == null) {
			names = new HashSet<>();
			namesView = Collections.unmodifiableSet(names);
		} else {
			names = null;
			namesView = Collections.emptySet();
		}
	}

	@Override
	public String toString() {
		return String.format("Variable(%s %s)", type, name);
	}

	public void assignUp(Type requiredType) {
		type = Type.assignUp(type, requiredType);
	}

	public void assignDown(Type requiredType) {
		type = Type.assignDown(type, requiredType);
	}

	/** @return {@code true}, если имя переменной неизменяемо */
	public boolean isNameFixed() {
		return names == null;
	}

	/** @throws IllegalStateException если имя переменной неизменяемо */
	public void setName(String name) {
		if (isNameFixed())
			throw new IllegalStateException("Name of " + this + " is fixed");

		this.name = name;
	}

	/** Добавляет потенциальное имя переменной */
	public void addName(String name) {
		if (names != null)
			names.add(name);
	}

	public @UnmodifiableView Set<String> getNames() {
		return namesView;
	}

	public boolean attemptDefine() {
		if (!defined) {
			return defined = true;
		}

		return false;
	}
}
