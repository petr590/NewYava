package x590.newyava.decompilation.code;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.objectweb.asm.Label;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.instruction.FlowControlInsn;
import x590.newyava.decompilation.instruction.Instruction;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.other.ProxyOperation;
import x590.newyava.decompilation.operation.condition.Condition;
import x590.newyava.decompilation.operation.condition.JumpOperation;
import x590.newyava.decompilation.operation.condition.Role;
import x590.newyava.decompilation.operation.condition.SwitchOperation;
import x590.newyava.decompilation.operation.variable.CatchOperation;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableSlotView;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.PrimitiveType;
import x590.newyava.util.Utils;

import java.util.*;

/**
 * Чанк - это блок непрерывно выполняющихся инструкций с возможной инструкцией перехода в конце.
 * Выполнение чанка всегда должно начинаться с первой инструкции, т.е. если есть переход
 * в середину чанка, то этот чанк должен быть разделён на два в точке перехода.
 * Осуществляет доступ к локальным переменным, списку операций и к чанкам, на которые переходит.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Chunk implements Comparable<Chunk> {

	/** Индекс инструкции, на которой начинается чанк */
	@Getter
	private final int startIndex;

	/** Индекс инструкции, следующей сразу за чанком */
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private int endIndex;

	/** Порядковый id чанка, начинается с 0 */
	@Getter
	private final int id;

	@Getter
	private final @UnmodifiableView List<? extends VariableSlotView> varSlots;

	private final List<Instruction> instructions = new ArrayList<>();

	/** Инструкция контроля потока в конце чанка или {@code null}, если такой инструкции нет */
	private @Nullable FlowControlInsn flowControlInsn;

	/** Список void-операций в чанке */
	@Getter
	private final List<Operation> operations = new ArrayList<>();


	/** Операции, которые принимает данный чанк. */
	private @Nullable List<ProxyOperation> poppedOperations;

	
	/** Операции, оставшиеся на стеке после декомпиляции чанка. */
	@Getter
	private Deque<Operation> pushedOperations;


	/** Операция условного/безусловного перехода в конце чанка или {@code null}, если такой операции нет.
	 * Примечание: {@code return} и {@code throw} не являются операциями перехода. */
	private @Nullable JumpOperation jumpOperation;

	/** Операция {@code switch} в конце чанка или {@code null}, если такой операции нет.
	 * Должна быть {@code null}, если {@link #jumpOperation} не является {@code null} */
	@Getter
	private @Nullable SwitchOperation switchOperation;

	/** Следующий чанк. Если следующего чанка нет или переход на него
	 * невозможен (из-за инструкций {@code goto/return/throw}), то поле равно {@code null} */
	@Getter
	@Setter(AccessLevel.PACKAGE)
	private @Nullable Chunk directChunk;

	/* ------------------------------------------------- Conditions ------------------------------------------------- */

	/** Условие перехода на {@link #conditionalChunk}.
	 * Должен быть {@code null} или не {@code null}, когда {@link #jumpOperation}
	 * имеет значение {@code null} или не {@code null} соответственно.
	 * Инициализируется в методе {@link #linkChunks}. */
	@Getter
	private @Nullable Condition condition;

	/** Чанк, на который выполнится переход при условии (даже если оно всегда {@code true}).
	 * Должен быть {@code null} или не {@code null}, когда {@link #jumpOperation}
	 * имеет значение {@code null} или не {@code null} соответственно.
	 * Инициализируется в методе {@link #linkChunks}. */
	@Getter
	private @Nullable Chunk conditionalChunk;


	/** @throws NullPointerException если {@link #condition} равен {@code null} */
	public Condition requireCondition() {
		return Objects.requireNonNull(condition);
	}

	/** @throws NullPointerException если {@link #conditionalChunk} равен {@code null} */
	public Chunk requireConditionalChunk() {
		return Objects.requireNonNull(conditionalChunk);
	}

	/**
	 * @throws IllegalStateException если поле {@link #condition} не установлено
	 * @throws NullPointerException если параметр равен {@code null}
	 */
	public void changeCondition(Condition condition) {
		if (this.condition == null) {
			throw new IllegalStateException("Cannot change condition cause it's not set");
		}

		this.condition = Objects.requireNonNull(condition);
	}


	/* ---------------------------------------------------- Main ---------------------------------------------------- */

	public VariableReference getVarRef(int slotId) {
		return varSlots.get(slotId).getOrCreate(startIndex, endIndex);
	}

	public void addInstruction(Instruction instruction) {
		if (flowControlInsn != null) {
			throw new IllegalStateException("Instruction " + instruction + " is after flow control instruction");
		}

		if (instruction instanceof FlowControlInsn flowControl) {
			flowControlInsn = flowControl;
		} else {
			instructions.add(instruction);
		}
	}

	/**
	 * Преобразует инструкции в операции, включая инструкцию перехода.
	 * @param catchMaps список карт, где ключ - id чанка, на котором начинается блок {@code catch},
	 *                  значение - операция исключения, которое ловит этот блок.
	 */
	void decompile(MethodContext methodContext, Collection<Int2ObjectMap<CatchOperation>> catchMaps) {
		methodContext.setCurrentChunk(this);

		var stack = methodContext.getStack();

		catchMaps.stream().map(map -> map.get(id))
				.filter(Objects::nonNull).findAny()
				.ifPresent(stack::push);


		for (int i = 0, s = instructions.size(); i < s; i++) {
			Operation operation = null;

			var instruction = instructions.get(i);

			if (i < s - 1) { // Пытаемся объединить две инструкции
				operation = instruction.toOperation(methodContext, instructions.get(i + 1));
			}

			if (operation != null) {
				i++; // Пропускаем две инструкции, а не одну
			} else {
				operation = instruction.toOperation(methodContext);
			}

			if (operation != null) {
				if (operation.getReturnType() != PrimitiveType.VOID) {
					stack.push(operation);
				} else {
					operations.add(operation);
				}
			}
		}

		if (flowControlInsn != null) {
			var operation = flowControlInsn.toOperation(methodContext);
			assert operation.getReturnType() == PrimitiveType.VOID;

			switch (operation) {
				case JumpOperation jump -> jumpOperation = jump;
				case SwitchOperation sw -> switchOperation = sw;
				default -> operations.add(operation);
			}
		}

		poppedOperations = stack.getAndResetPoppedOperations();
		pushedOperations = stack.getAndResetPushedOperations();

		methodContext.setCurrentChunk(null);
	}

	/** Инициализирует {@link #condition} и {@link #conditionalChunk} */
	void linkChunks(@UnmodifiableView Object2IntMap<Label> labels,
	                @UnmodifiableView Int2ObjectMap<Chunk> chunks) {

		if (jumpOperation != null) {
			this.condition = jumpOperation.getCondition();
			this.conditionalChunk = chunks.get(labels.getInt(jumpOperation.getLabel()));
		}
	}


	/** Связывает операции между чанками. */
	void linkStackState(@Unmodifiable List<Chunk> chunks) {
		int chunkId = id - 1;

		for (ProxyOperation popped : Objects.requireNonNull(poppedOperations)) {
			for (;;) {
				if (chunkId < 0) {
					throw new DecompilationException("All chunks are over during stack state linking");
				}

				var pushedOps = chunks.get(chunkId).pushedOperations;

				if (!pushedOps.isEmpty()) {
					popped.setOperation(pushedOps.pop());
					break;
				}

				chunkId -= 1;
			}
		}

		poppedOperations.clear();
	}


	/* ---------------------------------------------------- Role ---------------------------------------------------- */

	private boolean jumpOperationAdded = false;

	/**
	 * Инициализирует роль операции перехода.
	 * @throws NullPointerException если операция перехода не найдена.
	 * @throws IllegalStateException если роль уже инициализирована другим значением.
	 */
	public void initRole(Role role) {
		Objects.requireNonNull(jumpOperation).initRole(role);

		if (!jumpOperationAdded && jumpOperation.canWrite()) {
			operations.add(jumpOperation);
			jumpOperationAdded = true;
		}
	}

	/**
	 * @return {@code true}, если у чанка есть операция перехода и ей можно назначить роль.
	 * Это гарантирует, что {@link #jumpOperation}, {@link #conditionalChunk} и
	 * {@link #condition} не равны {@code null}.
	 */
	public boolean canTakeRole() {
		return jumpOperation != null && !jumpOperation.roleInitialized();
	}

	/**
	 * @return {@code true}, если у чанка есть операция перехода с назначенной ролью.
	 * Это гарантирует, что {@link #jumpOperation}, {@link #conditionalChunk} и
	 * {@link #condition} не равны {@code null}.
	 */
	public boolean hasRole() {
		return jumpOperation != null && jumpOperation.roleInitialized();
	}


	/* ---------------------------------------------------- Other ---------------------------------------------------- */

	/** @return {@code true}, если чанк оканчивается терминальной операцией */
	public boolean isTerminal() {
		return Utils.isLast(operations, Operation::isTerminal);
	}

	/** @return {@code true}, если чанк оканчивается операцией {@code throw} */
	public boolean isThrow() {
		return Utils.isLast(operations, Operation::isThrow);
	}

	@Override
	public int compareTo(Chunk other) {
		return this.id - other.id;
	}

	@Override
	public String toString() {
		return String.format("Chunk(#%d, start: %d)", id, startIndex);
	}
}
