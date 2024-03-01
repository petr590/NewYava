package x590.newyava.visitor;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.context.ClassContext;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.type.Type;

@Getter
public class DecompileFieldVisitor extends FieldVisitor {

	private final int modifiers;
	private final String name, typeName;
	private final @Nullable String signature;
	private final @Nullable Object constantValue;

	public DecompileFieldVisitor(int modifiers, String name, String typeName, @Nullable String signature, @Nullable Object constantValue) {
		super(Opcodes.ASM9);

		this.modifiers = modifiers;
		this.name = name;
		this.typeName = typeName;
		this.signature = signature;
		this.constantValue = constantValue;
	}

	public FieldDescriptor getDescriptor(ClassContext context) {
		return new FieldDescriptor(context.getThisType(), name, Type.valueOf(typeName));
	}
}
