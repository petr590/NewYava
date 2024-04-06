package x590.newyava.decompilation.operation;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

import java.util.List;

public interface Operation extends Importable {

	/** Возвращаемый тип операции, может быть void */
	Type getReturnType();

	/** При инициализации {@link Scope} */
	default void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {}

	@Override
	default void addImports(ClassContext context) {}

	default void updateReturnType(Type newType) {}

	default boolean isThisRef() {
		return false;
	}

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


	/**
	 * @return все вложенные операции для рекурсивного вызова любого метода у всех операций
	 */
	default @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return List.of();
	}


	@MustBeInvokedByOverriders
	default void beforeVariablesInit(MethodScope methodScope) {
		getNestedOperations().forEach(operation -> operation.beforeVariablesInit(methodScope));
	}

	default Priority getPriority() {
		return Priority.DEFAULT;
	}

	void write(DecompilationWriter out, Context context);

	/**
	 * Записывает массив без {@code new <type>[]} (если возможно).
	 * Для всех остальных операций работает точно также,
	 * как {@link #write(DecompilationWriter, Context)}
	 */
	default void writeAsArrayInitializer(DecompilationWriter out, Context context) {
		write(out, context);
	}
}
