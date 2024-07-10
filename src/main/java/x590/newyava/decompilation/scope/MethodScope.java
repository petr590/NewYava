package x590.newyava.decompilation.scope;


import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableSlotView;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.decompilation.operation.FieldOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtil;
import x590.newyava.decompilation.operation.invokedynamic.RecordInvokedynamicOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.decompilation.operation.terminal.ReturnVoidOperation;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.exception.DecompilationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MethodScope extends Scope {

	private final MethodContext methodContext;

	/** Индекс, с которого начинаются аргументы видимого дескриптора */
	@Getter
	private int argsStart;

	/** Индекс, на котором заканчиваются аргументы видимого дескриптора (не включительно) */
	@Getter
	private int argsEnd;

	public MethodScope(@Unmodifiable List<Chunk> chunks, MethodContext methodContext) {
		super(chunks);
		this.methodContext = methodContext;
		this.argsEnd = methodContext.getDescriptor().arguments().size();
	}

	@Override
	public void postDecompilation(MethodContext context) {
		super.postDecompilation(context);

		var operations = this.operations;

		int last = operations.size() - 1;

		if (last >= 0 && operations.get(last) == ReturnVoidOperation.INSTANCE) {
			operations.remove(last);
		}

		if (context.isConstructor()) {
			if (context.getThisType().isNested()) {
				// Инициализация this внешнего класса, всегда идёт до вызова суперконструктора
				if (!operations.isEmpty() &&
					OperationUtil.checkOuterInstanceInit(operations.get(0), context)) {

					operations.remove(0);
					argsStart += 1;
				}

				// Инициализация переменных из внешнего метода
				if (context.getThisType().isEnclosedInMethod()) {
					while (!operations.isEmpty() && OperationUtil.checkOuterVarInit(operations.get(0), context)) {
						operations.remove(0);
						argsEnd -= 1;
					}
				}
			}


			// Дефолтный супер-конструктор
			if (!operations.isEmpty() && operations.get(0).isDefaultConstructor(context)) {
				operations.remove(0);
			}

			// Поля record-ов инициализируются в конце конструктора
			if ((context.getClassModifiers() & Modifiers.ACC_RECORD) != 0) {
				int i = operations.size() - 1;

				while (i >= 0 && OperationUtil.isDefaultFieldInitializer(operations.get(i))) {
					operations.remove(i);
					i--;
				}
			}

		} else if (context.isStaticInitializer() && context.getThisType().isAnonymous()) {
			// Инициализация карты для switch(enum)
			int i = 0;

			for (int s = operations.size(); i < s; i += 2) {
				if (!OperationUtil.tryInitEnumMap(context, operations.get(i), operations.get(i + 1))) {
					break;
				}
			}

			operations.subList(0, i).clear();
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


		Int2ObjectMap<List<VarOwner>> hostsMap = new Int2ObjectArrayMap<>();

		for (int slotsCount = slots.size(); slotId < slotsCount; slotId++) {
			hostsMap.put(slotId, new ArrayList<>());
		}

		findVarsHosts(hostsMap);

		for (var entry : hostsMap.int2ObjectEntrySet()) {
			int varSlotId = entry.getIntKey();
			List<VarOwner> hosts = entry.getValue();

			for (VariableReference ref : slots.get(varSlotId).getVarRefs()) {
				var foundHost = hosts.stream().filter(host ->
								host.getStart() >= ref.getStart() && host.getEnd() <= ref.getEnd() ||
								host.getStart() <= ref.getStart() && host.getEnd() >= ref.getEnd()
						).findFirst();

				foundHost.ifPresentOrElse(
						host -> {
							ref.initVariable(host.getOrCreateVariable(ref));
							host.getScope().addVariable(ref.getVariable());
							host.getScope().variablesToDeclare.add(ref.getVariable());
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
				operations.get(0) instanceof ReturnValueOperation returnOperation &&
				predicate.test(returnOperation.getValue());
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
				operation instanceof FieldOperation fieldOperation &&
				fieldOperation.isGetter() &&
				fieldOperation.getDescriptor().equals(fieldDescriptor) &&
				OperationUtil.isThisRef(fieldOperation.getInstance())
		);
	}

	@Override
	public String toString() {
		return String.format("%s(%s, %d - %d)", getClass().getSimpleName(),
				methodContext.getDescriptor(), getStartChunk().getId(), getEndChunk().getId());
	}
}
