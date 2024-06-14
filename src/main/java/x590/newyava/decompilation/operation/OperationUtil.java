package x590.newyava.decompilation.operation;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.Modifiers;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.ReadonlyCode;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.decompilation.operation.invoke.InvokeStaticOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class OperationUtil {
	private OperationUtil() {}

	/**
	 * Читает аргументы со стека в обратном порядке (так как на стек они кладутся в прямом порядке).
	 * @param argTypes требуемые типы аргументов.
	 * @return список аргументов в обычном порядке.
	 */
	public static List<Operation> readArgs(MethodContext context, @Unmodifiable List<Type> argTypes) {
		List<Operation> args = new ArrayList<>(argTypes.size());

		for (Type argType : Lists.reverse(argTypes)) {
			args.add(context.popAs(argType));
		}

		Collections.reverse(args);
		return args;
	}

	/**
	 * Выводит типы аргументов в соответствии с переданным списком типов.
	 * @throws IllegalArgumentException если размеры переданных списков не совпадают
	 */
	public static void inferArgTypes(@Unmodifiable List<Operation> arguments, @Unmodifiable List<Type> argTypes) {
		int s = arguments.size();

		if (s != argTypes.size()) {
			throw new IllegalArgumentException(String.format(
					"arguments.size() != argTypes.size(): %d != %d",
					s, argTypes.size()
			));
		}

		for (int i = 0; i < s; i++) {
			arguments.get(i).inferType(argTypes.get(i));
		}
	}

	/**
	 * Выражения вида {@code int[]::new} при компиляции разворачиваются в {@code length -> new int[length]}.
	 * Этот метод распознаёт такие выражения и возвращает соответствующий дескриптор.
	 * @return найденный дескриптор или {@code null}
	 */
	public static MethodDescriptor recognizeArrayLambda(MethodDescriptor descriptor, ReadonlyCode code) {
		var operations = code.getMethodScope().getOperations();

		if (operations.size() != 1 ||
			descriptor.arguments().size() != 1 ||
			!descriptor.arguments().get(0).equals(PrimitiveType.INT)) {

			return null;
		}

		var operation = operations.get(0);
		var variables = code.getMethodScope().getVariables();

		if (operation instanceof ReturnValueOperation ret &&
			ret.getValue() instanceof NewArrayOperation newArray &&
			!newArray.hasInitializer() &&
			newArray.getSizes().size() == 1 &&
			newArray.getSizes().get(0) instanceof LoadOperation load &&
			variables.size() == 1 &&
			load.getVarRef().getVariable().equals(variables.get(0))) {

			return new MethodDescriptor(newArray.getReturnType(), MethodDescriptor.INIT, PrimitiveType.VOID);
		}

		return null;
	}

	private static boolean isThisRef(@Nullable Operation operation) {
		return operation != null && operation.isThisRef();
	}

	private static final Predicate<String> IS_SYNTHETIC_THIS = Pattern.compile("this\\$\\d+").asMatchPredicate();

	/**
	 * Проверяет, что операция является инициализацией синтетического поля,
	 * которое ссылается на экземпляр внешнего класса.
	 * @return {@code true}, если да, иначе {@code false}
	 */
	public static boolean checkOuterInstanceInit(Operation operation, MethodContext context) {
		if (!(operation instanceof FieldOperation fieldOp) || !isThisRef(fieldOp.getInstance())) return false;
		if (!(fieldOp.getValue() instanceof LoadOperation loadOp) || loadOp.getSlotId() != 1) return false;

		var descriptor = fieldOp.getDescriptor();

		if (descriptor.type().equals(context.getThisType().getOuter()) &&
			IS_SYNTHETIC_THIS.test(descriptor.name())) {

			var foundField = context.findField(descriptor);

			if (foundField.isPresent() && (foundField.get().getModifiers() & Modifiers.ACC_SYNTHETIC) != 0) {
				foundField.get().makeOuterInstance();
				return true;
			}
		}

		return false;
	}


	private static final MethodDescriptor STRING_VALUE_OF =
			new MethodDescriptor(ClassType.STRING, "valueOf", ClassType.STRING, List.of(ClassType.OBJECT));

	/** Если операция является вызовом метода {@link String#valueOf(Object)},
	 * то возвращает его аргумент, иначе возвращает саму операцию */
	public static Operation unwrapStringValueOfObject(Operation operation) {
		if (operation instanceof InvokeStaticOperation invokeStatic &&
			invokeStatic.getDescriptor().equals(STRING_VALUE_OF)) {

			return invokeStatic.getArguments().get(0);
		}

		return operation;
	}
}
