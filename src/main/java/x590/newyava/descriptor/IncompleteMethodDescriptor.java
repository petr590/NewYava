package x590.newyava.descriptor;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.io.SignatureReader;
import x590.newyava.type.Type;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Дескриптор метода без класса, к которому он принадлежит */
public record IncompleteMethodDescriptor(String name, Type returnType, @Unmodifiable List<Type> arguments) {

	public static IncompleteMethodDescriptor of(String name, String argsAndReturnType) {
		var reader = new SignatureReader(argsAndReturnType);

		List<Type> arguments = Type.parseMethodArguments(reader);
		Type returnType = Type.parseReturnType(reader);
		reader.checkEndForType();

		return new IncompleteMethodDescriptor(name, returnType, Collections.unmodifiableList(arguments));
	}

	public long slots() {
		return MethodDescriptor.slots(arguments);
	}

	public boolean equals(String name, Type returnType, @Unmodifiable List<Type> arguments) {
		return  this.name.equals(name) &&
				this.returnType.equals(returnType) &&
				this.arguments.equals(arguments);
	}

	@Override
	public String toString() {
		return String.format("%s %s(%s)", returnType, name,
				arguments.stream().map(Object::toString).collect(Collectors.joining(", ")));
	}
}
