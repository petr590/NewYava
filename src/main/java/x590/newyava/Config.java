package x590.newyava;

import lombok.Builder;

@Builder
public class Config {

	/** Если {@code true}, то будет игнорировать имена и типы переменных, указанные в классе. */
	private final boolean ignoreVariableTable;

	/** Если {@code true}, то всегда будет писать фигурные скобки в условиях, циклах и т.д. */
	private final boolean alwaysWriteBrackets;

	/** Если {@code true}, то всегда будет писать {@code this} или класс при обращении к полям и методам класса. */
	private final boolean alwaysWriteThisAndClass;

	/** Если {@code true}, то программа будет падать при
	 * {@link x590.newyava.exception.DecompilationException DecompilationException} и
	 * {@link x590.newyava.exception.DisassemblingException DisassemblingException}.
	 * Иначе просто выведется сообщение об ошибке и будет записан заголовок метода. */
	private final boolean failOnDecompilationException;

	/** Если {@code true}, то будет импортировать вложенные классы
	 * (при совпадении имён предпочтение отдаётся классам верхнего уровня). */
	private final boolean importNestedClasses;

	public boolean ignoreVariableTable() { return ignoreVariableTable; }
	public boolean canOmitBrackets() { return !alwaysWriteBrackets; }
	public boolean canOmitThisAndClass() { return !alwaysWriteThisAndClass; }
	public boolean failOnDecompilationException() { return failOnDecompilationException; }
	public boolean importNestedClasses() { return importNestedClasses; }


	private static Config defaultInstance;

	public static Config defaultConfig() {
		if (defaultInstance != null)
			return defaultInstance;

		return defaultInstance = Config.builder().build();
	}
}
