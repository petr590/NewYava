package x590.newyava.visitor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import x590.newyava.Util;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.constant.Constant;
import x590.newyava.decompilation.operation.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.FieldDescriptor;

import java.util.*;

@Getter
public class DecompileFieldVisitor extends FieldVisitor {
	@Setter(AccessLevel.PACKAGE)
	private int modifiers;

	private final FieldDescriptor descriptor;
	private final @Nullable String signature;

	@Setter(AccessLevel.PACKAGE)
	private @Nullable Object constantValue;

	private final Set<DecompilingAnnotation> annotations = new LinkedHashSet<>();

	@Getter(lazy = true)
	private final RecordComponentVisitor recordComponentVisitor = new DecompileRecordComponentVisitor(annotations);

	public DecompileFieldVisitor(FieldDescriptor descriptor, @Nullable String signature) {
		super(Opcodes.ASM9);

		this.descriptor = descriptor;
		this.signature = signature;
	}

	public @Nullable Operation getInitializer() {
		return constantValue == null ? null : new LdcOperation(Constant.fromObject(constantValue));
	}

	public @Unmodifiable Set<DecompilingAnnotation> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return Util.addAndGetBack(annotations, new DecompilingAnnotation(descriptor));
	}
}
