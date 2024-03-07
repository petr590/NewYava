package x590.newyava.decompilation;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Label;
import x590.newyava.ContextualWritable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.decompilation.instruction.FlowControlInsn;
import x590.newyava.decompilation.instruction.Instruction;
import x590.newyava.decompilation.operation.condition.ConstCondition;
import x590.newyava.decompilation.operation.condition.JumpOperation;
import x590.newyava.decompilation.scope.*;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.decompilation.variable.VariableTable;
import x590.newyava.descriptor.MethodDescriptor;
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
public class CodeGraph implements ContextualWritable, Importable {

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


	private final VariableTable varTable = new VariableTable();

	public void setVariable(int slotId, Type type, String name, Label start, Label end) {
		varTable.add(slotId, new VariableReference(type, name, labels.getInt(start), labels.getInt(end)));
	}

	public void initVariables(int maxVariables, List<Type> argTypes,
	                          boolean isStatic, Type classType) {

		if (maxVariables < argTypes.size()) {
			throw new IllegalArgumentException("maxVariables less than argTypes.size(): " + maxVariables + ", " + argTypes.size());
		}

		var varTable = this.varTable;

		varTable.extendTo(maxVariables - 1);

		if (!isStatic) {
			var thisSlot = varTable.get(0);

			if (thisSlot.isEmpty()) {
				thisSlot.add(new VariableReference(classType, 0, instructions.size()));
			}
		}

		int offset = isStatic ? 0 : 1;

		for (int i = 0; i < argTypes.size(); i++) {
			var slot = varTable.get(i + offset);
			var argType = argTypes.get(i);

			if (slot.isEmpty()) {
				slot.add(new VariableReference(argType, 0, instructions.size()));
			}

			if (argType.getSize() == TypeSize.LONG)
				offset++;
		}
	}


	private Chunk first, last;

	@Getter
	private MethodScope methodScope;

	public boolean isEmpty() {
		return methodScope.isEmpty();
	}

	public void decompile(MethodDescriptor descriptor, ClassContext context) {
		Int2ObjectMap<Chunk> chunkMap = readChunkMap();

		@Unmodifiable List<Chunk> chunks = chunkMap.values().stream().sorted().toList();

		var methodContext = new MethodContext(descriptor, context, visitor.getModifiers());

		chunks.forEach(chunk -> chunk.decompile(methodContext));
		chunks.forEach(chunk -> chunk.linkChunks(labels, chunkMap));

		List<Scope> scopes = new ArrayList<>();

		// Сначала ищем циклы и операторы break/continue, связанные с ними
		Int2IntMap loopChunkIds = findLoops(first, new Int2IntOpenHashMap(), new HashSet<>());
		addScopes(scopes, chunks, loopChunkIds, LoopScope::new);

		// Затем все условия, которые не являются заголовком цикла/break/continue, становятся if-ами
		Int2IntMap ifChunkIds = findIfs(first, new Int2IntOpenHashMap(), new HashSet<>());
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

		methodScope.initVariables();
		methodScope.removeRedundantOperations(methodContext);
	}


	/** Преобразует список инструкций в список чанков.
	 * Инициализирует {@link #first} и {@link #last} */
	private Int2ObjectMap<Chunk> readChunkMap() {
		for (var label : breakpointLabels) {
			breakpoints.add(labels.getInt(label));
		}

		breakpoints.remove(0); // На индексе 0 всегда начинается первый чанк

		Chunk current = first = new Chunk(0, 0, varTable.toList());

		var chunkMap = new Int2ObjectOpenHashMap<Chunk>();
		chunkMap.put(0, current);

		for (int i = 0, s = instructions.size(); i < s; i++) {
			if (breakpoints.contains(i)) {
				var newChunk = new Chunk(i, chunkMap.size(), varTable.toList());

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
		var methodScope = new MethodScope(chunks);

		int endId = last.getId();

		Queue<Scope> scopeQueue = new LinkedList<>(scopes);
		Scope current = methodScope;

		for (int id = 0; id <= endId; id++) {
			current = current.startScopes(scopeQueue, id);

			current.addOperations(chunks.get(id).getOperations());

			current = current.endIfReached(id);
		}

		return methodScope;
	}

	/**
	 * Ищет все циклы и сохраняет их индексы в {@code loopChunkIds}.
	 * @param loopChunkIds ключ - id начального чанка, значение - id чанка после цикла
	 * @return {@code loopChunkIds}
	 */
	private Int2IntMap findLoops(Chunk chunk, Int2IntMap loopChunkIds, Set<Chunk> visited) {
		if (chunk == null || visited.contains(chunk))
			return loopChunkIds;

		visited.add(chunk);

		Chunk jumpChunk = chunk.getConditionalChunk();

		if (jumpChunk != null) {
			int jumpId = jumpChunk.getId();

			if (jumpId <= chunk.getId()) {
				loopChunkIds.put(jumpId, Math.max(loopChunkIds.get(jumpId), chunk.getId() + 1));
			}

			findLoops(jumpChunk, loopChunkIds, visited);
		}

		return findLoops(chunk.getDirectChunk(), loopChunkIds, visited);
	}

	/**
	 * Ищет все if-ы и сохраняет их индексы в {@code ifChunkIds}.
	 * @param ifChunkIds ключ - id начального чанка, значение - id чанка после if-а
	 * @return {@code ifChunkIds}
	 */
	private Int2IntMap findIfs(Chunk chunk, Int2IntMap ifChunkIds, Set<Chunk> visited) {
		if (chunk == null || visited.contains(chunk))
			return ifChunkIds;

		visited.add(chunk);

		Chunk jumpChunk = chunk.getConditionalChunk();

		if (jumpChunk != null) {
			if (chunk.canTakeRole()) {
				int jumpId = jumpChunk.getId();

				if (jumpId > chunk.getId() && chunk.getCondition() != ConstCondition.TRUE) {

					// Не берём чанк с самим условием, так как
					// в нём может быть код, не относящийся к if
					ifChunkIds.put(chunk.getId() + 1, jumpId);

					chunk.initRole(JumpOperation.Role.IF_BRANCH);
				}
			}

			findIfs(jumpChunk, ifChunkIds, visited);
		}

		return findIfs(chunk.getDirectChunk(), ifChunkIds, visited);
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
				lastIfChunk.initRole(JumpOperation.Role.ELSE_BRANCH);
			}
		}

		return elseChunkIds;
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(methodScope);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.record(methodScope, context);
	}
}
