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
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
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

	@Override
	public Config getConfig() {
		return decompiler.getConfig();
	}

	@Override
	public int getClassModifiers() {
		return decompilingClass.getModifiers();
	}

	@Override
	public ReferenceType getThisType() {
		return decompilingClass.getThisType();
	}

	@Override
	public ClassType getSuperType() {
		return decompilingClass.getSuperType();
	}


	private Multiset<ClassType> importCandidates = HashMultiset.create();

	private Set<ClassType> imports;

	private @Nullable ClassContext outer;

	private Multiset<ClassType> getImportCandidates() {
		return outer != null ? outer.getImportCandidates() : importCandidates;
	}

	/**
	 * Устанавливает контекст внешнего класса.
	 * @throws IllegalStateException если контекст внешнего класса уже установлен.
	 * @throws IllegalArgumentException если контекст внешнего класса равен {@code this}.
	 */
	public void setOuterContext(ClassContext outer) {
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

	/** Добавляет импорт указанного класса, если он не объявлен внутри метода */
	public ClassContext addImport(@Nullable ClassType classType) {
		if (classType != null && !classType.isEnclosedInMethod())
			getImportCandidates().add(classType);

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

	/** Добавляет импорты для всех указанных типов */
	public ClassContext addImports(List<ClassType> classTypes) {
		classTypes.forEach(this::addImport);
		return this;
	}

	/**
	 * Вычисляет итоговые импорты. Из нескольких классов
	 * с одинаковым именем берёт самый часто встречающийся.
	 * Если установлен контекст внешнего класса, то ничего не делает.
	 * @throws IllegalStateException если импорты уже вычислены.
	 */
	public void computeImports() {
		if (outer != null)
			return;

		if (imports != null)
			throw new IllegalStateException("Imports already computed");

		imports = new HashSet<>();

		var grouped = importCandidates.entrySet().stream()
				.collect(Collectors.groupingBy(entry -> entry.getElement().getSimpleName()));

		for (var group : grouped.entrySet()) {
			imports.add(group.getValue().stream()
					.max(Comparator.comparingInt(Multiset.Entry::getCount))
					.orElseThrow().getElement());
		}
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

	/** @return {@code true} если данный класс импортирован, иначе {@code false}. */
	@Override
	public boolean imported(ClassType classType) {
		return getImports().contains(classType);
	}


	@Override
	public Optional<DecompilingField> findField(FieldDescriptor descriptor) {
		return decompilingClass.getFields().stream()
				.filter(field -> field.getDescriptor().equals(descriptor)).findAny();
	}

	@Override
	public Optional<DecompilingMethod> findMethod(MethodDescriptor descriptor) {
		return decompilingClass.getMethods().stream()
				.filter(method -> method.getDescriptor().equals(descriptor)).findAny();
	}

	public Stream<DecompilingMethod> findMethods(Predicate<DecompilingMethod> predicate) {
		return decompilingClass.getMethods().stream()
				.filter(predicate);
	}

	@Override
	public Optional<DecompilingClass> findClass(ClassType classType) {
		return decompiler.findClass(classType);
	}
}
