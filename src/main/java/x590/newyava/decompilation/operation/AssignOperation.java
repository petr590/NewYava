package x590.newyava.decompilation.operation;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
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

	/** Значение, которое идёт справа от знака равно
	 * (например, для {@code x = 5} оно равно {@code 5},
	 * также и для {@code x += 5} оно равно {@code 5}) */
	@Getter
	protected final @Nullable Operation value;

	/** Сокращённый оператор */
	protected @Nullable Operator operator;

	private final Type returnType;


	/**
	 * @param value значение, которое сохраняется в переменную/поле.
	 * @param defaultReturnType возвращаемый тип, который используется, когда {@code value == null}.
	 *                          Не должен быть {@code null} в таком случае.
	 * @param loadPredicate функция, которая проверяет, что операция является загрузкой
	 *                      той же самой переменной/поля
	 */
	protected AssignOperation(MethodContext context, @Nullable Operation value,
	                          @Nullable Type defaultReturnType, Predicate<Operation> loadPredicate) {

		if (value instanceof BinaryOperation binary &&
			binary.getOperator().isBinary() &&
			loadPredicate.test(binary.getOperand1())) {

			this.value = binary.getOperand2();
			this.operator = binary.getOperator();

		} else {
			this.value = value;
		}

		if (value == null) {
			this.returnType = Objects.requireNonNull(defaultReturnType);

		} else if (context.popIfSame(value)) {
			this.returnType = value.getReturnType();

			if (isIncOrDec()) {
				operator = operator.toPreIncOrDec();
			}

		} else {
			if (null != context.popIf(operation -> loadPredicate.test(operation) && isIncOrDec())) {
				this.returnType = value.getReturnType();
				operator = operator.toPostIncOrDec();
			} else {
				this.returnType = PrimitiveType.VOID;
			}
		}
	}

	private boolean isIncOrDec() {
		return (operator == Operator.ADD || operator == Operator.SUB) &&
				value instanceof LdcOperation ldc && ldc.getValue().valueEquals(1);
	}

	@Override
	public final Type getReturnType() {
		return returnType;
	}

	@Override
	@MustBeInvokedByOverriders
	public void inferType(Type requiredType) {
		if (value != null) {
			value.allowImplicitCast();
		}
	}

	@Override
	public Priority getPriority() {
		if (value == null) {
			return Priority.DEFAULT;
		}

		if (operator != null && operator.isIncOrDec()) {
			return operator.isPost() ? Priority.POST_INC_DEC : Priority.PRE_INC_DEC;
		}

		return Priority.ASSIGNMENT;
	}

	/** Записывает операцию присвоения через сокращённый оператор, если он есть,
	 * иначе записывает её как обычно */
	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (operator != null && operator.isIncOrDec()) {
			if (operator.isPost()) {
				writeTarget(out, context);
				out.record(operator.getValue());

			} else {
				out.record(operator.getValue());
				writeTarget(out, context);
			}

			return;
		}

		writeTarget(out, context);
		out.space().record(operator != null ? operator.getValue() : "").record('=').space();
		writeValue(out, context);
	}


	/** Записывает операцию, в которую сохраняется значение. */
	protected abstract void writeTarget(DecompilationWriter out, MethodWriteContext context);

	/** Записывает само значение. */
	protected void writeValue(DecompilationWriter out, MethodWriteContext context) {
		out.record(value, context, getPriority());
	}
}
