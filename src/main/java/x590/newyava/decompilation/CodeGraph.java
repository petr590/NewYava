package x590.newyava.decompilation;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.instruction.FlowControlInsn;
import x590.newyava.decompilation.instruction.Instruction;
import x590.newyava.decompilation.operation.condition.ConstCondition;
import x590.newyava.decompilation.operation.condition.Role;
import x590.newyava.decompilation.operation.condition.SwitchOperation;
import x590.newyava.decompilation.scope.*;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableTable;
import x590.newyava.decompilation.variable.VariableTableView;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.*;
import java.util.function.Function;

/**
 * Хранит код и проводит его декомпиляцию.
 */
@RequiredArgsConstructor
public class CodeGraph implements ReadonlyCode {

	private final DecompileMethodVisitor visitor;

	private final List<Instruction> instructions = new ArrayList<>();

	/** Связывает лейбл с индексом инструкции в списке {@link #instructions} */
	private final Object2IntMap<Label> labels = new Object2IntOpenHashMap<>();


	/** Набор лейблов, которые являются границами чанков. Нельзя преобразовать их сразу в индексы,
	 * так как их ещё нет в {@link #labels}, если лейбл указывает вперёд. */
	private final Set<Label> breakpointLabels = new HashSet<>();

	/** Набор индексов, которые являются границами чанков. */
	private final IntSet breakpoints = new IntOpenHashSet();


	/** Добавляет инструкцию. Все инструкции, которые являются {@link FlowControlInsn},
	 * должны быть добавлены с помощью метода {@link #addInstruction(FlowControlInsn)} */
	public void addInstruction(Instruction instruction) {
		instructions.add(instruction);
	}

	public void addInstruction(FlowControlInsn instruction) {
		addInstruction((Instruction) instruction);
		breakpoints.add(instructions.size());
		breakpointLabels.addAll(instruction.getLabels());
	}

	public void addLabel(Label label) {
		labels.put(label, instructions.size());
	}


	/* ------------------------------------------------- Variables ------------------------------------------------- */

	private final VariableTable varTable = new VariableTable();

	@Override
	public VariableTableView getVarTable() {
		return varTable;
	}

	public void setVariable(int slotId, Type type, String name, Label start, Label end) {
		varTable.add(slotId, new VariableReference(type, name, labels.getInt(start), labels.getInt(end)));
	}

	/**
	 * Инициализирует все {@link VariableReference} в таблице переменных,
	 * которые соответствуют {@code this} и параметрам метода.
	 */
	public void initVariables(int maxVariables, List<Type> argTypes, boolean isStatic, Type classType) {

		if (maxVariables < argTypes.size()) {
			throw new DecompilationException(String.format(
					"maxVariables < argTypes.size(): maxVariables = %d, argTypes.size() = %d",
					maxVariables, argTypes.size()
			));
		}

		var varTable = this.varTable;

		varTable.extendTo(maxVariables - 1);

		if (!isStatic) {
			var thisSlot = varTable.get(0);

			if (thisSlot.isEmpty()) {
				thisSlot.add(new VariableReference(classType, "this", 0, instructions.size()));
			}
		}

		int offset = isStatic ? 0 : 1;

		for (int i = 0; i < argTypes.size(); i++) {
			var slot = varTable.get(i + offset);
			var argType = argTypes.get(i);

			if (slot.isEmpty()) {
				slot.add(new VariableReference(argType, 0, instructions.size()));
			}

			if (argType.getSize() == TypeSize.LONG) {
				offset++;
			}
		}
	}

	/* ----------------------------------------------- Decompilation ----------------------------------------------- */

	private Chunk first, last;

	private @Unmodifiable List<Chunk> chunks;
	private MethodContext methodContext;

	private MethodScope methodScope;

	@Override
	public @NotNull MethodScope getMethodScope() {
		return Objects.requireNonNull(methodScope);
	}

	public boolean isEmpty() {
		return methodScope.isEmpty();
	}

