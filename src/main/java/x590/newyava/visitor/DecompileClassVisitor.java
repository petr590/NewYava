package x590.newyava.visitor;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;
import x590.newyava.Decompiler;
import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.EntryType;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static x590.newyava.Modifiers.*;

public class DecompileClassVisitor extends ClassVisitor {
	private final Decompiler decompiler;

	@Getter
	private int version;

	@Getter
	private int modifiers;

	private String name;

	@Getter
	private ClassType thisType;

	/** Суперкласс. Для {@code java.lang.Object} - сам {@code java.lang.Object}.
	 * Это нужно для избегания проверок на {@code null} */
	@Getter
	private ClassType superType;

	@Getter
	private @Unmodifiable List<ClassType> interfaces;

	private @Nullable String signature;


	private @Nullable String outerClassName;
	private @Nullable String enclosingMethodName;
	private @Nullable String enclosingMethodDesc;

	private final List<DecompileFieldVisitor> fieldVisitors = new ArrayList<>();
	private final List<DecompileMethodVisitor> methodVisitors = new ArrayList<>();
	private final List<DecompilingAnnotation> annotations = new ArrayList<>();

	public DecompileClassVisitor(Decompiler decompiler) {
		super(Opcodes.ASM9);
		this.decompiler = decompiler;
	}

	@Override
	public void visit(int version, int modifiers, String name, @Nullable String signature,
	                  @Nullable String superName, String[] interfaces) {

		this.version = version;
		this.modifiers = modifiers;
		this.name = name;
		this.thisType = ClassType.valueOf(name);
		this.superType = superName == null ? ClassType.OBJECT : ClassType.valueOf(superName);
		this.interfaces = Arrays.stream(interfaces).map(ClassType::valueOf).toList();
		this.signature = signature;

		super.visit(version, modifiers, name, signature, superName, interfaces);
	}

	@Override
	public void visitOuterClass(String owner, String methodName, String methodDesc) {
		super.visitOuterClass(owner, methodName, methodDesc);

		ClassType.checkOrUpdateNested(name, owner, true);

		assert !owner.equals(name) : owner;
		this.outerClassName = owner;
		this.enclosingMethodName = methodName;
		this.enclosingMethodDesc = methodDesc;
	}

	@Override
	public void visitInnerClass(String innerName, String outerName, String innerSimpleName, int modifiers) {
		super.visitInnerClass(innerName, outerName, innerSimpleName, modifiers);

		if (innerName.equals(this.name)) {
			modifiers |= (this.modifiers & ACC_RECORD);

			if ((this.modifiers & ~ACC_SUPER) != (modifiers & ~(ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC))) {
				System.err.printf("Modifiers of class and nested class are not matches: (%s), (%s)\n",
						EntryType.CLASS.modifiersToString(this.modifiers),
						EntryType.CLASS.modifiersToString(modifiers));
			}

			this.modifiers = modifiers;

			if (outerName != null) {
				assert !outerName.equals(name) : outerName;
				this.outerClassName = outerName;
			}
		}

		if (outerName != null) {
			ClassType.checkOrUpdateNested(innerName, outerName);
		}
	}

	@Override
	public FieldVisitor visitField(int modifiers, String name, String descriptor,
	                               @Nullable String signature, @Nullable Object value) {

		super.visitField(modifiers, name, descriptor, signature, value);

		var fieldVisitor = new DecompileFieldVisitor(thisType, modifiers, name, descriptor, signature, value);
		fieldVisitors.add(fieldVisitor);
		return fieldVisitor;
	}

	@Override
	public MethodVisitor visitMethod(int modifiers, String name, String descriptor,
	                                 @Nullable String signature, String[] exceptions) {

		super.visitMethod(modifiers, name, descriptor, signature, exceptions);

		var methodVisitor = new DecompileMethodVisitor(decompiler, thisType, modifiers, name, descriptor, signature, exceptions);
		methodVisitors.add(methodVisitor);
		return methodVisitor;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		var annotation = new DecompilingAnnotation(descriptor);
		annotations.add(annotation);
		return annotation;
	}

	@Override
	public void visitAttribute(Attribute attr) {
		super.visitAttribute(attr);
	}

	public @Nullable ClassType getOuterClassType() {
		return outerClassName == null ? null : ClassType.valueOf(outerClassName);
	}

	public @Nullable MethodDescriptor getEnclosingMethod() {
		return outerClassName == null || enclosingMethodName == null || enclosingMethodDesc == null ? null :
				MethodDescriptor.of(ClassType.valueOf(outerClassName), enclosingMethodName, enclosingMethodDesc);
	}

	public @Unmodifiable List<DecompilingField> getFields() {
		return fieldVisitors.stream().map(DecompilingField::new).toList();
	}

	public @Unmodifiable List<DecompilingMethod> getMethods() {
		return methodVisitors.stream().map(DecompilingMethod::new).toList();
	}

	public @Unmodifiable List<DecompilingAnnotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}
}
