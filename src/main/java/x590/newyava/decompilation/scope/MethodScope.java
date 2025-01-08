package x590.newyava.decompilation.scope;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.DecompilingField;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.other.FieldOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.invokedynamic.RecordInvokedynamicOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ReturnVoidOperation;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableSlotView;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.modifiers.Modifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
public class MethodScope extends Scope {

	private final MethodContext methodContext;

	/** Индекс, с которого начинаются аргументы видимого дескриптора */
	private int argsStart;

	/** Индекс, на котором заканчиваются аргументы видимого дескриптора (не включительно) */
	private int argsEnd;

	private boolean hasOuterInstance;

	/** Ключ - индекс переменной дескриптора, значение - поле */
	private @Nullable Int2ObjectMap<DecompilingField> outerVarTable;

	public MethodScope(@Unmodifiable List<Chunk> chunks, MethodContext methodContext) {
		super(chunks);
		this.methodContext = methodContext;
		this.argsStart = 0;
		this.argsEnd = methodContext.getDescriptor().arguments().size();
	}

	public boolean hasOuterInstance() {
		return hasOuterInstance;
	}

	@Override
	public boolean canShrink() {
		return false;
	}

	@Override
	public void postDecompilation(MethodContext context) {
		super.postDecompilation(context);

		removeLastOperationIf(operation -> operation == ReturnVoidOperation.INSTANCE);

		var operations = this.operations;

		if (context.isConstructor()) {
			if (context.getThisType().isNested()) {
				// Инициализация this внешнего класса, всегда идёт до вызова суперконструктора
				if (!operations.isEmpty() &&
					OperationUtils.tryMarkOuterInstance(operations.get(0), context)) {

					operations.remove(0);
					argsStart += 1;
					hasOuterInstance = true;
				}

				// Инициализация переменных из внешнего метода
				if (context.getThisType().isEnclosedInMethod()) {
					outerVarTable = new Int2ObjectArrayMap<>();

					int i = 0;

					for (int s = operations.size(); i < s; i++) {
						if (!OperationUtils.checkOuterVarInit(operations.get(i), context, outerVarTable)) {
							break;
						}
					}

					operations.subList(0, i).clear();
					argsEnd -= i;

					assert argsStart <= argsEnd : argsStart + " > " + argsEnd;
				}
			}

			// Дефолтный супер-конструктор
			boolean defaultSuperConstructor = !operations.isEmpty() && operations.get(0).isDefaultConstructor(context);

			if (defaultSuperConstructor) {
				operations.remove(0);
			}

			initializeFields(context, defaultSuperConstructor ? 0 : 1);

			// Поля record-ов инициализируются в конце конструктора
			if ((context.getClassModifiers() & Modifiers.ACC_RECORD) != 0) {
				for (int i = operations.size() - 1; i >= 0; i--) {
					if (OperationUtils.isDefaultFieldInitializer(operations.get(i))) {
						operations.remove(i);
					} else {
						break;
					}
				}
			}

		} else if (context.isStaticInitializer()) {
			initializeFields(context, 0);

			if (context.getThisType().isAnonymous() &&
				operations.size() > 0 &&
				OperationUtils.tryCreateEnumMap(context, operations.get(0))) {

				// Инициализация карты для switch(enum)
				int i = 1; // Первая операция - создание массива

				for (int s = operations.size(); i < s; i++) {
					OperationUtils.tryInitEnumMap(context, operations.get(i));
				}

				operations.subList(0, i).clear();
			}
		}
	}

	private void initializeFields(MethodContext context, int startIndex) {
		// Инициализация полей
		for (int i = startIndex, s = operations.size(); i < s; i++) {
			if (!operations.get(i).initializeField(context)) {
				break;
			}
		}
	}


	@Override
	public void afterDecompilation(MethodContext context) {
		super.afterDecompilation(context);

		if (context.isConstructor() || context.isStaticInitializer()) {
			operations.removeIf(Operation::isFieldInitialized);
		}
	}

	/** Внешний scope лямбда-метода. Не то же самое, что {@link #getParent()} */
	private @Nullable MethodScope outerScope;

	private boolean cyclicReferenceChecking;


	public void setOuterScope(MethodScope newOuterScope) {
		if (outerScope != null && outerScope != newOuterScope) {
			throw new DecompilationException(
					"Outer scope already has been set (old: %s, new: %s)",
					outerScope, newOuterScope
			);
		}

		outerScope = newOuterScope;
	}

