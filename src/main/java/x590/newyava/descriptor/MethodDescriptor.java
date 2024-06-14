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
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;
import x590.newyava.type.TypeSize;

import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/** Дескриптор метода */
public record MethodDescriptor(
		ReferenceType hostClass,
		String name,
		Type returnType,
		@Unmodifiable List<Type> arguments,
		int fromSlot
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

	public MethodDescriptor(ReferenceType hostClass, String name, Type returnType, @Unmodifiable List<Type> arguments) {
		this(hostClass, name, returnType, arguments, 0);
	}

	public MethodDescriptor(ReferenceType hostClass, String name, Type returnType) {
		this(hostClass, name, returnType, List.of());
	}

	public static MethodDescriptor of(ReferenceType hostClass, String name, String argsAndReturnType) {
		var reader = new SignatureReader(argsAndReturnType);

		List<Type> arguments = Type.parseMethodArguments(reader);
		Type returnType = Type.parseReturnType(reader);
		reader.checkEndForType();

		return new MethodDescriptor(hostClass, name, returnType, Collections.unmodifiableList(arguments));
	}

	public static MethodDescriptor of(Handle handle) {
		return of(ReferenceType.valueOf(handle.getOwner()), handle.getName(), handle.getDesc());
	}

	public static int slots(@Unmodifiable List<Type> arguments) {
		return arguments.stream().mapToInt(type -> type.getSize().slots()).sum();
	}

	public int slots() {
		return slots(arguments);
	}

	/** @return Новый дескриптор с аргументами начиная с {@code from} до конца */
	public MethodDescriptor slice(int from) {
		return slice(from, arguments.size());
	}

	/** @return Новый дескриптор с аргументами начиная с {@code from} до {@code to} не включительно.
	 * Если {@code from} равен 0, а {@code to} равен количеству аргументов, возвращает {@code this} */
	public MethodDescriptor slice(int from, int to) {
		return from == 0 && to == arguments.size() ? this :
				new MethodDescriptor(hostClass, name, returnType, arguments.subList(from, to), fromSlot + from);
	}


	public boolean isConstructor() {
		return name.equals(INIT);
	}

	public boolean isStaticInitializer() {
		return name.equals(CLINIT);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(returnType).addImportsFor(arguments);
	}

	public void write(DecompilationWriter out, Context context, boolean isStatic,
	                  @Nullable List<Variable> variables) {

		switch (name) {
			case CLINIT -> {
				out.record(Literals.LIT_STATIC);
				return;
			}

			case INIT -> out.record(hostClass, context);
			default -> out.recordSp(returnType, context).record(name);
		}

		IntFunction<String> nameGetter = variables != null ?
				new NameGetter(variables.subList(fromSlot, variables.size()), isStatic) :
				new NameGenerator();

		out.record('(').record(arguments, ", ",
				(type, i) -> out.recordSp(type, context).record(nameGetter.apply(i))
		).record(')');
	}

	private class NameGetter implements IntFunction<String> {
		private final List<Variable> variables;
		private int offset;

		public NameGetter(List<Variable> variables, boolean isStatic) {
			this.variables = variables;
			this.offset = isStatic ? 0 : 1;
		}

		@Override
		public String apply(int index) {
			String name = variables.get(index + offset).getName();

			if (arguments.get(index).getSize() == TypeSize.LONG)
				offset++;

			return name;
		}
	}

	private class NameGenerator implements IntFunction<String> {
		private final List<String> names;

		public NameGenerator() {
			Object2IntMap<String> namesTable = new Object2IntOpenHashMap<>();

			arguments.stream().map(Type::getVarName)
					.forEach(name -> namesTable.put(name, namesTable.getInt(name) + 1));

			for (var entry : namesTable.object2IntEntrySet()) {
				entry.setValue(entry.getIntValue() == 1 ? 0 : 1);
			}

			this.names = arguments.stream().map(Type::getVarName)
					.map(name -> {
						int n = namesTable.getInt(name);
						return n == 0 ? name : name + namesTable.put(name, n + 1);
					}).toList();
		}

		@Override
		public String apply(int index) {
			return names.get(index);
		}
	}


	public boolean equals(ReferenceType hostClass, String name, Type returnType) {
		return  this.hostClass.equals(hostClass) &&
				this.name.equals(name) &&
				this.returnType.equals(returnType) &&
				this.arguments.isEmpty();
	}

	public boolean equals(ReferenceType hostClass, String name, Type returnType, @Unmodifiable List<Type> arguments) {
		return  this.hostClass.equals(hostClass) &&
				this.name.equals(name) &&
				this.returnType.equals(returnType) &&
				this.arguments.equals(arguments);
	}

	@Override
	public String toString() {
		return String.format("%s %s.%s(%s)", returnType, hostClass, name,
				arguments.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
}
