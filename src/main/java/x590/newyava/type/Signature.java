package x590.newyava.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;

import java.util.Collections;
import java.util.List;

/**
 * Сигнатура класса или метода, такая как {@code <T>}, {@code <T extends X>} или , {@code <T super X>}.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Signature implements ContextualWritable, Importable {
	public static Signature parse(SignatureReader reader) {
		return new Signature(Collections.unmodifiableList(
				IClassType.parseParameters(reader, TypeParameter::parse)
		));
	}

	public static Signature parseOrEmpty(SignatureReader reader) {
		return reader.eat('<') ? parse(reader.dec()) : EMPTY;
	}

	public static final Signature EMPTY = new Signature(Collections.emptyList());

	private final @Unmodifiable List<TypeParameter> parameters;

	public boolean isEmpty() {
		return parameters.isEmpty();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(parameters);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (!isEmpty()) {
			out.record('<').record(parameters, context, ", ").record('>');
		}
	}
}
