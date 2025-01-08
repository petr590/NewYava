package x590.newyava.context;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.*;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.type.IClassArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.IClassType;

import java.util.List;
import java.util.Optional;

/**
 * Предоставляет доступ к основным свойствам класса
 */
public interface Context {
	Config getConfig();

	DecompilingClass getDecompilingClass();

	int getClassModifiers();

	default boolean isEnumClass() {
		return (getClassModifiers() & Modifiers.ACC_ENUM) != 0;
	}

	ClassType getThisType();

	IClassType getSuperType();

	@Nullable @Unmodifiable List<DecompilingField> getRecordComponents();


	/** @return {@code true} если мы зашли в область видимости текущего класса. */
	boolean entered();

	/** @return {@code true} если данный класс импортирован, иначе {@code false}. */
	boolean imported(ClassType classType);


	/** @return поле, найденное по указанному дескриптору или пустой Optional */
	Optional<DecompilingField> findField(FieldDescriptor descriptor);

	/** @return метод, найденный по указанному дескриптору или пустой Optional */
	Optional<DecompilingMethod> findMethod(MethodDescriptor descriptor);

	/** @return класс, найденный среди всех декомпилируемых данным декомпилятором классов
	 * или пустой Optional */
	Optional<DecompilingClass> findClass(@Nullable IClassArrayType type);


	/** @return поле, найденное по указанному дескриптору или пустой Optional */
	Optional<? extends IField> findIField(FieldDescriptor descriptor);

	/** @return метод, найденный по указанному дескриптору или пустой Optional */
	Optional<? extends IMethod> findIMethod(MethodDescriptor descriptor);

	/** @return класс, найденный среди всех декомпилируемых данным декомпилятором классов
	 * или пустой Optional */
	Optional<? extends IClass> findIClass(@Nullable IClassArrayType type);


	@Nullable FieldDescriptor getConstant(byte value);

	@Nullable FieldDescriptor getConstant(short value);

	@Nullable FieldDescriptor getConstant(char value);

	@Nullable FieldDescriptor getConstant(int value);

	@Nullable FieldDescriptor getConstant(long value);

	@Nullable FieldDescriptor getConstant(float value);

	@Nullable FieldDescriptor getConstant(double value);

	@Nullable FieldDescriptor getConstant(String value);
}
