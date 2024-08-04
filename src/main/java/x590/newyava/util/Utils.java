package x590.newyava.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import x590.newyava.type.PrimitiveType;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@UtilityClass
public final class Utils {
	/** Добавляет элемент в коллекцию и возвращает его.
	 * Удобно использовать для более короткой записи кода. */
	public static <T> T addAndGetBack(Collection<? super T> collection, T element) {
		collection.add(element);
		return element;
	}


	/**
	 * Приводит название в нижний camel-case.<br>
	 * Примеры:
	 * <pre>
	 * Integer -> integer
	 * HTML -> html
	 * HTMLDocument -> htmlDocument
	 * HtmlDocument -> htmlDocument
	 * </pre>
	 */
	public static String toLowerCamelCase(String str) {
		int index = 0,
			len = str.length();

		while (index < len && Character.isUpperCase(str.charAt(index))) {
			index++;
		}

		if (index == len) { // Если все символы заглавные, просто уменьшаем их
			return str.toLowerCase();
		}

		if (index > 1) { // Не уменьшать последний заглавный символ, если их больше одного
			index -= 1;
		}

		return str.substring(0, index).toLowerCase() + str.substring(index, len);
	}

	/** Заменяет строку, если она является литералом Java. */
	public static String replaceLiterals(String str) {
		return switch (str) {
			case "boolean"    -> PrimitiveType.BOOLEAN.getVarName();
			case "byte"       -> PrimitiveType.BYTE.getVarName();
			case "short"      -> PrimitiveType.SHORT.getVarName();
			case "char"       -> PrimitiveType.CHAR.getVarName();
			case "int"        -> PrimitiveType.INT.getVarName();
			case "long"       -> PrimitiveType.LONG.getVarName();
			case "float"      -> PrimitiveType.FLOAT.getVarName();
			case "double"     -> PrimitiveType.DOUBLE.getVarName();
			case "void"       -> PrimitiveType.VOID.getVarName();

			case "abstract"   -> "abs";
			case "assert"     -> "assrt";
			case "break"      -> "brk";
			case "case"       -> "cs";
			case "catch"      -> "ctch";
			case "class"      -> "clazz";
			case "const"      -> "cns";
			case "continue"   -> "cont";
			case "default"    -> "def";
			case "do"         -> "d";
			case "else"       -> "els";
			case "enum"       -> "en";
			case "extends"    -> "ext";
			case "false"      -> "fls";
			case "final"      -> "fin";
			case "finally"    -> "finl";
			case "for"        -> "fr";
			case "goto"       -> "gt";
			case "if"         -> "f";
			case "implements" -> "impl";
			case "import"     -> "imp";
			case "instanceof" -> "inst";
			case "interface"  -> "interf";
			case "native"     -> "nat";
			case "new"        -> "mew"; // ^•ﻌ•^
			case "null"       -> "nll";
			case "package"    -> "pack";
			case "private"    -> "priv";
			case "protected"  -> "prot";
			case "public"     -> "pub";
			case "return"     -> "ret";
			case "static"     -> "stat";
			case "strictfp"   -> "strict";
			case "super"      -> "sup";
			case "switch"     -> "swt";
			case "this"       -> "ths";
			case "throw"      -> "thr";
			case "throws"     -> "thrs";
			case "transient"  -> "trans";
			case "true"       -> "tr";
			case "try"        -> "tr";
			case "volatile"   -> "vol";
			case "while"      -> "whl";
			case "_"          -> "__"; // Шта?
			default           -> str;
		};
	}

	/** Возвращает строку в нижнем camel-case, заменяет её если это литерал Java. */
	public static String safeToLowerCamelCase(String str) {
		return replaceLiterals(toLowerCamelCase(str));
	}


	/** @return {@code true}, если список содержит один элемент, и этот элемент соответствует предикату. */
	public static <T> boolean isSingle(List<T> list, Predicate<? super T> predicate) {
		return list.size() == 1 && predicate.test(list.get(0));
	}

	/** @return {@code true}, если список не пуст, и последний элемент соответствует предикату. */
	public static <T> boolean isLast(List<T> list, Predicate<? super T> predicate) {
		int size = list.size();
		return size > 0 && predicate.test(list.get(size - 1));
	}

	/** Удаляет последний элемент из списка. */
	public static void removeLast(List<?> list) {
		list.remove(list.size() - 1);
	}

	/** @return последний элемент из списка. */
	public static <T> T getLast(List<? extends T> list) {
		return list.get(list.size() - 1);
	}

	/** @return первый элемент из списка или {@code null}, если список пуст. */
	public static <T> @Nullable T getFirstOrNull(List<? extends T> list) {
		return list.size() == 0 ? null : list.get(0);
	}

	/** @return последний элемент из списка или {@code null}, если список пуст. */
	public static <T> @Nullable T getLastOrNull(List<? extends T> list) {
		int size = list.size();
		return size == 0 ? null : list.get(list.size() - 1);
	}
}
