package x590.newyava;

import lombok.Getter;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.lang.reflect.Field;

@Getter
public class ReflectField implements IField {
	private final int modifiers;
	private final FieldDescriptor descriptor;

	public ReflectField(Field field) {
		this.modifiers = field.getModifiers();

		this.descriptor = new FieldDescriptor(
				ClassType.valueOf(field.getDeclaringClass()),
				field.getName(),
				Type.valueOf(field.getType())
		);
	}
}
