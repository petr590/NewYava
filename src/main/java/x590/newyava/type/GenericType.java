package x590.newyava.type;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.Context;
import x590.newyava.exception.InvalidTypeException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;
import x590.newyava.util.Utils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;

/**
 * Дженерик тип. Хранит имя и верхнюю границу.
 */
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenericType implements ReferenceType {

	public static GenericType parse(SignatureReader reader) {
		var name = new StringBuilder();

		while (true) {
			char ch = reader.next();

			if (ch == ';') {
				return new GenericType(name.toString(), ClassType.OBJECT);
			}

			if (Character.isJavaIdentifierPart(ch)) {
				name.append(ch);
				continue;
			}

			throw new InvalidTypeException(reader.dec());
		}
	}

	private static IClassArrayType getBase(java.lang.reflect.Type type) {
		return switch (type) {
			case Class<?> clazz                  -> IClassArrayType.valueOf(clazz);
			case ParameterizedType parameterized -> ClassType.valueOf((Class<?>) parameterized.getRawType());
			case TypeVariable<?> typeVar         -> getBase(typeVar.getBounds()[0]);
			case GenericArrayType genericArray   -> ArrayType.forType(ReferenceType.valueOf(genericArray.getGenericComponentType()));

			default -> throw new IllegalArgumentException(String.format(
					"Unknown type %s (class %s)", type, type.getClass()
			));
		};
	}

	public static GenericType valueOf(String name, java.lang.reflect.Type firstBound) {
		return new GenericType(name, getBase(firstBound));
	}


	private final String name;
	private final IClassArrayType base;

	@Override
	public Type base() {
		return base;
	}

	@Override
	public @Nullable ReferenceType getSuperType() {
		return null;
	}

	@Override
	public @Unmodifiable List<? extends ReferenceType> getInterfaces() {
		return Collections.emptyList();
	}

	@Override
	public String getVarName() {
		return Utils.safeToLowerCamelCase(name);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(name);
	}

	@Override
	public String toString() {
		return name;
	}
}
