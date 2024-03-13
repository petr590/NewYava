package x590.newyava.context;

import lombok.RequiredArgsConstructor;
import x590.newyava.DecompilingField;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;

import java.util.Optional;

/**
 * Делегирует получение основных свойств другому контексту.
 */
@RequiredArgsConstructor
public class DelegatingContext implements Context {
	protected final Context context;

	@Override
	public int getClassModifiers() {
		return context.getClassModifiers();
	}

	@Override
	public ReferenceType getThisType() {
		return context.getThisType();
	}

	@Override
	public ClassType getSuperType() {
		return context.getSuperType();
	}

	@Override
	public boolean imported(ClassType classType) {
		return context.imported(classType);
	}

	@Override
	public Optional<DecompilingField> findField(FieldDescriptor descriptor) {
		return context.findField(descriptor);
	}
}
