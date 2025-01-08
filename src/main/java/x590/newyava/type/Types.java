package x590.newyava.type;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import java.util.Collections;
import java.util.List;

/**
 * Константы вынесены в отдельный класс для того,
 * чтобы избежать циклической инициализации классов
 */
@UtilityClass
public final class Types {
	/** Любой тип. Используется переменной, когда её тип ещё неизвестен. */
	public static final Type ANY_TYPE = AnyType.INSTANCE;

	/** Массив неизвестного типа. */
	public static final ArrayType ANY_ARRAY_TYPE = ArrayType.forType(ANY_TYPE);

	/** Любой ссылочный тип. Если тип переменной так и останется любым, то
	 * он будет записан как {@link ClassType#OBJECT}. */
	public static final ReferenceType ANY_OBJECT_TYPE = AnyObjectType.PLAIN;

	/** Любой generic-тип, записывается как знак вопроса. */
	public static final ReferenceType ANY_WILDCARD_TYPE = AnyObjectType.WILDCARD;


	private sealed interface IAnyType extends Type {
		@Override
		default void addImports(ClassContext context) {
			context.addImport(ClassType.OBJECT);
		}

		@Override
		default void write(DecompilationWriter out, Context context) {
			out.record(ClassType.OBJECT, context);
		}
	}


	private enum AnyType implements IAnyType {
		INSTANCE;

		@Override
		public String getVarName() {
			return "var";
		}

		@Override
		public String toString() {
			return "<any-type>";
		}
	}

	@RequiredArgsConstructor
	private enum AnyObjectType implements IAnyType, ReferenceType {
		PLAIN("<any-object-type>"),
		WILDCARD("?") {
			@Override
			public void write(DecompilationWriter out, Context context) {
				out.record('?');
			}
		};

		private final String name;

		@Override
		public String getVarName() {
			return "obj";
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
		public String toString() {
			return name;
		}
	}
}
