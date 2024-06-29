package x590.newyava.context;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.Config;
import x590.newyava.DecompilingClass;
import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;

import java.util.List;
import java.util.Optional;

/**
 * Делегирует получение свойств другому контексту.
 */
@RequiredArgsConstructor
public class ContextProxy implements Context {
	protected final Context context;

	@Override
	public Config getConfig() {
		return context.getConfig();
	}

	@Override
	public int getClassModifiers() {
		return context.getClassModifiers();
	}

	@Override
	public ClassType getThisType() {
		return context.getThisType();
	}

	@Override
	public ClassType getSuperType() {
		return context.getSuperType();
	}

	@Override
	public @Nullable @Unmodifiable List<DecompilingField> getRecordComponents() {
		return context.getRecordComponents();
	}

	@Override
	public boolean imported(ClassType classType) {
		return context.imported(classType);
	}

	@Override
	public Optional<DecompilingField> findField(FieldDescriptor descriptor) {
		return context.findField(descriptor);
	}

	@Override
	public Optional<DecompilingMethod> findMethod(MethodDescriptor descriptor) {
		return context.findMethod(descriptor);
	}

	@Override
	public Optional<DecompilingClass> findClass(@Nullable ClassType classType) {
		return context.findClass(classType);
	}
}
