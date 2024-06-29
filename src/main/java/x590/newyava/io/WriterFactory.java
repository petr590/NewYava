package x590.newyava.io;

import java.io.IOException;
import java.io.Writer;

public interface WriterFactory {

	/**
	 * @param className имя класса в формате {@code "java.lang.Object"}.
	 * @return {@link Writer} для класса с указанным именем.
	 * Может возвращать один и тот же объект при разных вызовах.
	 * @apiNote вложенные классы записываются вместе со внешними,
	 * т.е. {@code className} всегда является именем класса верхнего уровня.
	 */
	Writer getWriter(String className) throws IOException;

	/**
	 * Вызывается после записи класса, служит для освобождения ресурсов {@link Writer}-а,
	 * полученного из метода {@link #getWriter(String)}.
	 */
	void closeWriter(Writer writer) throws IOException;

	/**
	 * Вызывается после записи всех классов, служит для освобождения всех ресурсов данной фабрики.
	 */
	default void close() throws IOException {}
}