	/** Проверяет, что нет циклических ссылок между лямбда-методами.
	 * @throws DecompilationException при обнаружении циклической ссылки. */
	public void checkCyclicReference() {
		if (outerScope == null)
			return;

		synchronized (this) {
			if (cyclicReferenceChecking) {
				throw new DecompilationException("Detected cycling reference in lambda methods: " + this);
			}

			cyclicReferenceChecking = true;
			outerScope.checkCyclicReference();
			cyclicReferenceChecking = false;
		}
	}

	/** @return приоритет, в котором должен вызываться метод {@link #initVariables} */
	public int getVariablesInitPriority() {
		return outerScope == null ? 0 : outerScope.getVariablesInitPriority() - 1;
	}

	@Override
	protected Optional<Variable> findVarByName(String name) {
		return outerScope == null ?
				super.findVarByName(name) :
				super.findVarByName(name).or(() -> outerScope.findVarByName(name));
	}

	private boolean variablesInitialized;


	/**
	 * Связывает с каждым {@link VariableReference} определённый {@link Variable}.
	 * Заполняет {@link #variables} и {@link #variablesToDeclare} в этом и всех дочерних scope-ах.
	 * @param sizes список размеров всех переменных, объявленных в сигнатуре метода
	 *              (т.е. {@code this} и всех аргументов метода).
	 */
	public void initVariables(IntList sizes) {
		assert !variablesInitialized;
		variablesInitialized = true;

		List<? extends VariableSlotView> slots = getStartChunk().getVarSlots();

		int slotId = 0;

		for (int size : sizes) {
			var refs = slots.get(slotId).getVarRefs();

			assert refs.size() == 1 : refs;

			var ref = refs.get(0);

			// Внешние ссылки на переменную лямбда-функции уже инициализированы в родительском методе.
			if (ref.getVariable() == null) {
				ref.initVariable(new Variable(ref, true));
			}

			addVariable(ref.getVariable());
			slotId += size;

			if (size == 2)
				addVariable(null);
		}


		Int2ObjectMap<List<VarOwner>> ownersMap = new Int2ObjectArrayMap<>();

		for (int slotsCount = slots.size(); slotId < slotsCount; slotId++) {
			ownersMap.put(slotId, new ArrayList<>());
		}

		findVarsOwners(ownersMap);

//		System.out.println(ownersMap.get(2)); // DEBUG

		for (var entry : ownersMap.int2ObjectEntrySet()) {
			int varSlotId = entry.getIntKey();
			List<VarOwner> owners = entry.getValue();

//			System.out.printf("%d %s\n", varSlotId, slots.get(varSlotId).getVarRefs()); // DEBUG

			for (VariableReference ref : slots.get(varSlotId).getVarRefs()) {
				var foundOwner = owners.stream().filter(owner ->
								owner.getStart() >= ref.getStart() && owner.getEnd() <= (ref.getEnd() - 1) ||
								owner.getStart() <= ref.getStart() && owner.getEnd() >= (ref.getEnd() - 1)
						).findFirst();

				foundOwner.ifPresentOrElse(
						owner -> {
							ref.initVariable(owner.getOrCreateVariable(ref));
							owner.getScope().addVariable(ref.getVariable());
							owner.getScope().variablesToDeclare.add(ref.getVariable());
//							System.out.printf("%s, %s\n", owner.getScope(), ref);
						},
						() -> {
							ref.initVariable(new Variable(ref, false));
							addVariable(ref.getVariable());
						}
				);
			}

			addNullVariableIfLess(varSlotId + 1);
		}
	}


	/** @return {@code true}, если этот метод содержит только {@link ReturnValueOperation},
	 * которая возвращает операцию, и для этой операции {@code predicate} возвращает {@code true}. */
	private boolean returnsSingleOperation(Predicate<Operation> predicate) {
		return  operations.size() == 1 &&
				operations.get(0) instanceof ReturnValueOperation returnOp &&
				predicate.test(returnOp.getValue());
	}

	/**
	 * @return {@code true}, если метод возвращает {@link RecordInvokedynamicOperation}.
	 */
	public boolean isRecordInvokedynamic() {
		return returnsSingleOperation(operation -> operation instanceof RecordInvokedynamicOperation);
	}

	/**
	 * @return {@code true}, если метод является геттером поля с соответствующим дескриптором.
	 */
	public boolean isGetterOf(FieldDescriptor fieldDescriptor) {
		return returnsSingleOperation(operation ->
				operation instanceof FieldOperation fieldOp &&
				fieldOp.isGetter() &&
				fieldOp.getDescriptor().baseEquals(fieldDescriptor) &&
				fieldOp.isThisField()
		);
	}

	@Override
	public String toString() {
		return String.format("%s(%s, %d - %d)", getClass().getSimpleName(),
				methodContext.getDescriptor(), getStartChunk().getId(), getEndChunk().getId());
	}
}
