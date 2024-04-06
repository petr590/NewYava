package x590.newyava.context;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.DecompilingClass;
import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.Importable;
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

	@Getter
	private final DecompilingClass decompilingClass;

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


	private Object2IntMap<ClassType> importCandidates = new Object2IntArrayMap<>();

	private Set<ClassType> imports;

	private @Nullable ClassContext outer;

	private Object2IntMap<ClassType> getImportCandidates() {
		return outer != null ? outer.getImportCandidates() : importCandidates;
	}

	public void setOuterContext(ClassContext outer) {
		if (this.outer != null) {
			throw new IllegalStateException("Outer context already has been set");
		}

		this.outer = outer;
		this.importCandidates = null;
	}

	public ClassContext addImport(@Nullable Type type) {
		if (type != null)
			type.addImports(this);

		return this;
	}

	public ClassContext addImport(@Nullable ClassType classType) {
		if (classType != null)
			getImportCandidates().compute(classType, (clType, count) -> count == null ? 1 : count + 1);

		return this;
	}

	public ClassContext addImportsFor(@Nullable Importable importable) {
		if (importable != null)
			importable.addImports(this);

		return this;
	}

	public ClassContext addImportsFor(@Nullable Iterable<? extends @Nullable Importable> importables) {
		if (importables != null)
			importables.forEach(this::addImportsFor);

		return this;
	}

	public ClassContext addImports(List<ClassType> classTypes) {
		classTypes.forEach(this::addImport);
		return this;
	}

	public void computeImports() {
		if (outer != null)
			return;

		if (imports != null)
			throw new IllegalStateException("Imports already computed");

		imports = new HashSet<>();

		var grouped = importCandidates.object2IntEntrySet().stream()
				.collect(Collectors.groupingBy(entry -> entry.getKey().getSimpleName()));

		for (var group : grouped.entrySet()) {
			imports.add(group.getValue().stream()
					.max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
					.orElseThrow().getKey());
		}
	}

	public @Unmodifiable Set<ClassType> getImports() {
		if (outer != null)
			return outer.getImports();

		if (imports == null)
			throw new IllegalStateException("Imports are not initialized yet");

		return Collections.unmodifiableSet(imports);
	}

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
}
