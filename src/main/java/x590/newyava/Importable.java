package x590.newyava;

import x590.newyava.context.ClassContext;

/** Представляет объект, использующий типы, которые необходимо импортировать. */
public interface Importable {

	/** Добавляет импорты всех типов, которые используются данным объектом. */
	void addImports(ClassContext context);
}
