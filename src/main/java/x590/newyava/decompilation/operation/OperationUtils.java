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
import x590.newyava.decompilation.operation.other.*;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ThrowOperation;
import x590.newyava.decompilation.operation.variable.CatchOperation;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.decompilation.scope.*;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.*;
import x590.newyava.util.Utils;

import java.util.*;
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
		int size = arguments.size();

		if (size != argTypes.size()) {
			throw new IllegalArgumentException(String.format(
					"arguments.size() != argTypes.size(): %d != %d",
					size, argTypes.size()
			));
		}

		for (int i = 0; i < size; i++) {
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
			descriptor.arguments().get(0) != PrimitiveType.INT) {
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


	private static final Pattern
			SYNTHETIC_THIS_PATTERN = Pattern.compile("this\\$\\d+"),
			SYNTHETIC_VAR_PATTERN = Pattern.compile("val\\$.*");

	/**
	 * Проверяет, что операция является инициализацией синтетического поля, которое ссылается на
	 * экземпляр внешнего класса. Если это так, то помечает это поле как внешний экземпляр {@code this}.
	 * @return {@code true} в случае успеха, иначе {@code false}.
	 */
	public static boolean tryMarkOuterInstance(Operation operation, MethodContext context) {
		if (!(operation instanceof FieldOperation fieldOp) || !fieldOp.isThisField()) return false;
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
	public static boolean checkOuterVarInit(
			Operation operation, MethodContext context, Int2ObjectMap<DecompilingField> outerVarTable
	) {
		if (!(operation instanceof FieldOperation fieldOp) || !fieldOp.isThisField()) return false;
		if (!(fieldOp.getValue() instanceof ILoadOperation load)) return false;

		var descriptor = fieldOp.getDescriptor();

		if (SYNTHETIC_VAR_PATTERN.matcher(descriptor.name()).matches()) {
			var foundField = context.findField(descriptor);

			if (foundField.isPresent() && foundField.get().isSynthetic()) {
				outerVarTable.put(context.getDescriptor().indexBySlot(load.getSlotId() - 1), foundField.get());
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
				fieldOp.isThisField() &&
				fieldOp.getValue() instanceof ILoadOperation;
	}


	private static final String SWITCH_MAP_PREFIX = "$SwitchMap$";

	private static boolean isSwitchMapField(Context context, FieldOperation fieldOp) {
		return fieldOp.isStatic() &&
				fieldOp.getDescriptor().name().startsWith(SWITCH_MAP_PREFIX) &&
				fieldOp.getDescriptor().hostClass().equals(context.getThisType());
	}

	private static final Template ENUM_MAP_TEMPLATE =
			Template.staticSetter(
					Template.newArray(PrimitiveType.INT, Template.arrayLength(Template.invokeStatic(
							descriptor -> descriptor.name().equals("values") && descriptor.arguments().isEmpty()
					)))
			).make();

	/**
	 * Если операция является инициализацией enumMap, то создаёт её в соответствующем поле.
	 * @return {@code true}, если enumMap инициализирован, иначе {@code false}.
	 */
	public static boolean tryCreateEnumMap(Context context, Operation operation) {
		return ENUM_MAP_TEMPLATE.test(context, operation);
	}

	/**
	 * Если операция является try-catch, причём try содержит инициализацию enumMap,
	 * то данный метод инициализирует её.
	 */
	public static void tryInitEnumMap(Context context, Operation operation) {
		if (operation instanceof JoiningTryCatchScope joiningScope &&
			joiningScope.getOperations().size() == 2 &&
			joiningScope.getOperations().get(0) instanceof TryScope tryScope &&
			joiningScope.getOperations().get(1) instanceof CatchScope catchScope &&
			catchScope.isEmpty() &&
			catchScope.getCatchOperation().getExceptionTypes().equals(List.of(ClassType.NO_SUCH_FIELD_ERROR))) {

			var operations = tryScope.getOperations();

			if (operations.size() == 1 &&
				operations.get(0) instanceof ArrayStoreOperation astoreOp &&

				astoreOp.getArray() instanceof FieldOperation array &&
				array.isGetter() && isSwitchMapField(context, array) &&

				astoreOp.getIndex() instanceof InvokeNonstaticOperation invokeOp &&
				invokeOp.getObject() instanceof FieldOperation enumConstant &&
				enumConstant.isGetter() && enumConstant.isStatic() &&

				invokeOp.getDescriptor().equals(enumConstant.getDescriptor().hostClass(), "ordinal", PrimitiveType.INT)
			) {

				var id = LdcOperation.getIntConstant(astoreOp.getValue());
				if (id == null) return;

				var foundField = context.findField(array.getDescriptor());
				if (foundField.isEmpty() || !foundField.get().isSynthetic()) return;

				foundField.get().setEnumEntry(id.getValue(), enumConstant.getDescriptor());
			}
		}
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

		for (int i = scopes.size() - 1; i >= 0; i--) {
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

	public static @Nullable ILoadOperation getStaticInstance(IClassArrayType hostClass, Operation prev) {
		if (hostClass.isAnonymous() &&
			prev instanceof PopOperation pop &&
			pop.getValue() instanceof ILoadOperation load) {

			return load;
		}

		return null;
	}

	private static final MethodDescriptor REQUIRE_NON_NULL_DESCRIPTOR = new MethodDescriptor(
			ClassType.valueOf(Objects.class), "requireNonNull", ClassType.OBJECT, List.of(ClassType.OBJECT)
	);

	/** @return {@code true}, если первая операция является проверкой второй операции на {@code null} */
	public static boolean isNullCheck(Operation operation, Operation nullable) {
		return  operation instanceof PopOperation pop &&
				pop.getValue() instanceof InvokeStaticOperation invokeStatic &&
				invokeStatic.getDescriptor().equals(REQUIRE_NON_NULL_DESCRIPTOR) &&
				Utils.isSingle(invokeStatic.getArguments(), arg -> arg.equals(nullable));
	}

	public static Operation castIfNull(Operation operation, ReferenceType type) {
		return operation != ConstNullOperation.INSTANCE ? operation :
				CastOperation.narrow(operation, Types.ANY_OBJECT_TYPE, type);
	}
}
