package x590.newyava.visitor;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.constant.Constant;
import x590.newyava.decompilation.operation.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class DecompileFieldVisitor extends FieldVisitor {

	private final int modifiers;
	private final FieldDescriptor descriptor;
	private final @Nullable String signature;
	private final @Nullable Object constantValue;
	private final List<DecompilingAnnotation> annotations = new ArrayList<>();

	public DecompileFieldVisitor(ClassType hostClass, int modifiers, String name, String typeName,
	                             @Nullable String signature, @Nullable Object constantValue) {

		super(Opcodes.ASM9);

		this.modifiers = modifiers;
		this.descriptor = new FieldDescriptor(hostClass, name, Type.valueOf(typeName));
		this.signature = signature;
		this.constantValue = constantValue;
	}

	public @Nullable Operation getInitializer() {
		return constantValue == null ? null : new LdcOperation(Constant.fromObject(constantValue));
	}

	public @Unmodifiable List<DecompilingAnnotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		var annotation = new DecompilingAnnotation(descriptor);
		annotations.add(annotation);
		return annotation;
	}
}
