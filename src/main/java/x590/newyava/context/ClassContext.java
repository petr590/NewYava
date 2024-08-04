package x590.newyava.context;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.*;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Представляет контекст класса.
 */
@RequiredArgsConstructor
public class ClassContext implements Context {

	private final Decompiler decompiler;

	@Getter
	private final DecompilingClass decompilingClass;

	/* -------------------------------------------------- Getters --------------------------------------------------- */

	@Override
	public Config getConfig() {
		return decompiler.getConfig();
	}

	@Override
	public int getClassModifiers() {
		return decompilingClass.getModifiers();
	}

	@Override
	public ClassType getThisType() {
		return decompilingClass.getThisType();
	}

	@Override
	public ClassType getSuperType() {
		return decompilingClass.getSuperType();
	}

	@Override
	public @Nullable @Unmodifiable List<DecompilingField> getRecordComponents() {
		return decompilingClass.getRecordComponents();
	}

	/* -------------------------------------------------- Imports --------------------------------------------------- */
	/** Кандидаты на импорт */
	private @Nullable Multiset<ClassType> importCandidates = HashMultiset.create();

	/** Простые имена классов, объявленных в данном классе, а также имя данного класса.
	 * Нельзя импортировать классы с такими же именами. */
	private final Set<String> declaredClassesNames = new HashSet<>();

	/** Конечные импорты. Для вложенных классов равно {@code null}. */
	private @Nullable Set<ClassType> imports;

	/** Импорты {@link #getThisType()} и всех его внешних классов */
	private @Nullable Set<ClassType> thisClassImports;

	/** Контекст внешнего класса. Если он не равен {@code null}, то к нему делегируются вся работа с импортами. */
	private @Nullable ClassContext outer;

	/**
	 * Устанавливает контекст внешнего класса.
	 * @throws NullPointerException если переданный параметр равен {@code null}.
	 * @throws IllegalStateException если контекст внешнего класса уже установлен.
	 * @throws IllegalArgumentException если контекст внешнего класса равен {@code this}.
	 */
	public void setOuterContext(ClassContext outer) {
		Objects.requireNonNull(outer);

		if (this.outer != null) {
			throw new IllegalStateException("Outer context already has been set");
		}

		if (outer == this) {
			throw new IllegalArgumentException("outer == this");
		}

		this.outer = outer;
		this.importCandidates = null;
	}

	/** Добавляет необходимые импорты для типа */
	public ClassContext addImport(@Nullable Type type) {
		if (type != null)
			type.addImports(this);

		return this;
	}

	/** Добавляет импорт указанного класса, если он не объявлен внутри метода.
	 * Не импортирует вложенные классы, если {@link Config#importNestedClasses()} равно {@code false},
	 * вместо этого импортирует класс верхнего уровня. */
	public ClassContext addImport(@Nullable ClassType classType) {
		if (outer != null) {
			outer.addImport(classType);
			return this;
		}

		if (classType == null || classType.isEnclosedInMethod()) {
			return this;
		}

		if (classType.getTopLevelClass().equals(getThisType())) {
			declaredClassesNames.add(classType.getSimpleName());
		}

		assert importCandidates != null;

		if (classType.isNested() && !getConfig().importNestedClasses()) {
			importCandidates.add(classType.getTopLevelClass());
		} else {
			importCandidates.add(classType);
		}

		return this;
	}

	/** Добавляет импорты для всех указанных типов */
	public ClassContext addImports(@Unmodifiable List<? extends ClassType> classTypes) {
		classTypes.forEach(this::addImport);
		return this;
	}

	/** Добавляет импорты для переданного объекта */
	public ClassContext addImportsFor(@Nullable Importable importable) {
		if (importable != null)
			importable.addImports(this);

		return this;
	}

	/** Добавляет импорты для всех переданных объектов */
	public ClassContext addImportsFor(@Nullable Iterable<? extends @Nullable Importable> importables) {
		if (importables != null)
			importables.forEach(this::addImportsFor);

		return this;
	}

