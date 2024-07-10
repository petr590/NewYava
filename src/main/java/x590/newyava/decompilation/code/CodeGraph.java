package x590.newyava.decompilation.code;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.objectweb.asm.Label;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.instruction.FlowControlInsn;
import x590.newyava.decompilation.instruction.Instruction;
import x590.newyava.decompilation.operation.condition.ConstCondition;
import x590.newyava.decompilation.operation.condition.OperatorCondition;
import x590.newyava.decompilation.operation.condition.Role;
import x590.newyava.decompilation.operation.condition.SwitchOperation;
import x590.newyava.decompilation.operation.variable.CatchOperation;
import x590.newyava.decompilation.scope.*;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableTable;
import x590.newyava.decompilation.variable.VariableTableView;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.*;
import java.util.function.Function;

/**
 * Хранит код и проводит его декомпиляцию.
 */
@RequiredArgsConstructor
public class CodeGraph implements Code {

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
		varTable.add(slotId, new VariableReference(type, name, slotId, labels.getInt(start), labels.getInt(end)));
	}

	/**
	 * Инициализирует все {@link VariableReference} в таблице переменных,
	 * которые соответствуют {@code this} и параметрам метода.
	 */
	public void initVariables(int maxVariables, MethodDescriptor descriptor, boolean isStatic) {
		var argTypes = descriptor.arguments();

		if (maxVariables < argTypes.size()) {
			throw new DecompilationException(
					"maxVariables < argTypes.size(): maxVariables = %d, argTypes.size() = %d",
					maxVariables, argTypes.size()
			);
		}

		var varTable = this.varTable;

		varTable.extendTo(maxVariables - 1);

		if (!isStatic) {
			var thisSlot = varTable.get(0);

			if (thisSlot.isEmpty()) {
				thisSlot.add(new VariableReference(descriptor.hostClass(), "this", 0, 0, instructions.size()));
			}
		}

		int offset = isStatic ? 0 : 1;

		for (int i = 0; i < argTypes.size(); i++) {
			var slot = varTable.get(i + offset);
			var argType = argTypes.get(i);

			if (slot.isEmpty()) {
				slot.add(new VariableReference(argType, i + offset, 0, instructions.size()));
			}

			if (argType.getSize() == TypeSize.LONG) {
				offset++;
			}
		}
	}


	public void inferVariableTypesAndNames() {
		// Запускаем два раза для правильного вычисления типа констант
		methodScope.inferType(PrimitiveType.VOID);
		methodScope.inferType(PrimitiveType.VOID);

		methodScope.declareVariables();
		methodScope.initVariableNames();
	}


	/* ------------------------------------------------ MethodScope ------------------------------------------------- */

	private MethodScope methodScope;

	@Override
	public @NotNull MethodScope getMethodScope() {
		return Objects.requireNonNull(methodScope);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean caughtException() {
		return false;
	}

	public boolean isEmpty() {
		return methodScope.isEmpty();
	}


	/* ------------------------------------------------- Try, catch ------------------------------------------------- */
	private record TryCatchBlock(Label start, Label end, Label handler, @Nullable ClassType type) {}

	private final List<TryCatchBlock> tryCatchBlocks = new ArrayList<>();

	public void addTryCatchBlock(Label start, Label end, Label handler, @Nullable ClassType type) {
		breakpointLabels.add(start);
		breakpointLabels.add(end);
		breakpointLabels.add(handler);

		tryCatchBlocks.add(new TryCatchBlock(start, end, handler, type));
	}

	/**
	 * @return карту: ключ - блок try, значения - блоки catch, привязанные к этому try.
	 */
	private Map<IntIntPair, Int2ObjectMap<CatchOperation>> getTryCatchMap(
			@UnmodifiableView Int2ObjectMap<Chunk> chunkMap
	) {
		Map<IntIntPair, Int2ObjectMap<CatchOperation>> result = new HashMap<>();

		for (TryCatchBlock block : tryCatchBlocks) {
			int tryStart = chunkMap.get(labels.getInt(block.start)).getId(),
				tryEnd = chunkMap.get(labels.getInt(block.end)).getId(),
				catchStart = chunkMap.get(labels.getInt(block.handler)).getId();

			// Нашли try, который совпадает с другим catch
			if (block.type == null &&
				result.values().stream().anyMatch(catches -> catches.containsKey(tryStart))) {
				continue; // Пока что проигнорируем его
			}

			var sameCatchEntry = result.entrySet().stream()
					.filter(tryCatches -> tryCatches.getValue().containsKey(catchStart))
					.findAny();

			// Если catch совпадает с другим catch,
			if (sameCatchEntry.isPresent()) {
				// ... то объединяем два try в один
				var anotherTry = sameCatchEntry.get().getKey();

				var unitedTry = IntIntPair.of(
						Math.min(tryStart, anotherTry.leftInt()),
						Math.max(tryEnd, anotherTry.rightInt())
				);

				if (!anotherTry.equals(unitedTry)) {
					result.remove(anotherTry);

					var catches = sameCatchEntry.get().getValue();

					result.compute(unitedTry, (key, otherCatches) -> {
						if (otherCatches != null)
							catches.putAll(otherCatches);

						return catches;
					});
				}

				continue;
			}

			var tryBlock = IntIntPair.of(tryStart, tryEnd);

			var catchBlock = result
					.computeIfAbsent(tryBlock, tb -> new Int2ObjectOpenHashMap<>())
					.computeIfAbsent(catchStart, cs -> new CatchOperation());

			if (block.type != null) {
				catchBlock.add(block.type);
			}
		}

		for (var catches : result.values()) {
			for (var catchOp : catches.values()) {
				catchOp.finalizeExceptionTypesInit();
			}
		}

		return result;
	}

	private void initCatchEndIndexes(Map<IntIntPair, Int2ObjectMap<CatchOperation>> tryCatchMap,
	                                 @Unmodifiable List<Chunk> chunks) {

		for (var tryBlock : tryCatchMap.keySet()) {
			var iter = tryCatchMap.get(tryBlock)
					.int2ObjectEntrySet().stream()
					.sorted(Comparator.comparingInt(Int2ObjectMap.Entry::getIntKey))
					.iterator();

			var catch1 = iter.next();

			// Инициализируем конечные индексы всех catch, кроме последнего
			while (iter.hasNext()) {
				var catch2 = iter.next();
				catch1.getValue().setEndId(catch2.getIntKey());
				catch1 = catch2;
			}

			// Инициализируем конечные индексы последнего catch
			var lastCatchEnd = chunks.get(tryBlock.secondInt()).getConditionalChunk();
			catch1.getValue().setEndId(lastCatchEnd != null ? lastCatchEnd.getId() : chunks.size());
		}
	}

	/** Инициализирует конечный индекс всех catch блоков */
	private void addTryCatchScopes(
			List<Scope> scopes,
	        Map<IntIntPair, Int2ObjectMap<CatchOperation>> tryCatchMap,
	        @Unmodifiable List<Chunk> chunks
	) {
		for (IntIntPair tryBlock : tryCatchMap.keySet()) {
			scopes.add(new TryScope(chunks.subList(tryBlock.firstInt(), tryBlock.secondInt() + 1)));

			for (var catchEntry : tryCatchMap.get(tryBlock).int2ObjectEntrySet()) {
				scopes.add(new CatchScope(
						chunks.subList(catchEntry.getIntKey(), catchEntry.getValue().getEndId()),
						catchEntry.getValue()
				));
			}
		}
	}


	/* ----------------------------------------------- Decompilation ------------------------------------------------ */

	private Chunk last;

	private MethodContext methodContext;


	/** Главный метод приложения. Именно здесь происходит вся магия.
	 * Декомпилирует все операции, создаёт {@link Scope}-ы и инициализирует {@link #methodScope}. */
	public void decompile(MethodDescriptor descriptor, Context context) {
		Int2ObjectMap<Chunk> chunkMap = readChunkMap();
		@Unmodifiable List<Chunk> chunks = chunkMap.values().stream().sorted().toList();

		var methodContext = new MethodContext(context, descriptor, visitor.getModifiers());
		this.methodContext = methodContext;

		var tryCatchMap = getTryCatchMap(chunkMap);

		chunks.forEach(chunk -> chunk.decompile(methodContext, tryCatchMap.values()));
		chunks.forEach(chunk -> chunk.linkChunks(labels, chunkMap));

		initCatchEndIndexes(tryCatchMap, chunks);


		List<Scope> scopes = new ArrayList<>();

		// Добавляем try-catch
		addTryCatchScopes(scopes, tryCatchMap, chunks);

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
					var condition = chunks.get(ifChunks.get(0).getId() - 1).requireCondition().opposite();
					return new IfScope(condition, ifChunks);
				}
		);

		// После if-ов ищем связанные с ними else-ы
		Int2IntMap elseChunkIds = findElses(chunks, ifChunkIds);
		addScopes(scopes, chunks, elseChunkIds, ElseScope::new);

		// И строим иерархию Scope-ов
		Collections.sort(scopes);
		methodScope = hierarchizeScopes(chunks, scopes);

		linkChunkStackStates(chunks);

		methodScope.postDecompilation(methodContext);
	}


	/** Преобразует список инструкций в карту чанков.
	 * Ключ - индекс инструкции, значение - чанк, начинающийся на этом индексе.
	 * Инициализирует поле {@link #last}. */
	private Int2ObjectMap<Chunk> readChunkMap() {
		for (var label : breakpointLabels) {
			breakpoints.add(labels.getInt(label));
		}

		breakpoints.remove(0); // На индексе 0 всегда начинается первый чанк

		Chunk current = new Chunk(0, 0, varTable.listView());

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
	 * @return methodScope, который является вершиной всей иерархии.
	 */
	private MethodScope hierarchizeScopes(@Unmodifiable List<Chunk> chunks, List<Scope> scopes) {
		var methodScope = new MethodScope(chunks, methodContext);

		int endId = last.getId();

		Queue<Scope> scopeQueue = new LinkedList<>(scopes);
		Scope current = methodScope;

		var generator = new LabelNameGenerator();

		for (int id = 0; id <= endId; id++) {
			current = current.startScopes(scopeQueue, id);

			var chunk = chunks.get(id);

			current.addOperationsFromChunk(chunk);

			for (var operation : chunk.getOperations())
				operation.resolveLabelNames(current, generator);

			current = current.endIfReached(id);
		}

		return methodScope;
	}


	private void linkChunkStackStates(@Unmodifiable List<Chunk> chunks) {
		chunks.forEach(chunk -> chunk.linkStackState(chunks));
	}


	/* --------------------------------------------------- Loops ---------------------------------------------------- */

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


	/* -------------------------------------------------- Switches -------------------------------------------------- */

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
					entries.get(i).getValue(),
					false
			));
		}

		// Ищем, где конец последнего case
		var lastEntry = entries.get(entries.size() - 1);
		int lastId = lastEntry.getKey().getId();

		var minEndId = cases.stream()
				.map(Scope::getEndChunk).filter(Chunk::canTakeRole)
				.map(Chunk::getConditionalChunk).filter(Objects::nonNull)
				.mapToInt(Chunk::getId).filter(id -> id > lastId)
				.max();

		if (minEndId.isPresent()) {
			cases.add(new SwitchScope.CaseScope(
					chunks.subList(lastId, minEndId.getAsInt()),
					lastEntry.getValue(),
					true
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


	/* ----------------------------------------------- Ifs and elses ------------------------------------------------ */

	/**
	 * Ищет все if-ы и сохраняет их индексы.
	 * @param chunks все чанки в методе.
	 * @return Карту: ключ - id первого чанка, не включающего условие, значение - id чанка после if-а
	 */
	private Int2IntMap findIfs(@Unmodifiable List<Chunk> chunks) {
		List<IfEntry> ifEntries = new LinkedList<>();

		for (Chunk chunk : chunks) {
			Chunk jumpChunk = chunk.getConditionalChunk();

			if (jumpChunk != null && chunk.getCondition() != ConstCondition.TRUE && chunk.canTakeRole()) {
				int jumpId = jumpChunk.getId();

				if (jumpId > chunk.getId()) {
					// Не берём чанк с самим условием, так как
					// в нём может быть код, не относящийся к if
					int startChunkId = chunk.getId() + 1;

					ifEntries.add(new IfEntry(startChunkId, startChunkId, jumpId));

					chunk.initRole(Role.IF_BRANCH);
				}
			}
		}

		boolean result;

		do {
			result = collapseConditions(chunks, ifEntries);
		} while (result);


		return ifEntries.stream().collect(
				Int2IntOpenHashMap::new,
				(map, ifEntry) -> map.put(ifEntry.end, ifEntry.jump),
				Int2IntMap::putAll
		);
	}


	@ToString
	@AllArgsConstructor
	private static final class IfEntry {
		/** Индекс чанка, на котором начинается условие */
		private int start;

		/** Индекс первого чанка после условия */
		private int end;

		/** Индекс чанка, на который совершается переход */
		private int jump;
	}


	/**
	 * Объединяет отдельные условия в "and" и "or" условия. Изменяет {@code ifEntries} в процессе.
	 * @return {@code true}, если объединено хотя бы одно условие, иначе {@code false}.
	 */
	private boolean collapseConditions(@Unmodifiable List<Chunk> chunks, List<IfEntry> ifEntries) {
		for (var entry2 : ifEntries) {
			var foundEntry = ifEntries.stream()
					.filter(entry1 -> entry1 != entry2 && entry1.end + 1 == entry2.start).findAny();

			if (foundEntry.isPresent()) {
				var entry1 = foundEntry.get();

				// Нашли "and" или "or"
				if (entry1.jump == entry2.jump ||
					entry1.jump == entry2.end) {

					Chunk chunk1 = chunks.get(entry1.end - 1),
						  chunk2 = chunks.get(entry2.end - 1);

					// Между условиями не должно быть другого кода
					if (!chunk2.getOperations().isEmpty()) continue;

					// В IfScope условие инвертируется, поэтому "and" станет "or", и наоборот

					if (entry1.jump == entry2.jump) { // and
						chunk2.changeCondition(OperatorCondition.or(
								chunk1.requireCondition(),
								chunk2.requireCondition()
						));

					} else { // or
						chunk2.changeCondition(OperatorCondition.and(
								chunk1.requireCondition().opposite(),
								chunk2.requireCondition()
						));
					}

					chunk1.changeCondition(ConstCondition.FALSE);

					entry2.start = entry1.start;
					ifEntries.remove(entry1);
					return true;
				}
			}
		}

		return false;
	}




	/**
	 * Ищет все else-ы и сохраняет их индексы в {@code elseChunkIds}.
	 * @param ifChunkIds найденные if-ы
	 * @return Карту: ключ - id начального чанка, значение - id чанка после else-а
	 */
	private Int2IntMap findElses(@Unmodifiable List<Chunk> chunks, Int2IntMap ifChunkIds) {
		Int2IntMap elseChunkIds = new Int2IntOpenHashMap();

		for (Int2IntMap.Entry entry : ifChunkIds.int2IntEntrySet()) {
			Chunk lastIfChunk = chunks.get(entry.getIntValue() - 1);

			if (lastIfChunk.canTakeRole() &&
				lastIfChunk.requireCondition() == ConstCondition.TRUE) {

				int start = lastIfChunk.getId(),
					end = lastIfChunk.requireConditionalChunk().getId();

				if (start < end) {
					elseChunkIds.put(start + 1, end);
					lastIfChunk.initRole(Role.ELSE_BRANCH);
				}
			}
		}

		return elseChunkIds;
	}


	/* --------------------------------------------- After decompiling ---------------------------------------------- */

	public void afterDecompilation() {
		methodScope.afterDecompilation(methodContext);
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
		methodScope.initVariables(visitor.getArgumentsSizes());
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(methodScope);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(methodScope, new MethodWriteContext(context, methodScope));
	}
}