	/** Главный метод всего приложения. Именно здесь происходит вся магия.
	 * Инициализирует {@link #methodScope}. */
	public void decompile(MethodDescriptor descriptor, ClassContext context) {
		Int2ObjectMap<Chunk> chunkMap = readChunkMap();

		@Unmodifiable List<Chunk> chunks = chunkMap.values().stream().sorted().toList();

		var methodContext = new MethodContext(context, descriptor, visitor.getModifiers());

		this.chunks = chunks;
		this.methodContext = methodContext;

		chunks.forEach(chunk -> chunk.decompile(methodContext));
		chunks.forEach(chunk -> chunk.linkChunks(labels, chunkMap));

		List<Scope> scopes = new ArrayList<>();

		// Ищем циклы и операторы break/continue, связанные с ними
		addScopes(scopes, chunks, findLoops(chunks), LoopScope::new);

		// Ищем switch и соответствующие break
		List<SwitchScope> switchScopes = findSwitches(chunks, chunkMap);
		scopes.addAll(switchScopes);
		switchScopes.forEach(switchScope -> scopes.addAll(switchScope.getCases()));

		// Все условия, которые не являются заголовком цикла/break/continue, становятся if-ами
		Int2IntMap ifChunkIds = findIfs(chunks);
		addScopes(scopes, chunks, ifChunkIds,
				(ifChunks) -> {
					var condition = Objects.requireNonNull(chunks.get(ifChunks.get(0).getId() - 1).getCondition()).opposite();
					return new IfScope(condition, ifChunks);
				}
		);

		// После if-ов ищем связанные с ними else-ы
		Int2IntMap elseChunkIds = findElses(chunks, ifChunkIds, new Int2IntOpenHashMap());
		addScopes(scopes, chunks, elseChunkIds, ElseScope::new);

		Collections.sort(scopes);
		methodScope = createMethodScope(chunks, scopes);
		methodScope.removeRedundantOperations(methodContext);
	}

	public void beforeVariablesInit() {
		methodScope.beforeVariablesInit(methodScope);
		methodScope.checkCyclicReference();
	}


	/** @return приоритет, в котором должен вызываться метод {@link #initVariables()} */
	public int getVariablesInitPriority() {
		return methodScope.getVariablesInitPriority();
	}

	/** Инициализирует переменные в методе */
	public void initVariables() {
		methodScope.initVariables(chunks);
	}


	/** Преобразует список инструкций в карту чанков.
	 * Ключ - индекс инструкции, значение - чанк, начинающийся на этом индексе.
	 * Инициализирует {@link #first} и {@link #last} */
	private Int2ObjectMap<Chunk> readChunkMap() {
		for (var label : breakpointLabels) {
			breakpoints.add(labels.getInt(label));
		}

		breakpoints.remove(0); // На индексе 0 всегда начинается первый чанк

		Chunk current = first = new Chunk(0, 0, varTable.listView());

		var chunkMap = new Int2ObjectOpenHashMap<Chunk>();
		chunkMap.put(0, current);

		for (int i = 0, s = instructions.size(); i < s; i++) {
			if (breakpoints.contains(i)) {
				var newChunk = new Chunk(i, chunkMap.size(), varTable.listView());

				if (instructions.get(i - 1).canStay()) {
					current.setDirectChunk(newChunk);
				}

				current.setEndIndex(i);

				current = newChunk;
				chunkMap.put(i, current);
			}

			current.addInstruction(instructions.get(i));
		}

		current.setEndIndex(instructions.size());
		this.last = current;

		return chunkMap;
	}


	/**
	 * Добавляет новые scope-ы в {@code scopes}. Каждый scope соответствует паре индексов
	 * из {@code chunkIds}. Для их создания используется функция {@code scopeCreator}
	 */
	private void addScopes(List<Scope> scopes, @Unmodifiable List<Chunk> chunks, Int2IntMap chunkIds,
	                       Function<? super @Unmodifiable List<Chunk>, ? extends Scope> scopeCreator) {

		for (Int2IntMap.Entry entry : chunkIds.int2IntEntrySet()) {
			int startId = entry.getIntKey(),
				endId = entry.getIntValue();

			scopes.add(scopeCreator.apply(chunks.subList(startId, endId)));
		}
	}


	/**
	 * Преобразует список {@link Scope} в {@code MethodScope}, который содержит
	 * все операции и scope-ы, соблюдая их иерархию.
	 */
	private MethodScope createMethodScope(@Unmodifiable List<Chunk> chunks, List<Scope> scopes) {
		var methodScope = new MethodScope(chunks, methodContext);

		int endId = last.getId();

		Queue<Scope> scopeQueue = new LinkedList<>(scopes);
		Scope current = methodScope;

		var generator = new LabelNameGenerator();

		for (int id = 0; id <= endId; id++) {
			current = current.startScopes(scopeQueue, id);

			var operations = chunks.get(id).getOperations();

			current.addOperations(operations);

			for (var operation : operations)
				operation.resolveLabelNames(current, generator);

			current = current.endIfReached(id);
		}

		return methodScope;
	}

	/**
	 * Ищет все циклы и сохраняет их индексы.
	 * @param chunks все чанки в методе.
	 * @return Карту: ключ - id начального чанка, значение - id чанка после цикла
	 */
	private Int2IntMap findLoops(@Unmodifiable List<Chunk> chunks) {
		Int2IntMap loopChunkIds = new Int2IntOpenHashMap();

		for (Chunk chunk : chunks) {
			Chunk jumpChunk = chunk.getConditionalChunk();

			if (jumpChunk != null) {
				int jumpId = jumpChunk.getId();

				if (jumpId <= chunk.getId()) {
					loopChunkIds.put(jumpId, Math.max(loopChunkIds.get(jumpId), chunk.getId() + 1));
				}
			}
		}

		return loopChunkIds;
	}


