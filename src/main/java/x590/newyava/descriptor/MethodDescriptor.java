package x590.newyava.descriptor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Handle;
import x590.newyava.Importable;
import x590.newyava.Literals;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.exception.InvalidDescriptorException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;
import x590.newyava.type.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;
import java.util.stream.Collectors;

/**
 * Дескриптор метода.
 */
public record MethodDescriptor(
		IClassArrayType hostClass,
		String name,
		Type returnType,
		@Unmodifiable List<Type> arguments
) implements Importable {

	public static final String
			INIT = "<init>",
			CLINIT = "<clinit>";

	public MethodDescriptor {
		switch (name) {
			case INIT -> {
				if (returnType != PrimitiveType.VOID)
					throw new InvalidDescriptorException("Constructor has wrong return type: " + returnType);
			}

			case CLINIT -> {
				if (returnType != PrimitiveType.VOID)
					throw new InvalidDescriptorException("Static initializer has wrong return type: " + returnType);

				if (!arguments.isEmpty())
					throw new InvalidDescriptorException("Static initializer has arguments: " + arguments);
			}
		}
	}

	public MethodDescriptor(IClassArrayType hostClass, String name, Type returnType) {
		this(hostClass, name, returnType, Collections.emptyList());
	}

	public static MethodDescriptor of(IClassArrayType hostClass, String name, String argsAndReturnType) {
		var reader = new SignatureReader(argsAndReturnType);

		List<Type> arguments = Type.parseMethodArguments(reader);
		Type returnType = Type.parseReturnType(reader);
		reader.checkEndForType();

		return new MethodDescriptor(hostClass, name, returnType, Collections.unmodifiableList(arguments));
	}

	public static MethodDescriptor of(Handle handle) {
		return of(IClassArrayType.valueOf(handle.getOwner()), handle.getName(), handle.getDesc());
	}

	public static MethodDescriptor constructor(IClassArrayType hostClass) {
		return constructor(hostClass, Collections.emptyList());
	}

	public static MethodDescriptor constructor(IClassArrayType hostClass, @Unmodifiable List<Type> arguments) {
		return new MethodDescriptor(hostClass, INIT, PrimitiveType.VOID, arguments);
	}

	public static int slots(@Unmodifiable List<Type> arguments) {
		return arguments.stream().mapToInt(type -> type.getSize().slots()).sum();
	}

	public int slots() {
		return slots(arguments);
	}


	/**
	 * @return индекс аргумента по слоту переменной
	 * (для нестатических методов ссылка на {@code this} не учитывается).
	 * @throws IllegalArgumentException если слот меньше нуля или ни один индекс не соответствует слоту.
	 */
	public int indexBySlot(int slot) {
		if (slot < 0)
			throw new IllegalArgumentException("Negative slot: " + slot);

		for (int i = 0, s = 0, size = arguments.size(); i < size; i++) {
			if (s == slot) {
				return i;
			}

			s += arguments.get(i).getSize().slots();
		}

		throw new IllegalArgumentException(String.format("Cannot find index for slot %d in descriptor %s", slot, this));
	}


	/** @return Новый дескриптор с аргументами начиная с {@code fromIndex} до {@code toIndex} не включительно.
	 * Если {@code fromIndex} равен 0, а {@code toIndex} равен количеству аргументов, возвращает {@code this}. */
	public MethodDescriptor slice(int fromIndex, int toIndex) {
		return fromIndex == 0 && toIndex == arguments.size() ?
				this :
				new MethodDescriptor(hostClass, name, returnType, arguments.subList(fromIndex, toIndex));
	}


	public boolean isConstructor() {
		return name.equals(INIT);
	}

	public boolean isStaticInitializer() {
		return name.equals(CLINIT);
	}


	public boolean isRecordDefaultConstructor(Context context) {
		if (!isConstructor()) return false;

		var recordComponents = context.getRecordComponents();

		if (recordComponents != null && recordComponents.size() == arguments.size()) {
			var iterator = arguments.iterator();
			return recordComponents.stream()
					.allMatch(component -> component.getDescriptor().type().baseEquals(iterator.next()));
		}

		return false;
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImport(returnType).addImportsFor(arguments);
	}

	/**
	 * @param startSlot слот, с которого начинаются аргументы.
	 * @param variables список переменных данного метода или {@code null}, если метод не имеет тела.
	 * @param possibleNames список имён аргументов. Используется, если {@code variables == null}.
	 */
	public void write(DecompilationWriter out, Context context, boolean isVarargs, int startSlot,
	                  @Nullable @Unmodifiable List<Variable> variables,
	                  @Nullable @Unmodifiable List<String> possibleNames) {

		switch (name) {
			case CLINIT -> {
				out.record(Literals.LIT_STATIC);
				return;
			}

			case INIT -> {
				out.record(hostClass, context);

				if (isRecordDefaultConstructor(context)) {
					return;
				}
			}

			default -> out.record(returnType, context).space().record(name);
		}

		// Принимает индекс аргумента метода, возвращает имя этого аргумента
		IntFunction<String> nameGetter = variables != null ?
				new NameGetter(variables, startSlot) :
				new NameGenerator(possibleNames);

		// Принимает тип и индекс аргумента, записывает его
		ObjIntConsumer<Type> writer = (type, i) -> {
			if (isVarargs && i == arguments.size() - 1 && type instanceof ArrayType arrayType) {
				out.record(arrayType.getElementType(), context).record("... ");
			} else {
				out.record(type, context).space();
			}

			out.record(nameGetter.apply(i));
		};

		out.record('(').record(arguments, ", ", writer).record(')');
	}

	/** Получает имя из списка переменных */
	private class NameGetter implements IntFunction<String> {
		private final @Unmodifiable List<Variable> variables;
		private int offset;

		public NameGetter(List<Variable> variables, int startSlot) {
			this.variables = variables;
			this.offset = startSlot;
		}

		@Override
		public String apply(int index) {
			String name = variables.get(index + offset).getName();

			if (arguments.get(index).getSize() == TypeSize.LONG)
				offset++;

			return Objects.requireNonNull(name);
		}
	}

	/** Генерирует имена всех параметров метода на основе их типов */
	private class NameGenerator implements IntFunction<String> {
		private final List<String> names;

		public NameGenerator(@Nullable @Unmodifiable List<String> possibleNames) {
			List<String> names = possibleNames != null ?
					possibleNames :
					arguments.stream().map(Type::getVarName).toList();

			Object2IntMap<String> namesTable = new Object2IntOpenHashMap<>();
			names.forEach(name -> namesTable.put(name, namesTable.getInt(name) + 1));

			for (var entry : namesTable.object2IntEntrySet()) {
				entry.setValue(entry.getIntValue() == 1 ? 0 : 1);
			}

			this.names = names.stream().map(name -> {
				int n = namesTable.getInt(name);
				return n == 0 ? name : name + namesTable.put(name, n + 1);
			}).toList();
		}

		@Override
		public String apply(int index) {
			return names.get(index);
		}
	}


	public boolean equals(IClassArrayType hostClass, String name, Type returnType) {
		return  this.hostClass.equals(hostClass) &&
				this.name.equals(name) &&
				this.returnType.equals(returnType) &&
				this.arguments.isEmpty();
	}

	public boolean equals(IClassArrayType hostClass, String name, Type returnType, @Unmodifiable List<Type> arguments) {
		return  this.hostClass.equals(hostClass) &&
				this.name.equals(name) &&
				this.returnType.equals(returnType) &&
				this.arguments.equals(arguments);
	}

	public boolean equalsIgnoreClass(String name, Type returnType) {
		return  this.name.equals(name) &&
				this.returnType.equals(returnType) &&
				this.arguments.isEmpty();
	}

	public boolean equalsIgnoreClass(IncompleteMethodDescriptor other) {
		return  name.equals(other.name()) &&
				returnType.equals(other.returnType()) &&
				arguments.equals(other.arguments());
	}

	@Override
	public String toString() {
		return String.format("%s %s.%s(%s)", returnType, hostClass, name,
				arguments.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
}
