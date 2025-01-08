package x590.newyava.decompilation.code;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
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
import x590.newyava.decompilation.operation.emptyscope.EmptyableScopeOperation;
import x590.newyava.decompilation.operation.OperationUtils;
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
import x590.newyava.util.Utils;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.*;
import java.util.function.Function;

/**
 * Хранит код и проводит его декомпиляцию.
 */
@RequiredArgsConstructor
public final class CodeGraph extends Code {

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

	public int getSize() {
		return instructions.size();
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


	/** @param possibleNames список имён аргументов. */
	public void inferVariableTypesAndNames(@Nullable @Unmodifiable List<String> possibleNames) {
		var methodScope = getMethodScope();

		if (possibleNames != null) {
			var descriptor = methodScope.getMethodContext().getDescriptor();
			var variables = methodScope.getVariables();

			int i = 0;
			int slot = methodScope.getMethodContext().isStatic() ? 0 : 1;

			for (String name : possibleNames) {
				Objects.requireNonNull(variables.get(slot)).addPossibleName(name);
				slot += descriptor.arguments().get(i).getSize().slots();
			}
		}

		// Запускаем два раза для правильного вычисления типа констант
		methodScope.inferType(PrimitiveType.VOID);
		methodScope.inferType(PrimitiveType.VOID);

		methodScope.declareVariables();
		methodScope.initPossibleVarNames();
		methodScope.initVariableNames();
	}


	/* ------------------------------------------------ MethodScope ------------------------------------------------- */

	private @Nullable MethodScope methodScope;

	@Override
	public MethodScope getMethodScope() {
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
		return getMethodScope().isEmpty();
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

	/** @return карту: ключ - блок try, значения - блоки catch, привязанные к этому try. */
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

	/** Инициализирует конечный индекс всех catch блоков */
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

			// Инициализируем конечный индекс последнего catch
			var lastCatchEnd = chunks.get(tryBlock.secondInt()).getConditionalChunk();
			if (lastCatchEnd != null && lastCatchEnd.getId() > catch1.getIntKey()) {
				catch1.getValue().setEndId(lastCatchEnd.getId());
			} else {
				catch1.getValue().setEndId(chunks.size());
			}
		}
	}

	/** Добавляет все блоки try-catch и synchronized */
	private void addTryCatchSynchronizedScopes(
			List<Scope> scopes,
	        Map<IntIntPair, Int2ObjectMap<CatchOperation>> tryCatchMap,
	        @Unmodifiable List<Chunk> chunks
	) {
		for (var entry : tryCatchMap.entrySet()) {
			var tryBlock = entry.getKey();
			var catchMap = entry.getValue();
			int tryEnd = catchMap.keySet().intStream().min().orElse(tryBlock.secondInt());

			var synchronizedScope = OperationUtils.getSynchronizedScope(tryBlock, catchMap, chunks);

			if (synchronizedScope != null) {
				scopes.add(synchronizedScope);
				continue;
			}


			int lastCatchEndId = catchMap.int2ObjectEntrySet().stream()
					.mapToInt(catchEntry -> catchEntry.getValue().getEndId())
					.max().orElse(tryEnd);

			var joiningScope = new JoiningTryCatchScope(chunks.subList(tryBlock.firstInt(), lastCatchEndId));
			scopes.add(joiningScope);

			scopes.add(new TryScope(
					chunks.subList(tryBlock.firstInt(), tryEnd),
					joiningScope
			));

			for (var catchEntry : catchMap.int2ObjectEntrySet()) {
				scopes.add(new CatchScope(
						chunks.subList(catchEntry.getIntKey(), catchEntry.getValue().getEndId()),
						catchEntry.getValue(),
						joiningScope
				));
			}
		}
	}


	/* ----------------------------------------------- Decompilation ------------------------------------------------ */

	private @Nullable Chunk last;

	private @Nullable MethodContext methodContext;

	public MethodContext getMethodContext() {
		return Objects.requireNonNull(methodContext);
	}


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

		// Добавляем try-catch и synchronized
		addTryCatchSynchronizedScopes(scopes, tryCatchMap, chunks);
//		System.out.println(chunks); // DEBUG
//		System.out.println(scopes); // DEBUG

		// Ищем циклы и операторы break/continue, связанные с ними
		addScopes(scopes, chunks, findLoops(chunks), LoopScope::create);

		// Ищем switch и соответствующие break
		addSwitchScopes(scopes, chunks, chunkMap);

		// Все условия, которые не являются заголовком цикла/break/continue, становятся if-ами
		Int2IntMap ifChunkIds = findIfs(chunks);
		addScopes(scopes, chunks, ifChunkIds,
				(allChunks, startId, endId) -> {
					var condition = allChunks.get(startId - 1).requireCondition().opposite();
					return IfScope.create(condition, allChunks.subList(startId, endId));
				}
		);

		// После if-ов ищем связанные с ними else-ы
		Int2IntMap elseChunkIds = findElses(chunks, ifChunkIds);
		addScopes(scopes, chunks, elseChunkIds, ElseScope::create);

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

		breakpoints.remove(0); // На индексе 0 всегда начинается первый чанк, тут не нужен брекпоинт

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


