package x590.newyava.context;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.DecompilingClass;
import x590.newyava.Importable;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ClassContext {

	@Getter
	private final DecompilingClass decompilingClass;

	public ReferenceType getThisType() {
		return decompilingClass.getThisType();
	}

	public ClassType getSuperType() {
		return decompilingClass.getSuperType();
	}


	private final Object2IntMap<ClassType> importsCandidates = new Object2IntArrayMap<>();

	private Set<ClassType> imports;

	public ClassContext addImport(@Nullable Type type) {
		if (type != null)
			type.addImports(this);

		return this;
	}

	public ClassContext addImport(@Nullable ClassType classType) {
		if (classType != null)
			importsCandidates.compute(classType, (clType, count) -> count == null ? 1 : count + 1);

		return this;
	}

	public ClassContext addImportsFor(@Nullable Importable importable) {
		if (importable != null)
			importable.addImports(this);

		return this;
	}

	public ClassContext addImportsFor(Iterable<? extends Importable> importables) {
		importables.forEach(this::addImportsFor);
		return this;
	}

	public ClassContext addImports(List<ClassType> classTypes) {
		classTypes.forEach(this::addImport);
		return this;
	}

	public void computeImports() {
		if (imports != null)
			throw new IllegalStateException("Imports already computed");

		imports = new HashSet<>();

		var grouped = importsCandidates.object2IntEntrySet().stream()
				.collect(Collectors.groupingBy(entry -> entry.getKey().getSimpleName()));

		for (var group : grouped.entrySet()) {
			imports.add(group.getValue().stream()
					.max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
					.orElseThrow().getKey());
		}
	}

	public @Unmodifiable Set<ClassType> getImports() {
		if (imports == null)
			throw new IllegalStateException("Imports are not initialized yet");

		return Collections.unmodifiableSet(imports);
	}

	public boolean imported(ClassType classType) {
		return imports.contains(classType);
	}
}
