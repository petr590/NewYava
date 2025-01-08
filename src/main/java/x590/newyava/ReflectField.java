package x590.newyava;

import lombok.Getter;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.lang.reflect.Field;

@Getter
public class ReflectField implements IField {
	private final int modifiers;
	private final FieldDescriptor descriptor, visibleDescriptor;

	public ReflectField(Field field) {
		this.modifiers = field.getModifiers();

		var hostClass = ClassType.valueOf(field.getDeclaringClass());
		var name = field.getName();

		this.descriptor = new FieldDescriptor(hostClass, name, Type.valueOf(field.getType()));
		this.visibleDescriptor = new FieldDescriptor(hostClass, name, Type.valueOf(field.getGenericType()));
	}
}
