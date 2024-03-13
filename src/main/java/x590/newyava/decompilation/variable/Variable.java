package x590.newyava.decompilation.variable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import x590.newyava.type.Type;

/**
 * Переменная. Содержит тип и имя.
 */
@Getter
@AllArgsConstructor
public final class Variable {
	private final @NotNull Type type;

	@Setter
	private @NotNull String name;

	@Override
	public String toString() {
		return String.format("Variable(%s %s)", type, name);
	}
}
