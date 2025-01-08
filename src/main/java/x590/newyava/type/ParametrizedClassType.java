package x590.newyava.type;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParametrizedClassType implements IClassType {
	public static ParametrizedClassType valueOf(ClassType baseClass, List<ReferenceType> parameters) {
		return new ParametrizedClassType(baseClass, null, Collections.unmodifiableList(parameters));
	}

	public static ParametrizedClassType valueOf(ClassType baseClass, IClassType outerClass, List<ReferenceType> parameters) {
		return new ParametrizedClassType(baseClass, outerClass, Collections.unmodifiableList(parameters));
	}

	private final ClassType baseClass;
	private final @Nullable IClassType outerClass;
	private final @Unmodifiable List<ReferenceType> parameters;

	@Override
	public ClassType base() {
		return baseClass;
	}

	@Override
	public String getBinName() {
		return baseClass.getBinName();
	}

	@Override
	public String getClassBinName() {
		return baseClass.getClassBinName();
	}

	@Override
	public String getVarName() {
		return baseClass.getVarName();
	}

	@Override
	public @Nullable ReferenceType getSuperType() {
		return baseClass.getSuperType();
	}

	@Override
	public @Unmodifiable List<? extends ReferenceType> getInterfaces() {
		return baseClass.getInterfaces();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(baseClass).addImportsFor(parameters);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (outerClass != null) {
			out.record(outerClass, context).record('.').record(baseClass.getSimpleName());
		} else {
			out.record(baseClass, context);
		}

		if (!parameters.isEmpty()) {
			out.record('<').record(parameters, context, ", ").record('>');
		}
	}

	@Override
	public String toString() {
		return baseClass + "<" + parameters.stream().map(Object::toString).collect(Collectors.joining(", ")) + ">";
	}
}
