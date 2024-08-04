package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.decompilation.variable.VarUsage;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.Deque;
import java.util.List;
import java.util.Optional;

public interface Operation extends Importable {

	/** Возвращаемый тип операции, может быть void. */
	Type getReturnType();

	/** Неявный возвращаемый тип операции. */
	default Type getImplicitType() {
		return getReturnType();
	}

	/** Разрешает неявное приведение к более широкому типу.
	 * Должен вызываться из метода {@link #inferType(Type)} */
	default void allowImplicitCast() {}

	/** Запрещает опускать явное приведение констант к {@code byte} или {@code short}. */
	default void denyByteShortImplicitCast() {}

	/** Вызывается при инициализации {@link Scope} */
	default void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {}

	/* ------------------------------------------------- Properties ------------------------------------------------- */

	/** @return {@code true}, если операция является ссылкой на {@code this}, иначе {@code false}.
	 * Если метод вернул {@code true}, то операция гарантированно является экземпляром
	 * {@link x590.newyava.decompilation.operation.variable.ILoadOperation ILoadOperation}. */
	default boolean isThisRef() {
		return false;
	}

	/** @return {@code true}, если операция является вызовом super-конструктора по умолчанию
	 * (в том числе и конструктора {@code Enum(String, int)}). */
	default boolean isDefaultConstructor(MethodContext context) {
		return false;
	}

	/** @return {@code true}, если операция внешне является scope-ом.
	 * Это не гарантирует, что операция - экземпляр класса {@link Scope}.
	 * Однако, все {@link Scope}-ы должны возвращать {@code true} из этого метода. */
	default boolean isScopeLike() {
		return false;
	}

	/** @return {@code true}, если операция терминальная ({@code return} или {@code throw}) */
	default boolean isTerminal() {
		return false;
	}

	/** @return {@code true}, если операция является {@code return} */
	default boolean isReturn() {
		return false;
	}

	/** @return {@code true}, если операция является {@code throw} */
	default boolean isThrow() {
		return false;
	}


	/* ---------------------------------------- Variables recursive methods ----------------------------------------- */

	/** @return {@code true} если операция или одна из вложенных операций использует
	 * какие-либо локальные переменные (читает/записывает) */
	default boolean usesAnyVariable() {
		return getNestedOperations().stream().anyMatch(Operation::usesAnyVariable);
	}


	/** Вызывается перед вызовом метода {@link x590.newyava.DecompilingMethod#initVariables(Context)
	 * DecompilingMethod.initVariables(Context)}.
	 * @apiNote возможно объединение с методом {@link Scope#afterDecompilation(MethodContext)} */
	@MustBeInvokedByOverriders
	default void beforeVariablesInit(MethodScope methodScope) {
		getNestedOperations().forEach(operation -> operation.beforeVariablesInit(methodScope));
	}


	/** @return способ <b>первого</b> использования переменной в указанном слоте.
	 * Если переменная не используется, то метод возвращает {@link VarUsage#NONE}. */
	default VarUsage getVarUsage(int slotId) {
		return getNestedOperations().stream()
				.map(operation -> operation.getVarUsage(slotId))
				.filter(varUsage -> varUsage != VarUsage.NONE)
				.findFirst().orElse(VarUsage.NONE);
	}


	/** Выводит типы переменных. Должен вызываться рекурсивно для всех дочерних операций.
	 * Обновляет возвращаемый тип операции, если он изменяем.
	 * @param requiredType требуемый тип операции. Игнорируется большинством операций, так как
	 * они имеют фиксированный тип. Используется для выведения типов переменных и констант. */
	default void inferType(Type requiredType) {}


	/** Объявляет переменные, которые ещё не объявлены.
	 * @return {@code true}, если переменная была объявлена в данной операции, иначе {@code false}. */
	@MustBeInvokedByOverriders
	default boolean declareVariables() {
		boolean result = false;

		for (var operation : getNestedOperations()) {
			result |= operation.declareVariables();
		}

		return result;
	}


	/** Инициализирует возможные имена переменных */
	@MustBeInvokedByOverriders
	default void initPossibleVarNames() {
		getNestedOperations().forEach(Operation::initPossibleVarNames);
	}


	/** @return возможное имя переменной (например, для вызова метода {@code getName()}
	 * может вернуть {@code "name"}) */
	default Optional<String> getPossibleVarName() {
		return Optional.empty();
	}


	/** Добавляет имя переменной, которую представляет операция */
	default void addPossibleVarName(@Nullable String name) {}


	/* ----------------------------------------- Another recursive methods ------------------------------------------ */

	/**
	 * Для {@link x590.newyava.decompilation.operation.other.FieldOperation FieldOperation}
	 * добавляет инициализатор к нестатическому полю, если это возможно.
	 * @return {@code true} в случае успеха, иначе {@code false}.
	 */
	default boolean initInstanceField(MethodContext context) {
		return false;
	}

	/**
	 * @return для {@link x590.newyava.decompilation.operation.other.FieldOperation FieldOperation}
	 * возвращает {@code true}, если нестатическое поле инициализировано.
	 */
	default boolean isInstanceFieldInitialized() {
		return false;
	}

	/**
	 * Для {@link Scope} проходится по всем операциям <b>в обратном порядке</b>.
	 * Инициализирует {@code yield} для всех операций, которые являются {@code break}.
	 */
	default void initYield(Scope switchScope, Deque<Operation> pushedOperations) {}


	/** @return все вложенные операции для рекурсивного вызова любого метода у всех операций */
	default @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of();
	}


	/* --------------------------------------------- addImports, write --------------------------------------------- */

	@Override
	default void addImports(ClassContext context) {}

	default Priority getPriority() {
		return Priority.DEFAULT;
	}

	void write(DecompilationWriter out, MethodWriteContext context);

	/**
	 * Записывает массив без {@code new <type>[]} (если возможно).
	 * Для всех остальных операций работает точно также,
	 * как {@link #write(DecompilationWriter, MethodWriteContext)}
	 */
	default void writeAsArrayInitializer(DecompilationWriter out, MethodWriteContext context) {
		write(out, context);
	}

	/**
	 * Записывает литерал {@code int} как {@code char}.
	 * Для всех остальных операций работает точно также,
	 * как {@link #write(DecompilationWriter, MethodWriteContext)}
	 */
	default void writeIntAsChar(DecompilationWriter out, MethodWriteContext context) {
		write(out, context);
	}

	/**
	 * Записывает литералы {@code int} и {@code long} в hex-формате.
	 * Для всех остальных операций работает точно также,
	 * как {@link #write(DecompilationWriter, MethodWriteContext)}
	 */
	default void writeHex(DecompilationWriter out, MethodWriteContext context) {
		write(out, context);
	}
}
