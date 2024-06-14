package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public interface Operation extends Importable {

	/** Возвращаемый тип операции, может быть void */
	Type getReturnType();

	/** Разрешает неявное приведение к более широкому типу.
	 * Должен вызываться из метода {@link #inferType(Type)} */
	default void allowImplicitCast() {}

	/** Вызывается при инициализации {@link Scope} */
	default void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {}

	/* ------------------------------------------------- Properties ------------------------------------------------- */

	/** @return {@code true}, если операция является ссылкой на {@code this}, иначе {@code false} */
	default boolean isThisRef() {
		return false;
	}

	/** @return {@code true}, если операция является вызовом super-конструктора по умолчанию */
	default boolean isDefaultConstructor(MethodContext context) {
		return false;
	}

	/** @return {@code true}, если операция внешне является scope-ом.
	 * Это не гарантирует, что операция - экземпляр класса {@link Scope} */
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


	/* --------------------------------------------- Recursive methods --------------------------------------------- */

	/** @return {@code true} если операция или одна из вложенных операций использует
	 * какие-либо локальные переменные (читает/записывает) */
	default boolean usesAnyVariable() {
		return getNestedOperations().stream().anyMatch(Operation::usesAnyVariable);
	}

	@MustBeInvokedByOverriders
	default void beforeVariablesInit(MethodScope methodScope) {
		getNestedOperations().forEach(operation -> operation.beforeVariablesInit(methodScope));
	}

	/** Выводит типы переменных. Должен вызываться рекурсивно для всех дочерних операций.
	 * Обновляет возвращаемый тип операции, если он изменяем.
	 * @param requiredType требуемый тип операции. Игнорируется большинством операций, так как
	 * они имеют фиксированный тип. Используется для выведения типов переменных и констант. */
	default void inferType(Type requiredType) {}

	/** Объявляет переменную в операции {@link StoreOperation}, если она ещё не объявлена. */
	@MustBeInvokedByOverriders
	default void declareVariableOnStore() {
		getNestedOperations().forEach(Operation::declareVariableOnStore);
	}


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
}
