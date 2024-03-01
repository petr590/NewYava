package x590.newyava.descriptor;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.Importable;
import x590.newyava.Literals;
import x590.newyava.context.ClassContext;
import x590.newyava.decompilation.variable.Variable;
import x590.newyava.exception.InvalidDescriptorException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public record MethodDescriptor(
		ReferenceType hostClass,
		String name,
		Type returnType,
		@Unmodifiable List<Type> arguments
) implements Importable {

	private static final String
			CONSTRUCTOR = "<init>",
			STATIC_INITIALIZER = "<clinit>";

	public MethodDescriptor {
		switch (name) {
			case CONSTRUCTOR -> {
				if (returnType != PrimitiveType.VOID)
					throw new InvalidDescriptorException("Constructor has wrong return type: " + returnType);
			}

			case STATIC_INITIALIZER -> {
				if (returnType != PrimitiveType.VOID)
					throw new InvalidDescriptorException("Static initializer has wrong return type: " + returnType);

				if (!arguments.isEmpty())
					throw new InvalidDescriptorException("Static initializer has arguments: " + arguments);
			}
		}
	}

	public static MethodDescriptor of(ReferenceType hostClass, String name, String argsAndReturnType) {
		var reader = new SignatureReader(argsAndReturnType);

		List<Type> arguments = Type.parseMethodArguments(reader);
		Type returnType = Type.parseReturnType(reader);
		reader.checkEndForType();

		return new MethodDescriptor(hostClass, name, returnType, Collections.unmodifiableList(arguments));
	}

	public boolean isConstructor() {
		return name.equals(CONSTRUCTOR);
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(returnType).addImportsFor(arguments);
	}

	public void write(DecompilationWriter out, ClassContext context, boolean isStatic, @Nullable List<Variable> variables) {
		switch (name) {
			case STATIC_INITIALIZER -> {
				out.record(Literals.LIT_STATIC);
				return;
			}

			case CONSTRUCTOR -> out.record(hostClass, context);
			default -> out.recordsp(returnType, context).record(name);
		}

		int offset = isStatic ? 0 : 1; // skip "this"

		IntFunction<String> nameGetter = variables != null ?
				i -> variables.get(i + offset).name() :
				new NameGetter();

		out.record('(').record(arguments, ", ",
				(type, i) -> out.recordsp(type, context).record(nameGetter.apply(i))
		).record(')');
	}

	private class NameGetter implements IntFunction<String> {

		private final Object2IntMap<String> namesTable = new Object2IntArrayMap<>();

		@Override
		public String apply(int index) {
			String name = arguments.get(index).getVarName();
			int n = namesTable.getInt(name);
			namesTable.put(name, n + 1);

			return n == 0 ? name : name + n;
		}
	}


	@Override
	public String toString() {
		return String.format("%s %s.%s(%s)", returnType, hostClass, name,
				arguments.stream().map(Objects::toString).collect(Collectors.joining(", ")));
	}
}
