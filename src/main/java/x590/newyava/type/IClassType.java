package x590.newyava.type;

import x590.newyava.exception.InvalidTypeException;
import x590.newyava.io.SignatureReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Представляет тип класса или параметризованного класса.
 */
public sealed interface IClassType extends IClassArrayType permits ClassType, ParametrizedClassType {

	@Override
	ClassType base();

	static IClassType parse(SignatureReader reader) {
		if (!reader.eat('L')) {
			throw new InvalidTypeException(reader);
		}

		var binName = new StringBuilder();

		for (;;) {
			char ch = reader.next();

			if (Character.isJavaIdentifierPart(ch) || ch == '/') {
				binName.append(ch);
				continue;
			}

			return switch (ch) {
				case ';' -> ClassType.valueOf(binName.toString());
				case '<' -> {
					var type = ParametrizedClassType.valueOf(
							ClassType.valueOf(binName.toString()),
							parseParameters(reader.dec(), ReferenceType::parse)
					);

					yield switch (reader.next()) {
						case '.' -> parseInnerClass(type, reader);
						case ';' -> type;
						default -> throw new InvalidTypeException(reader);
					};
				}

				default -> throw new InvalidTypeException(reader.dec(binName.length() + 1));
			};
		}
	}

	private static IClassType parseInnerClass(IClassType outerClass, SignatureReader reader) {
		var simpleName = new StringBuilder();

		for (;;) {
			char ch = reader.next();

			if (Character.isJavaIdentifierPart(ch)) {
				simpleName.append(ch);
				continue;
			}

			return switch (ch) {
				case '<' -> {
					var type = innerParametrizedClassType(
							outerClass, simpleName,
							parseParameters(reader.dec(), ReferenceType::parse)
					);

					yield switch (reader.next()) {
						case '.' -> parseInnerClass(type, reader);
						case ';' -> type;
						default -> throw new InvalidTypeException(reader.dec());
					};
				}

				case '.' -> parseInnerClass(innerParametrizedClassType(outerClass, simpleName), reader);
				case ';' -> innerParametrizedClassType(outerClass, simpleName);

				default -> throw new InvalidTypeException(reader.dec());
			};
		}
	}

	private static ParametrizedClassType innerParametrizedClassType(IClassType outerClass, StringBuilder simpleName) {
		return innerParametrizedClassType(outerClass, simpleName, Collections.emptyList());
	}

	private static ParametrizedClassType innerParametrizedClassType(
			IClassType outerClass, StringBuilder simpleName, List<ReferenceType> parameters
	) {
		return ParametrizedClassType.valueOf(
				ClassType.valueOf(outerClass.base().getClassBinName() + "$" + simpleName),
				outerClass, parameters
		);
	}

	/**
	 * Парсит всё между треугольными скобками.
	 * @param parser функция, которая принимает {@code reader} и возвращает прочитанный объект.
	 * @throws InvalidTypeException если {@code reader} не начинается с символа {@code '<'}.
	 */
	static <T> List<T> parseParameters(SignatureReader reader, Function<? super SignatureReader, ? extends T> parser) {
		if (!reader.eat('<'))
			throw new InvalidTypeException(reader);

		List<T> parameters = new ArrayList<>();

		while (!reader.eat('>')) {
			parameters.add(parser.apply(reader));
		}

		return parameters;
	}

	static List<IClassType> parseInterfaces(SignatureReader reader) {
		List<IClassType> result = new ArrayList<>();

		while (!reader.isEnd()) {
			result.add(parse(reader));
		}

		return result;
	}
}
