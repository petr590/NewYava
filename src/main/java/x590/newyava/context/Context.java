package x590.newyava.context;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.*;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.type.ClassType;

import java.util.List;
import java.util.Optional;

/**
 * Предоставляет доступ к основным свойствам класса
 */
public interface Context {
	Config getConfig();

	int getClassModifiers();

	default boolean isEnumClass() {
		return (getClassModifiers() & Modifiers.ACC_ENUM) != 0;
	}

	ClassType getThisType();

	ClassType getSuperType();

	@Nullable @Unmodifiable List<DecompilingField> getRecordComponents();


	/** @return {@code true} если данный класс импортирован, иначе {@code false}. */
	boolean imported(ClassType classType);

	/** @return поле, найденное в классе или пустой Optional */
	Optional<DecompilingField> findField(FieldDescriptor descriptor);

	/** @return метод, найденный в классе или пустой Optional */
	Optional<DecompilingMethod> findMethod(MethodDescriptor descriptor);

	/** @return класс, найденный среди всех декомпилируемых данным декомпилятором классов
	 * или пустой Optional */
	Optional<DecompilingClass> findClass(@Nullable ClassType classType);
}
