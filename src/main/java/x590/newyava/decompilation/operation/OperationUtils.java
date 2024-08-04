package x590.newyava.decompilation.operation;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.DecompilingField;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.code.Code;
import x590.newyava.decompilation.operation.array.ArrayLoadOperation;
import x590.newyava.decompilation.operation.array.ArrayStoreOperation;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.decompilation.operation.condition.GotoOperation;
import x590.newyava.decompilation.operation.invoke.InvokeNonstaticOperation;
import x590.newyava.decompilation.operation.invoke.InvokeSpecialOperation;
import x590.newyava.decompilation.operation.invoke.InvokeStaticOperation;
import x590.newyava.decompilation.operation.monitor.MonitorEnterOperation;
import x590.newyava.decompilation.operation.monitor.MonitorExitOperation;
import x590.newyava.decompilation.operation.other.FieldOperation;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ThrowOperation;
import x590.newyava.decompilation.operation.variable.CatchOperation;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.decompilation.scope.*;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

@UtilityClass
public final class OperationUtils {
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
	public static @Nullable MethodDescriptor recognizeArrayLambda(MethodDescriptor descriptor, Code code) {
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
			variables.size() == 1 &&
			Utils.isSingle(newArray.getSizes(), arraySizeOp ->
					arraySizeOp instanceof ILoadOperation load &&
					load.getVarRef().requireVariable().equals(variables.get(0)))) {

			return MethodDescriptor.constructor(newArray.getReturnType());
		}