	@FunctionalInterface
	private interface ScopeCreator {
		/**
		 * Создаёт новый scope или пустую операцию.
		 * @param chunks список всех чанков. Индекс в этом списке соответствует id чанка.
		 * @param startId id начала scope.
		 * @param endId id конца scope.
		 * @return созданный scope или пустую операцию.
		 */
		EmptyableScopeOperation create(@Unmodifiable List<Chunk> chunks, int startId, int endId);
	}


	/**
	 * Добавляет новые scope-ы в {@code scopes}. Каждый scope
	 * соответствует паре индексов из {@code chunkIds}.
	 * @param scopeCreator функция для создания scope-ов. Принимает
	 *                     список чанков, которые принадлежат ему.
	 */
	private void addScopes(List<Scope> scopes, @Unmodifiable List<Chunk> chunks, Int2IntMap chunkIds,
	                       Function<@Unmodifiable List<Chunk>, EmptyableScopeOperation> scopeCreator) {

		addScopes(scopes, chunks, chunkIds,
				(allChunks, startId, endId) -> scopeCreator.apply(allChunks.subList(startId, endId)));
	}


	/**
	 * Добавляет новые scope-ы в {@code scopes}. Каждый scope соответствует паре индексов
	 * из {@code chunkIds}. Для их создания используется {@code scopeCreator}
	 */
	private void addScopes(List<Scope> scopes, @Unmodifiable List<Chunk> chunks,
	                       Int2IntMap chunkIds, ScopeCreator scopeCreator) {

		for (Int2IntMap.Entry entry : chunkIds.int2IntEntrySet()) {
			int startId = entry.getIntKey(),
				endId = entry.getIntValue();

			var scopeOperation = scopeCreator.create(chunks, startId, endId);

			if (scopeOperation instanceof Scope scope) {
				scopes.add(scope);
			} else {
				chunks.get(startId).getOperations().add(0, scopeOperation);
			}

//			System.out.println(scopes); // DEBUG
		}
	}


	/**
	 * Преобразует список {@link Scope} в {@code MethodScope}, который содержит
	 * все операции и scope-ы, соблюдая их иерархию.
	 * @return methodScope, который является вершиной всей иерархии.
	 */
	private MethodScope hierarchizeScopes(@Unmodifiable List<Chunk> chunks, List<Scope> scopes) {
		var methodScope = new MethodScope(chunks, getMethodContext());

		assert last != null;
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

			current = current.endIfReached(id, chunks);
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

	/** Ищет все switch и case и добавляет их в список {@code scopes}. */
	private void addSwitchScopes(List<Scope> scopes, @Unmodifiable List<Chunk> chunks, Int2ObjectMap<Chunk> chunkMap) {
		for (Chunk chunk : chunks) {
			var switchOperation = chunk.getSwitchOperation();

			if (switchOperation != null) {
				// Ключ - чанк начала блока case, значение - список констант case
				SortedMap<Chunk, @Nullable Collection<IntConstant>> table = new TreeMap<>();

				for (var entry : switchOperation.table().int2ObjectEntrySet()) {
					table.computeIfAbsent(
							chunkMap.get(labels.getInt(entry.getValue())),
							c -> new ArrayList<>()
					).add(IntConstant.valueOf(entry.getIntKey()));
				}

				// Для блока default список констант всегда null
				table.put(chunkMap.get(labels.getInt(switchOperation.defaultLabel())), null);

				var scopeOperation = createSwitchScope(switchOperation, chunks, table);

				if (scopeOperation instanceof SwitchScope switchScope) {
					scopes.add(switchScope);
					scopes.addAll(switchScope.getCases());
				} else {
					chunk.getOperations().add(0, scopeOperation);
				}
			}
		}
	}

	private static EmptyableScopeOperation createSwitchScope(
			SwitchOperation switchOperation, @Unmodifiable List<Chunk> chunks,
	        SortedMap<Chunk, @Nullable Collection<IntConstant>> table
	) {
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
		var lastEntry = Utils.getLast(entries);
		int lastStartId = lastEntry.getKey().getId();

		int lastEndId = cases.stream()
				.map(Scope::getEndChunk).filter(Chunk::canTakeRole)
				.map(Chunk::getConditionalChunk).filter(Objects::nonNull)
				.mapToInt(Chunk::getId).filter(id -> id > lastStartId)
				.max().orElse(chunks.size());

		cases.add(new SwitchScope.CaseScope(
				chunks.subList(lastStartId, lastEndId),
				lastEntry.getValue(),
				true
		));

		return SwitchScope.create(switchOperation.value(), cases, chunks);
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
		getMethodScope().afterDecompilation(getMethodContext());
	}

	public void beforeVariablesInit() {
		var methodScope = getMethodScope();
		methodScope.beforeVariablesInit(methodScope.getMethodContext(), methodScope);
		methodScope.checkCyclicReference();
	}


	/** @return приоритет, в котором должен вызываться метод {@link #initVariables()} */
	public int getVariablesInitPriority() {
		return getMethodScope().getVariablesInitPriority();
	}

	/** Инициализирует переменные в методе */
	public void initVariables() {
		getMethodScope().initVariables(visitor.getArgumentsSizes());
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(methodScope);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(getMethodScope(), new MethodWriteContext(context, methodScope));
	}
}
