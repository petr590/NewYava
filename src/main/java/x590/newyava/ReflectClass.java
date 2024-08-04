package x590.newyava;

import lombok.Getter;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassArrayType;
import x590.newyava.type.ClassType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public class ReflectClass implements IClass {
	private final int modifiers;

	private final ClassArrayType thisType;
	private final ClassType superType;
	private final @Unmodifiable List<ClassType> interfaces;

	private final Map<FieldDescriptor, ReflectField> fields;
	private final Map<MethodDescriptor, ReflectMethod> methods;

	public ReflectClass(Class<?> clazz) {
		if (clazz.isPrimitive())
			throw new IllegalArgumentException("Class cannot be primitive");

		this.modifiers = clazz.getModifiers();
		this.thisType = ClassArrayType.valueOf(clazz);

		var superclass = clazz.getSuperclass();
		this.superType = superclass == null ? ClassType.OBJECT : ClassType.valueOf(superclass);

		this.interfaces = Arrays.stream(clazz.getInterfaces()).map(ClassType::valueOf).toList();

		this.fields = Arrays.stream(clazz.getFields()).map(ReflectField::new)
				.collect(Collectors.toMap(ReflectField::getDescriptor, field -> field));

		this.methods = Arrays.stream(clazz.getMethods()).map(ReflectMethod::new)
				.collect(Collectors.toMap(ReflectMethod::getDescriptor, method -> method));
	}

	@Override
	public Optional<? extends IField> findField(FieldDescriptor descriptor) {
		return Optional.ofNullable(fields.get(descriptor));
	}

	@Override
	public Optional<? extends IMethod> findMethod(MethodDescriptor descriptor) {
		return Optional.ofNullable(methods.get(descriptor));
	}
}
