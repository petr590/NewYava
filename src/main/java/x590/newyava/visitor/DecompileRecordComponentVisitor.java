package x590.newyava.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import x590.newyava.Util;
import x590.newyava.annotation.DecompilingAnnotation;

import java.util.Set;

public class DecompileRecordComponentVisitor extends RecordComponentVisitor {
	private final Set<DecompilingAnnotation> annotations;

	DecompileRecordComponentVisitor(Set<DecompilingAnnotation> annotations) {
		super(Opcodes.ASM9);
		this.annotations = annotations;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return Util.addAndGetBack(annotations, new DecompilingAnnotation(descriptor));
	}
}
