package x590.newyava.decompilation.operation.other;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.operator.BinaryOperator;
import x590.newyava.decompilation.operation.operator.OperatorType;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Операция сохранения значения в переменную или в поле.
 * Распознаёт сокращённые операторы, такие как {@code ++}, {@code --}, {@code +=}, {@code *=} и т.д.
 */
public abstract class AssignOperation implements Operation {

	/** Исходное значение, не сокращённое */
	@Getter
	protected final @Nullable Operation value;

	/** Значение, которое идёт справа от знака равно
	 * (например, для {@code x = 5} оно равно {@code 5},
	 * также и для {@code x += 5} оно равно {@code 5}) */
	@Getter
	protected final @Nullable Operation shortValue;

	/** Сокращённый оператор */
	protected @Nullable OperatorType operatorType;

	private final Type returnType;


	/** @return поле {@link #value}.
	 * @throws NullPointerException если поле равно {@code null}. */
	public @NotNull Operation requireValue() {
		return Objects.requireNonNull(value);
	}

	/** @return поле {@link #shortValue}.
	 * @throws NullPointerException если поле равно {@code null}. */
	public @NotNull Operation requireShortValue() {
		return Objects.requireNonNull(shortValue);
	}


	/**
	 * @param value значение, которое сохраняется в переменную/поле.
	 * @param defaultReturnType возвращаемый тип, который используется, когда {@code value == null}.
	 *                          Не должен быть {@code null} в таком случае.
	 * @param loadPredicate функция, которая проверяет, что операция является загрузкой
	 *                      той же самой переменной/поля
	 */
	protected AssignOperation(
			MethodContext context, @Nullable Operation value, @Nullable Type defaultReturnType,
			Predicate<Operation> loadPredicate
	) {
		this.value = value;

		if (value instanceof BinaryOperator binary &&
			binary.getOperatorType().isBinary() &&
			loadPredicate.test(binary.getOperand1())) {

			this.shortValue = binary.getOperand2();
			this.operatorType = binary.getOperatorType();

		} else {
			this.shortValue = value;
		}

		if (value == null) {
			this.returnType = Objects.requireNonNull(defaultReturnType);

		} else if (context.popIfSame(value)) {
			this.returnType = value.getReturnType();

			if (isIncOrDec()) {
				operatorType = operatorType.toPreIncOrDec();
			}

		} else if (isIncOrDec() && context.popIf(loadPredicate) != null) {
			this.returnType = value.getReturnType();
			operatorType = operatorType.toPostIncOrDec();

		} else {
			this.returnType = PrimitiveType.VOID;
		}
	}

	private boolean isIncOrDec() {
		return (operatorType == OperatorType.ADD || operatorType == OperatorType.SUB) &&
				shortValue instanceof LdcOperation ldc && ldc.getValue().valueEquals(1);
	}

	@Override
	public final Type getReturnType() {
		return returnType;
	}

	@Override
	@MustBeInvokedByOverriders
	public void inferType(Type requiredType) {
		if (shortValue != null) {
			shortValue.allowImplicitCast();
		}
	}

	@Override
	@MustBeInvokedByOverriders
	public void addImports(ClassContext context) {
		context.addImportsFor(shortValue);
	}

	@Override
	public Priority getPriority() {
		if (shortValue == null) {
			return Priority.DEFAULT;
		}

		if (operatorType != null && operatorType.isIncOrDec()) {
			return operatorType.isPost() ? Priority.POST_INC_DEC : Priority.PRE_INC_DEC;
		}

		return Priority.ASSIGNMENT;
	}

	/** Записывает операцию присвоения через сокращённый оператор, если он есть,
	 * иначе записывает её как обычно */
	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (operatorType != null && operatorType.isIncOrDec()) {
			if (operatorType.isPost()) {
				writeTarget(out, context);
				out.record(operatorType.getValue());

			} else {
				out.record(operatorType.getValue());
				writeTarget(out, context);
			}

			return;
		}

		writeTarget(out, context);
		out.space().record(operatorType != null ? operatorType.getValue() : "").record('=').space();
		writeShortValue(out, context);
	}


	/** Записывает операцию, в которую сохраняется значение. */
	protected abstract void writeTarget(DecompilationWriter out, MethodWriteContext context);

	/** Записывает само значение. */
	protected void writeShortValue(DecompilationWriter out, MethodWriteContext context) {
		out.record(requireShortValue(), context, getPriority());
	}
}
