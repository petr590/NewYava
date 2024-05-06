package x590.newyava.type;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Массив какого-либо другого типа.
 */
public class ArrayType implements ReferenceType {

	private static final Map<Type, ArrayType> ARRAYS_POOL = new HashMap<>();

	private static final List<ClassType> INTERFACES = List.of(ClassType.COMPARABLE, ClassType.SERIALIZABLE);

	@Getter
	private final Type type;

	@Getter
	private final int nestLevel;

	private final String braces;

	private @Nullable String varName;

	public static ArrayType parse(SignatureReader reader) {
		int nestLevel = 0;

		while (reader.next() == '[') {
			nestLevel++;
		}

		return forType(Type.parse(reader.dec()), nestLevel);
	}

	public static ArrayType forType(Type type, int nestLevel) {
		if (type instanceof ArrayType arrayType) {
			return forType(arrayType.getMemberType(), arrayType.nestLevel + nestLevel);
		}

		return nestLevel == 1 ? forType0(type) : new ArrayType(type, nestLevel);
	}

	public static ArrayType forType(Type type) {
		return type instanceof ArrayType arrayType ?
				forType(arrayType.getMemberType(), arrayType.nestLevel + 1) :
				forType0(type);
	}

	private static ArrayType forType0(Type type) {
		return ARRAYS_POOL.computeIfAbsent(type, tp -> new ArrayType(tp, 1));
	}

	public static ArrayType valueOf(String binName) {
		var reader = new SignatureReader(binName);
		var result = parse(reader);
		reader.checkEndForType();
		return result;
	}

	private ArrayType(Type type, int nestLevel) {
		this.type = type;
		this.nestLevel = nestLevel;
		this.braces = "[]".repeat(nestLevel);
	}

	@Override
	public @Nullable ReferenceType getSuperType() {
		return ClassType.OBJECT;
	}

	@Override
	public List<? extends ReferenceType> getInterfaces() {
		return INTERFACES;
	}

	public Type getMemberType(int nestLevel) {
		if (nestLevel == 0)
			return this;

		if (nestLevel < 0)
			throw new IllegalArgumentException("Negative level");

		if (nestLevel > this.nestLevel)
			throw new IllegalArgumentException("Too deep nest level for array " + this);

		return nestLevel == this.nestLevel ? type : forType(type, this.nestLevel - nestLevel);
	}

	/** @return конечный тип, который хранится в массиве. Например,
	 * для {@code int[]} и для {@code int[][]} вернёт {@code int} */
	public Type getMemberType() {
		return type;
	}

	/** @return тип, который хранится в массиве. Например,
	 * для {@code int[]} вернёт {@code int}, а для {@code int[][]} вернёт {@code int[]} */
	public Type getElementType() {
		return nestLevel == 1 ? type : forType(type, nestLevel - 1);
	}

	@Override
	public String getVarName() {
		if (varName != null)
			return varName;

		return varName = nestLevel > 1 ?
				type.getArrVarName() + nestLevel + "dArray" :
				type.getArrVarName() + "Array";
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(type);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.record(type, context).record(braces);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj ||
				obj instanceof ArrayType other &&
				nestLevel == other.nestLevel &&
				type.equals(other.type);
	}

	@Override
	public String toString() {
		return type.toString() + braces;
	}
}