	/**
	 * Вычисляет итоговые импорты. Из нескольких классов
	 * с одинаковым именем берёт самый часто встречающийся.
	 * Если установлен контекст внешнего класса, то ничего не делает.
	 * @throws IllegalStateException если импорты уже вычислены.
	 */
	public void computeImports() {
		thisClassImports = new HashSet<>();
		for (ClassType type = getThisType(); type != null; type = type.getOuter()) {
			thisClassImports.add(type);
		}

		if (outer != null)
			return;

		if (imports != null)
			throw new IllegalStateException("Imports already computed");

		imports = new HashSet<>();

		assert importCandidates != null;

		var grouped = importCandidates.entrySet().stream()
				.collect(Collectors.groupingBy(entry -> entry.getElement().getSimpleName()));

		for (var group : grouped.entrySet()) {
			if (declaredClassesNames.contains(group.getKey())) {
				continue;
			}

			imports.add(group.getValue().stream()
					.max(ClassContext::compareImportCandidates)
					.orElseThrow().getElement());
		}
	}

	/** Сравнивает две записи по количеству использований.
	 * Классы верхнего уровня имеют приоритет. */
	private static int compareImportCandidates(Multiset.Entry<ClassType> entry1, Multiset.Entry<ClassType> entry2) {
		boolean isNested1 = entry1.getElement().isNested(),
				isNested2 = entry2.getElement().isNested();

		return isNested1 != isNested2 ?
				(isNested1 ? -1 : 1) :
				entry1.getCount() - entry2.getCount();
	}


	/**
	 * @return набор импортируемых классов, вычисленный методом {@link #computeImports()}.
	 * @throws IllegalStateException если импорты ещё не вычислены.
	 */
	public @Unmodifiable Set<ClassType> getImports() {
		if (outer != null)
			return outer.getImports();

		if (imports == null)
			throw new IllegalStateException("Imports are not computed yet");

		return Collections.unmodifiableSet(imports);
	}

	/** Перед классом мы должны писать имя этого класса при обращении к его членам.
	 * Пока класс не начался, эта переменная равна {@code false}. */
	private boolean entered;

	public void enter() {
		entered = true;
	}

	public void exit() {
		entered = false;
	}


	/** @return {@code true}, если класс импортирован. */
	@Override
	public boolean imported(ClassType classType) {
		if (getImports().contains(classType)) return true;

		assert thisClassImports != null;

		return thisClassImports.contains(classType) ||
				(entered && thisClassImports.contains(classType.getOuter()));
	}


	/* ---------------------------------------------------- find ---------------------------------------------------- */

	@Override
	public Optional<DecompilingField> findField(FieldDescriptor descriptor) {
		return descriptor.hostClass().equals(getThisType()) ?
				decompilingClass.findField(descriptor) :
				findClass(descriptor.hostClass()).flatMap(clazz -> clazz.findField(descriptor));
	}

	@Override
	public Optional<DecompilingMethod> findMethod(MethodDescriptor descriptor) {
		return descriptor.hostClass().equals(getThisType()) ?
				decompilingClass.findMethod(descriptor) :
				findClass(descriptor.hostClass()).flatMap(clazz -> clazz.findMethod(descriptor));
	}

	/** @return поток всех методов данного класса, которые соответствуют предикату. */
	public Stream<DecompilingMethod> findMethods(Predicate<DecompilingMethod> predicate) {
		return decompilingClass.getMethods().stream().filter(predicate);
	}

	@Override
	public Optional<DecompilingClass> findClass(@Nullable ClassArrayType type) {
		return decompiler.findClass(type);
	}

	@Override
	public Optional<? extends IField> findIField(FieldDescriptor descriptor) {
		return descriptor.hostClass().equals(getThisType()) ?
				decompilingClass.findField(descriptor) :
				findIClass(descriptor.hostClass()).flatMap(clazz -> clazz.findField(descriptor));
	}

	@Override
	public Optional<? extends IMethod> findIMethod(MethodDescriptor descriptor) {
		return descriptor.hostClass().equals(getThisType()) ?
				decompilingClass.findMethod(descriptor) :
				findIClass(descriptor.hostClass()).flatMap(clazz -> clazz.findMethod(descriptor));
	}

	@Override
	public Optional<? extends IClass> findIClass(@Nullable ClassArrayType type) {
		return decompiler.findIClass(type);
	}
}
