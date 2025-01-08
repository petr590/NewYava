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
import x590.newyava.io.SignatureReader;
import x590.newyava.type.ReferenceType;
import x590.newyava.util.Utils;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.constant.Constant;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.descriptor.FieldDescriptor;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Визитор поля. Собирает все данные о поле и предоставляет к ним доступ.
 */
@Getter
public class DecompileFieldVisitor extends FieldVisitor {
	@Setter(AccessLevel.PACKAGE)
	private int modifiers;

	private final FieldDescriptor descriptor, visibleDescriptor;

	@Setter(AccessLevel.PACKAGE)
	private @Nullable Object constantValue;

	private final Set<DecompilingAnnotation> annotations = new LinkedHashSet<>();

	@Getter(lazy = true)
	private final RecordComponentVisitor recordComponentVisitor = new DecompileRecordComponentVisitor(annotations);

	public DecompileFieldVisitor(FieldDescriptor descriptor, @Nullable String signatureStr) {
		super(Opcodes.ASM9);

		this.descriptor = descriptor;
		this.visibleDescriptor = signatureStr == null ? descriptor :
				new FieldDescriptor(
						descriptor.hostClass(), descriptor.name(),
						SignatureReader.parse(signatureStr, ReferenceType::parse)
				);
	}

	public @Nullable Object getConstantValue() {
		return constantValue;
	}

	/** @return операцию инициализации статического поля, если она есть.
	 * Для нестатических полей всегда возвращает {@code null}. */
	public @Nullable Operation getInitializer() {
		return constantValue != null && (modifiers & Opcodes.ACC_STATIC) != 0 ?
				new LdcOperation(Constant.fromObject(constantValue)) : null;
	}

	public @Unmodifiable Set<DecompilingAnnotation> getAnnotations() {
		return Collections.unmodifiableSet(annotations);
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return Utils.addAndGetBack(annotations, new DecompilingAnnotation(descriptor));
	}
}