		return null;
	}


	/**
	 * Лямбды для супер-методов разворачиваются в вызов, супер-метода.
	 * Данный метод распознаёт такие выражения и возвращает соответствующий дескриптор.
	 * @return найденный дескриптор и новый аргумент инструкции invokedynamic или {@code null}
	 */
	public static @Nullable Pair<MethodDescriptor, Operation> recognizeFunctionLambda(
			MethodDescriptor descriptor, Code code, @Unmodifiable List<Operation> indyArgs
	) {
		var operations = code.getMethodScope().getOperations();

		if (operations.size() != 1 || indyArgs.size() != 1)
			return null;

		var variables = code.getMethodScope().getVariables();

		if (variables.isEmpty() || variables.size() != descriptor.arguments().size() + 1)
			return null;

		var operation = operations.get(0);
		var iterator = variables.iterator();

		if (operation instanceof ReturnValueOperation ret &&
			ret.getValue() instanceof InvokeSpecialOperation invoke &&
			invoke.getObject().isThisRef() &&
			isLoadOf(iterator, invoke.getObject()) &&
			invoke.getArguments().stream().allMatch(arg -> isLoadOf(iterator, arg))) {

			return Pair.of(invoke.getDescriptor(), invoke.getObject());
		}

		return null;
	}

	private static boolean isLoadOf(Iterator<Variable> varsIter, Operation operation) {
		return varsIter.hasNext() &&
				operation instanceof ILoadOperation load &&
				load.getVarRef().getVariable() == varsIter.next();
	}


	/**
	 * @return {@code true}, если операция не {@code null} и является ссылкой на {@code this}
	 */
	public static boolean isThisRef(@Nullable Operation operation) {
		return operation != null && operation.isThisRef();
	}

	private static final Pattern
			SYNTHETIC_THIS_PATTERN = Pattern.compile("this\\$\\d+"),
			SYNTHETIC_VAR_PATTERN = Pattern.compile("val\\$(.*)");

	/**
	 * Проверяет, что операция является инициализацией синтетического поля, которое ссылается на
	 * экземпляр внешнего класса. Если это так, то помечает это поле как внешний экземпляр {@code this}.
	 * @return {@code true} в случае успеха, иначе {@code false}.
	 */
	public static boolean tryMarkOuterInstance(Operation operation, MethodContext context) {
		if (!(operation instanceof FieldOperation fieldOp) || !isThisRef(fieldOp.getInstance())) return false;
		if (!(fieldOp.getValue() instanceof ILoadOperation loadOp) || loadOp.getSlotId() != 1) return false;

		var descriptor = fieldOp.getDescriptor();

		if (descriptor.type().equals(context.getThisType().getOuter()) &&
			SYNTHETIC_THIS_PATTERN.matcher(descriptor.name()).matches()) {

			var foundField = context.findField(descriptor);

			if (foundField.isPresent() && foundField.get().isSynthetic()) {
				foundField.get().makeOuterInstance();
				return true;
			}
		}

		return false;
	}


	/**
	 * Проверяет, что операция является инициализацией синтетического поля, которое ссылается на
	 * внешнюю переменную. Если это так, то помечает это поле как экземпляр внешней переменной.
	 * @return {@code true} в случае успеха, иначе {@code false}.
	 */
	public static boolean checkOuterVarInit(Operation operation, MethodContext context) {
		if (!(operation instanceof FieldOperation fieldOp) || !isThisRef(fieldOp.getInstance())) return false;
		if (!(fieldOp.getValue() instanceof ILoadOperation)) return false;

		var descriptor = fieldOp.getDescriptor();

		var matcher = SYNTHETIC_VAR_PATTERN.matcher(descriptor.name());

		if (matcher.matches()) {
			var foundField = context.findField(descriptor);

			String varName = matcher.group(1);

			if (foundField.isPresent() && foundField.get().isSynthetic()) {
				foundField.get().makeOuterVariable(varName);
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

	/** @return {@code true}, если операция является инициализацией поля из переменной. */
	public static boolean isDefaultFieldInitializer(Operation operation) {
		return operation instanceof FieldOperation fieldOp &&
				fieldOp.isSetter() &&
				isThisRef(fieldOp.getInstance()) &&
				fieldOp.getValue() instanceof ILoadOperation;
	}

	/** Добавляет операцию в начало списка операций и возвращает новый список.
	 * Не изменяет переданный список. */
	public static @Unmodifiable List<? extends Operation> addBefore(
			Operation object, @Unmodifiable List<? extends Operation> operations
	) {
		List<Operation> result = new ArrayList<>(operations.size() + 1);
		result.add(object);
		result.addAll(operations);
		return Collections.unmodifiableList(result);
	}


	private static final String SWITCH_MAP_PREFIX = "$SwitchMap$";

	/**
	 * Если операции являются try-catch, причём try содержит инициализацию enumMap,
	 * то данный метод инициализирует её.
	 * @return {@code true}, если enumMap инициализирован, иначе {@code false}.
	 */
	public static boolean tryInitEnumMap(Context context, Operation operation1, Operation operation2) {
		if (operation1 instanceof TryScope tryScope &&
			operation2 instanceof CatchScope catchScope &&
			catchScope.isEmpty() &&
			catchScope.getCatchOperation().getExceptionTypes().equals(List.of(ClassType.NO_SUCH_FIELD_ERROR))) {

			var operations = tryScope.getOperations();

			if (operations.size() == 1 &&
				operations.get(0) instanceof ArrayStoreOperation astoreOp &&

				astoreOp.getArray() instanceof FieldOperation array &&
				array.isGetter() && array.isStatic() &&
				array.getDescriptor().name().startsWith(SWITCH_MAP_PREFIX) &&

				astoreOp.getIndex() instanceof InvokeNonstaticOperation invokeOp &&
				invokeOp.getObject() instanceof FieldOperation enumConstant &&
				enumConstant.isGetter() && enumConstant.isStatic() &&

				invokeOp.getDescriptor().equals(enumConstant.getDescriptor().hostClass(), "ordinal", PrimitiveType.INT) &&
				array.getDescriptor().hostClass().equals(context.getThisType())) {

				var id = LdcOperation.getIntConstant(astoreOp.getValue());
				if (id == null) return false;

				var foundField = context.findField(array.getDescriptor());
				if (foundField.isEmpty() || !foundField.get().isSynthetic()) return false;

				return foundField.get().setEnumEntry(id.getValue(), enumConstant.getDescriptor());
			}
		}

		return false;
	}


	/**
	 * Если переданная операция является обращением к enumMap, то возвращает
	 * операцию самого enum-объекта и enumMap.
	 * @return найденные значения или {@code null}, если ничего не найдено.
	 */
	public static @Nullable Pair<Operation, Int2ObjectMap<FieldDescriptor>> getEnumMap(
			Context context, Operation operation
	) {
		if (operation instanceof ArrayLoadOperation aloadOp &&

			aloadOp.getArray() instanceof FieldOperation enumMapGetter &&
			enumMapGetter.isGetter() && enumMapGetter.isStatic() &&

			aloadOp.getIndex() instanceof InvokeNonstaticOperation invokeOp &&
			invokeOp.getDescriptor().equalsIgnoreClass("ordinal", PrimitiveType.INT)) {

			var descriptor = enumMapGetter.getDescriptor();

			var enumMap = context.findClass(descriptor.hostClass())
					.flatMap(clazz -> clazz.findField(descriptor))
					.map(DecompilingField::getEnumMap).orElse(null);

			return enumMap == null ? null : Pair.of(invokeOp.getObject(), enumMap);
		}

		return null;
	}

	/** Удаляет последнюю операцию в scope, если она является
	 * оператором {@code continue} для указанного цикла. */
	public void removeLastContinueOfLoop(Scope scope, LoopScope loop) {
		boolean removed = scope.removeLastOperationIf(
				operation -> operation instanceof GotoOperation gotoOp && gotoOp.getRole().isContinueOf(loop)
		);

		if (removed) return;

		var scopes = scope.getScopes();

		for (int i = scopes.size() - 1; i >= 0; --i) {
			if (!scopes.get(i).removeLastContinueOfLoop(loop)) {
				break;
			}
		}
	}


	/**
	 * Распознаёт try-finally, которые фактически являются блоком synchronized.
	 * @return SynchronizedScope, если блок synchronized найден, иначе {@code null}.
	 */
	public static @Nullable SynchronizedScope getSynchronizedScope(
			IntIntPair tryBlock,
			Int2ObjectMap<CatchOperation> catchMap,
	        @Unmodifiable List<Chunk> chunks
	) {
		int tryStart = tryBlock.firstInt();
		if (tryStart == 0 || catchMap.size() != 1)
			return null;

		var operations = chunks.get(tryStart - 1).getOperations();
		if (!(Utils.getLastOrNull(operations) instanceof MonitorEnterOperation monitorEnter))
			return null;

		var catchEntry = catchMap.int2ObjectEntrySet().iterator().next();
		var catchOp = catchEntry.getValue();
		int catchStart = catchEntry.getIntKey();

		if (!catchOp.isFinally() || catchStart + 2 != catchOp.getEndId()) return null;

		var catchOperations1 = chunks.get(catchStart).getOperations();
		var catchOperations2 = chunks.get(catchStart + 1).getOperations();

		if (Utils.isSingle(catchOperations1, operation -> operation instanceof MonitorExitOperation) &&
			Utils.isSingle(catchOperations2, operation -> operation instanceof ThrowOperation)) {

			Utils.removeLast(operations);
			catchOperations1.clear();
			catchOperations2.clear();

			return new SynchronizedScope(chunks.subList(tryStart, catchStart), monitorEnter.getValue());
		}

		return null;
	}
}
