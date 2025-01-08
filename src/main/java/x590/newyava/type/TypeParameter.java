package x590.newyava.type;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;
import x590.newyava.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeParameter implements ContextualWritable, Importable {
	private final String name;

	private final ReferenceType superType;

	private final @Unmodifiable List<ReferenceType> interfaces;

	public static TypeParameter parse(SignatureReader reader) {
		var name = new StringBuilder();

		while (!reader.eat(':')) {
			name.append(reader.next());
		}

		ReferenceType superType = ClassType.OBJECT;
		List<ReferenceType> interfaces = new ArrayList<>();

		if (reader.eat(':')) {
			reader.dec();
		} else {
			superType = ReferenceType.parse(reader);
		}

		while (reader.eat(':')) {
			interfaces.add(ReferenceType.parse(reader));
		}

		return new TypeParameter(name.toString(), superType, Collections.unmodifiableList(interfaces));
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(superType).addImportsFor(interfaces);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(name);

		var superTypes = superType.equals(ClassType.OBJECT) ?
				interfaces :
				Utils.addBefore(superType, interfaces);

		if (!superTypes.isEmpty()) {
			out.record(" extends ").record(superTypes, context, " & ");
		}
	}
}
