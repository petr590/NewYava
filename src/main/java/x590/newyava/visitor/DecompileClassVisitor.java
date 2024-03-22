package x590.newyava.visitor;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;
import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.EntryType;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.context.ClassContext;
import x590.newyava.exception.DecompilationException;
import x590.newyava.type.ClassType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static x590.newyava.Modifiers.*;

public class DecompileClassVisitor extends ClassVisitor {

	@Getter
	private int version;

	@Getter
	private int modifiers;

	private String name, superName;
	private String[] interfaces;
	private @Nullable String signature;


	private @Nullable String outerClassName;


	private @Nullable String enclosingClassName;
	private @Nullable String enclosingMethodName;
	private @Nullable String enclosingMethodDesc;

	private final List<DecompileFieldVisitor> fieldVisitors = new ArrayList<>();
	private final List<DecompileMethodVisitor> methodVisitors = new ArrayList<>();
	private final List<DecompilingAnnotation> annotations = new ArrayList<>();

	public DecompileClassVisitor() {
		super(Opcodes.ASM9);
	}

	@Override
	public void visit(int version, int modifiers, String name,
	                  @Nullable String signature, String superName, String[] interfaces) {

		this.version = version;
		this.modifiers = modifiers;
		this.name = name;
		this.superName = superName;
		this.interfaces = interfaces;
		this.signature = signature;

		super.visit(version, modifiers, name, signature, superName, interfaces);
	}

	@Override
	public void visitOuterClass(String owner, String methodName, String methodDesc) {
		this.enclosingClassName = owner;
		this.enclosingMethodName = methodName;
		this.enclosingMethodDesc = methodDesc;

		super.visitOuterClass(owner, methodName, methodDesc);
	}

	@Override
	public void visitInnerClass(String innerName, String outerName, String innerSimpleName, int modifiers) {
		if (innerName.equals(this.name)) {
			this.outerClassName = outerName;

			modifiers |= (this.modifiers & ACC_RECORD);

			if ((this.modifiers & ~ACC_SUPER) != (modifiers & ~(ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC))) {
				System.err.printf("Modifiers of class and nested class are not matches: (%s), (%s)\n",
						EntryType.CLASS.modifiersToString(this.modifiers),
						EntryType.CLASS.modifiersToString(modifiers));
			}

			this.modifiers = modifiers;
		}

		if (outerName == null) {
			outerName = innerName;

			if (innerSimpleName != null && outerName.endsWith(innerSimpleName)) {
				outerName = outerName.substring(0, outerName.length() - innerSimpleName.length());
			}

			int i = outerName.length() - 1;
			for (; i > 0; i--) {
				if (outerName.charAt(i) == '$') {
					break;
				} else if (!Character.isDigit(outerName.charAt(i))) {
					throw new DecompilationException("Invalid inner name of anonymous class " + innerName);
				}
			}

			outerName = outerName.substring(0, i);
		}

		ClassType.checkOrUpdateNested(innerName, outerName);

		super.visitInnerClass(innerName, outerName, innerSimpleName, modifiers);
	}

	@Override
	public FieldVisitor visitField(int modifiers, String name, String descriptor,
	                               @Nullable String signature, @Nullable Object value) {

		super.visitField(modifiers, name, descriptor, signature, value);

		var fieldVisitor = new DecompileFieldVisitor(modifiers, name, descriptor, signature, value);
		fieldVisitors.add(fieldVisitor);
		return fieldVisitor;
	}

	@Override
	public MethodVisitor visitMethod(int modifiers, String name, String descriptor,
	                                 @Nullable String signature, String[] exceptions) {

		super.visitMethod(modifiers, name, descriptor, signature, exceptions);

		var methodVisitor = new DecompileMethodVisitor(this.name, modifiers, name, descriptor, signature, exceptions);
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

	@Override
	public void visitEnd() {
		super.visitEnd();
	}

	public ClassType getThisType() {
		return ClassType.valueOf(name);
	}

	public ClassType getSuperType() {
		return ClassType.valueOf(superName);
	}

	public @Unmodifiable List<ClassType> getInterfaces() {
		return Arrays.stream(interfaces).map(ClassType::valueOf).toList();
	}

	public @Unmodifiable List<DecompilingField> getFields(ClassContext context) {
		return fieldVisitors.stream().map(visitor -> new DecompilingField(visitor, context)).toList();
	}

	public @Unmodifiable List<DecompilingMethod> getMethods(ClassContext context) {
		return methodVisitors.stream().map(visitor -> new DecompilingMethod(visitor, context)).toList();
	}

	public @Unmodifiable List<DecompilingAnnotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}
}
