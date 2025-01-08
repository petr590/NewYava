package x590.newyava.context;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.*;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
	public IClassType getSuperType() {
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
	public ClassContext addImports(@Unmodifiable List<? extends IClassType> classTypes) {
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

	@Override
	public boolean entered() {
		return entered;
	}

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
	public Optional<DecompilingClass> findClass(@Nullable IClassArrayType type) {
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
	public Optional<? extends IClass> findIClass(@Nullable IClassArrayType type) {
		return decompiler.findIClass(type);
	}


	/* ------------------------------------------------- Constants -------------------------------------------------- */

	private static final Int2ObjectMap<FieldDescriptor> DEFAULT_INT_TABLE = new Int2ObjectOpenHashMap<>(Map.of(
			Integer.MIN_VALUE, new FieldDescriptor(ClassType.INTEGER, "MIN_VALUE", PrimitiveType.INT),
			Integer.MAX_VALUE, new FieldDescriptor(ClassType.INTEGER, "MAX_VALUE", PrimitiveType.INT)
	));

	private static final Long2ObjectMap<FieldDescriptor> DEFAULT_LONG_TABLE = new Long2ObjectOpenHashMap<>(Map.of(
			Long.MIN_VALUE, new FieldDescriptor(ClassType.LONG, "MIN_VALUE", PrimitiveType.LONG),
			Long.MAX_VALUE, new FieldDescriptor(ClassType.LONG, "MAX_VALUE", PrimitiveType.LONG)
	));

	private static final Float2ObjectMap<FieldDescriptor> DEFAULT_FLOAT_TABLE = new Float2ObjectOpenHashMap<>(Map.of(
			Float.MIN_VALUE,         new FieldDescriptor(ClassType.FLOAT, "MIN_VALUE",         PrimitiveType.FLOAT),
			Float.MIN_NORMAL,        new FieldDescriptor(ClassType.FLOAT, "MIN_NORMAL",        PrimitiveType.FLOAT),
			Float.MAX_VALUE,         new FieldDescriptor(ClassType.FLOAT, "MAX_VALUE",         PrimitiveType.FLOAT),
			Float.NaN,               new FieldDescriptor(ClassType.FLOAT, "NaN",               PrimitiveType.FLOAT),
			Float.POSITIVE_INFINITY, new FieldDescriptor(ClassType.FLOAT, "POSITIVE_INFINITY", PrimitiveType.FLOAT),
			Float.NEGATIVE_INFINITY, new FieldDescriptor(ClassType.FLOAT, "NEGATIVE_INFINITY", PrimitiveType.FLOAT)
	));

	private static final Double2ObjectMap<FieldDescriptor> DEFAULT_DOUBLE_TABLE = new Double2ObjectOpenHashMap<>(Map.of(
			Double.MIN_VALUE,         new FieldDescriptor(ClassType.DOUBLE, "MIN_VALUE",         PrimitiveType.DOUBLE),
			Double.MIN_NORMAL,        new FieldDescriptor(ClassType.DOUBLE, "MIN_NORMAL",        PrimitiveType.DOUBLE),
			Double.MAX_VALUE,         new FieldDescriptor(ClassType.DOUBLE, "MAX_VALUE",         PrimitiveType.DOUBLE),
			Double.NaN,               new FieldDescriptor(ClassType.DOUBLE, "NaN",               PrimitiveType.DOUBLE),
			Double.POSITIVE_INFINITY, new FieldDescriptor(ClassType.DOUBLE, "POSITIVE_INFINITY", PrimitiveType.DOUBLE),
			Double.NEGATIVE_INFINITY, new FieldDescriptor(ClassType.DOUBLE, "NEGATIVE_INFINITY", PrimitiveType.DOUBLE),
			Math.PI,                  new FieldDescriptor(ClassType.MATH,   "PI",                PrimitiveType.DOUBLE),
			Math.E,                   new FieldDescriptor(ClassType.MATH,   "E",                 PrimitiveType.DOUBLE),
			Math.TAU,                 new FieldDescriptor(ClassType.MATH,   "TAU",               PrimitiveType.DOUBLE)
	));

	private final Byte2ObjectMap<FieldDescriptor> byteConstantTable = new Byte2ObjectOpenHashMap<>();
	private final Short2ObjectMap<FieldDescriptor> shortConstantTable = new Short2ObjectOpenHashMap<>();
	private final Char2ObjectMap<FieldDescriptor> charConstantTable = new Char2ObjectOpenHashMap<>();
	private final Int2ObjectMap<FieldDescriptor> intConstantTable = new Int2ObjectOpenHashMap<>();
	private final Long2ObjectMap<FieldDescriptor> longConstantTable = new Long2ObjectOpenHashMap<>();
	private final Float2ObjectMap<FieldDescriptor> floatConstantTable = new Float2ObjectOpenHashMap<>();
	private final Double2ObjectMap<FieldDescriptor> doubleConstantTable = new Double2ObjectOpenHashMap<>();
	private final Map<String, FieldDescriptor> stringConstantTable = new HashMap<>();

	public void initConstantTables(@Unmodifiable List<DecompilingField> fields) {
		for (var field : fields) {
			var constant = field.getConstantValue();

			if (constant != null) {
				switch (constant) {
					case Byte byteVal -> {
						put(byteConstantTable, byteVal, field);
						put(shortConstantTable, (short)byteVal, field);
						put(intConstantTable, byteVal, field);
					}

					case Short shortVal -> {
						put(shortConstantTable, shortVal, field);
						put(intConstantTable, shortVal, field);
					}

					case Character charVal -> put(charConstantTable, charVal, field);
					case Integer intVal    -> put(intConstantTable, intVal, field);
					case Long longVal      -> put(longConstantTable, longVal, field);
					case Float floatVal    -> put(floatConstantTable, floatVal, field);
					case Double doubleVal  -> put(doubleConstantTable, doubleVal, field);
					case String stringVal  -> put(stringConstantTable, stringVal, field);
					default -> {}
				}
			}
		}

		for (var entry : DEFAULT_INT_TABLE.int2ObjectEntrySet()) {
			if (!intConstantTable.containsKey(entry.getIntKey())) {
				intConstantTable.put(entry.getIntKey(), entry.getValue());
			}
		}

		addDefaults(
				DEFAULT_INT_TABLE.int2ObjectEntrySet(), intConstantTable,
				(entry, table) -> table.get(entry.getIntKey()),
				(entry, table) -> table.put(entry.getIntKey(), entry.getValue())
		);

		addDefaults(
				DEFAULT_LONG_TABLE.long2ObjectEntrySet(), longConstantTable,
				(entry, table) -> table.get(entry.getLongKey()),
				(entry, table) -> table.put(entry.getLongKey(), entry.getValue())
		);

		addDefaults(
				DEFAULT_FLOAT_TABLE.float2ObjectEntrySet(), floatConstantTable,
				(entry, table) -> table.get(entry.getFloatKey()),
				(entry, table) -> table.put(entry.getFloatKey(), entry.getValue())
		);

		addDefaults(
				DEFAULT_DOUBLE_TABLE.double2ObjectEntrySet(), doubleConstantTable,
				(entry, table) -> table.get(entry.getDoubleKey()),
				(entry, table) -> table.put(entry.getDoubleKey(), entry.getValue())
		);
	}

	private <T, M> void addDefaults(
			Iterable<T> defaultSet, M map, BiFunction<T, M, @Nullable FieldDescriptor> containsKey, BiConsumer<T, M> put
	) {
		for (T entry : defaultSet) {
			if (containsKey.apply(entry, map) == null) {
				put.accept(entry, map);
			}
		}
	}

	private void put(Int2ObjectMap<FieldDescriptor> table, int key, DecompilingField field) {
		if (table.containsKey(key)) {
			table.put(key, null);
		} else {
			table.put(key, field.getDescriptor());
		}
	}

	private <K> void put(Map<K, FieldDescriptor> table, K key, DecompilingField field) {
		if (table.containsKey(key)) {
			table.put(key, null);
		} else {
			table.put(key, field.getDescriptor());
		}
	}

	@Override
	public @Nullable FieldDescriptor getConstant(byte value) {
		return byteConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(short value) {
		return shortConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(char value) {
		return charConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(int value) {
		return intConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(long value) {
		return longConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(float value) {
		return floatConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(double value) {
		return doubleConstantTable.get(value);
	}

	@Override
	public @Nullable FieldDescriptor getConstant(String value) {
		return stringConstantTable.get(value);
	}
}