	/** Ищет все switch и возвращает их список */
	private List<SwitchScope> findSwitches(@Unmodifiable List<Chunk> chunks, Int2ObjectMap<Chunk> chunkMap) {
		List<SwitchScope> switchScopes = new ArrayList<>();

		for (Chunk chunk : chunks) {
			var switchOperation = chunk.getSwitchOperation();

			if (switchOperation != null) {
				SortedMap<Chunk, @Nullable Collection<IntConstant>> table = new TreeMap<>();

				for (var entry : switchOperation.table().int2ObjectEntrySet()) {
					table.computeIfAbsent(
							chunkMap.get(labels.getInt(entry.getValue())),
							c -> new ArrayList<>()
					).add(IntConstant.valueOf(entry.getIntKey()));
				}

				table.put(chunkMap.get(labels.getInt(switchOperation.defaultLabel())), null);

				switchScopes.add(createSwitchScope(switchOperation, chunks, table));
			}
		}

		return switchScopes;
	}

	private static SwitchScope createSwitchScope(SwitchOperation switchOperation, @Unmodifiable List<Chunk> chunks,
	                                             SortedMap<Chunk, @Nullable Collection<IntConstant>> table) {

		var entries = new ArrayList<>(table.entrySet());

		List<SwitchScope.CaseScope> cases = new ArrayList<>();

		// Собираем все case кроме последнего
		for (int i = 0, s = entries.size() - 1; i < s; i++) {
			cases.add(new SwitchScope.CaseScope(
					chunks.subList(
							entries.get(i).getKey().getId(),
							entries.get(i + 1).getKey().getId()
					),
					entries.get(i).getValue()
			));
		}

		// Ищем, где конец последнего case
		var lastEntry = entries.get(entries.size() - 1);
		int lastId = lastEntry.getKey().getId();

		var minEndId = cases.stream()
				.map(Scope::getEndChunk).filter(Chunk::canTakeRole)
				.map(Chunk::getConditionalChunk).filter(Objects::nonNull)
				.mapToInt(Chunk::getId).filter(id -> id >= lastId)
				.max();

		if (minEndId.isPresent() && lastId != minEndId.getAsInt()) {
			cases.add(new SwitchScope.CaseScope(
					chunks.subList(lastId, minEndId.getAsInt()),
					lastEntry.getValue()
			));
		}

		return new SwitchScope(
				switchOperation.value(),
				cases,
				chunks.subList(
						cases.get(0).getStartChunk().getId(),
						cases.get(cases.size() - 1).getEndChunk().getId() + 1
				)
		);
	}

	/**
	 * Ищет все if-ы и сохраняет их индексы.
	 * @param chunks все чанки в методе.
	 * @return Карту: ключ - id начального чанка, значение - id чанка после if-а
	 */
	private Int2IntMap findIfs(@Unmodifiable List<Chunk> chunks) {
		Int2IntMap ifChunkIds = new Int2IntOpenHashMap();

		for (Chunk chunk : chunks) {
			Chunk jumpChunk = chunk.getConditionalChunk();

			if (jumpChunk != null) {
				if (chunk.canTakeRole()) {
					int jumpId = jumpChunk.getId();

					if (jumpId > chunk.getId() && chunk.getCondition() != ConstCondition.TRUE) {

						// Не берём чанк с самим условием, так как
						// в нём может быть код, не относящийся к if
						ifChunkIds.put(chunk.getId() + 1, jumpId);

						chunk.initRole(Role.IF_BRANCH);
					}
				}
			}
		}

		return ifChunkIds;
	}

	/**
	 * Ищет все else-ы и сохраняет их индексы в {@code elseChunkIds}.
	 * @param ifChunkIds найденные if-ы
	 * @param elseChunkIds ключ - id начального чанка, значение - id чанка после else-а
	 * @return {@code elseChunkIds}
	 */
	private Int2IntMap findElses(@Unmodifiable List<Chunk> chunks, Int2IntMap ifChunkIds, Int2IntMap elseChunkIds) {
		for (Int2IntMap.Entry entry : ifChunkIds.int2IntEntrySet()) {
			Chunk lastIfChunk = chunks.get(entry.getIntValue() - 1);

			if (lastIfChunk.canTakeRole() &&
				lastIfChunk.requireCondition() == ConstCondition.TRUE &&
				lastIfChunk.requireConditionalChunk().getId() > lastIfChunk.getId()) {

				elseChunkIds.put(lastIfChunk.getId() + 1, lastIfChunk.requireConditionalChunk().getId());
				lastIfChunk.initRole(Role.ELSE_BRANCH);
			}
		}

		return elseChunkIds;
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(methodScope);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(methodScope, context);
	}
}
