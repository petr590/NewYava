package x590.newyava.decompilation.operation;

import com.google.common.collect.Lists;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.ReadonlyCode;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OperationUtil {
	private OperationUtil() {}

	/**
	 * Читает аргументы со стека в обратном порядке (так как на стек они кладутся в прямом порядке).
	 * @param argTypes требуемые типы аргументов.
	 * @return список аргументов в обычном порядке.
	 */
	public static List<Operation> readArgs(MethodContext context, List<Type> argTypes) {
		List<Operation> args = new ArrayList<>(argTypes.size());

		for (Type argType : Lists.reverse(argTypes)) {
			args.add(context.popAs(argType));
		}

		Collections.reverse(args);
		return args;
	}

	/**
	 * Выражения вида {@code int[]::new} при компиляции разворачиваются в {@code length -> new int[length]}.
	 * Метод распознаёт такие выражения и записывает соответствующий конструктор.
	 * @return {@code true}, если выражение найдено и записано, иначе {@code false}
	 */
	public static boolean writeArrayLambda(DecompilationWriter out, Context context, MethodDescriptor descriptor, ReadonlyCode code) {
		var operations = code.getMethodScope().getOperations();

		if (operations.size() != 1 ||
			descriptor.arguments().size() != 1 ||
			!descriptor.arguments().get(0).equals(PrimitiveType.INT)) {

			return false;
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

			out.record(newArray.getReturnType(), context).record("::new");
			return true;
		}

		return false;
	}
}
