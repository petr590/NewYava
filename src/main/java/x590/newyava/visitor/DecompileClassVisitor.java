package x590.newyava.visitor;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;
import x590.newyava.Decompiler;
import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.Util;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.modifiers.EntryType;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;

import java.util.*;

import static x590.newyava.modifiers.Modifiers.*;

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

	private final Map<FieldDescriptor, DecompileFieldVisitor> fieldVisitors = new LinkedHashMap<>();
	private final List<DecompileMethodVisitor> methodVisitors = new ArrayList<>();
	private final List<DecompilingAnnotation> annotations = new ArrayList<>();
	private final List<ClassType> permittedSubclasses = new ArrayList<>();

	@Getter
	private @Nullable ModuleInfo moduleInfo;

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
	}

	@Override
	public void visitOuterClass(String owner, String methodName, String methodDesc) {
		ClassType.checkOrUpdateNested(name, owner, true);

		assert !owner.equals(name) : owner;
		this.outerClassName = owner;
		this.enclosingMethodName = methodName;
		this.enclosingMethodDesc = methodDesc;
	}

	private static final int
			IGNORED_FORMAL_MODIFIERS = ACC_PUBLIC | ACC_SUPER | ACC_DEPRECATED | ACC_RECORD,
			IGNORED_INNER_MODIFIERS = ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED | ACC_STATIC;

	@Override
	public void visitInnerClass(String innerName, String outerName, String innerSimpleName, int innerModifiers) {
		if (innerName.equals(this.name)) {
			int formalModifiers = modifiers;

			if ((formalModifiers & ~IGNORED_FORMAL_MODIFIERS) != (innerModifiers & ~IGNORED_INNER_MODIFIERS)) {
				System.err.printf("Modifiers of class and nested class are not matches: 0x%x (%s), 0x%x (%s): %s\n",
						formalModifiers, EntryType.CLASS.modifiersToString(formalModifiers),
						innerModifiers, EntryType.CLASS.modifiersToString(innerModifiers),
						innerName);
			}

			modifiers |= innerModifiers;

			if ((innerModifiers & ACC_PROTECTED) != 0) {
				modifiers &= ~ACC_PUBLIC;
			}

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
	public RecordComponentVisitor visitRecordComponent(String name, String typeName, @Nullable String signature) {
		var fieldVisitor = fieldVisitors.computeIfAbsent(
				FieldDescriptor.of(thisType, name, typeName),
				desc -> new DecompileFieldVisitor(desc, signature)
		);

		return fieldVisitor.getRecordComponentVisitor();
	}

	@Override
	public FieldVisitor visitField(int modifiers, String name, String typeName,
	                               @Nullable String signature, @Nullable Object value) {

		var fieldVisitor = fieldVisitors.computeIfAbsent(
				FieldDescriptor.of(thisType, name, typeName),
				desc -> new DecompileFieldVisitor(desc, signature)
		);

		fieldVisitor.setModifiers(modifiers);
		fieldVisitor.setConstantValue(value);

		return fieldVisitor;
	}

	@Override
	public MethodVisitor visitMethod(int modifiers, String name, String descriptor,
	                                 @Nullable String signature, String[] exceptions) {

		return Util.addAndGetBack(methodVisitors,
				new DecompileMethodVisitor(decompiler, thisType, modifiers, name, descriptor, signature, exceptions));
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return Util.addAndGetBack(annotations, new DecompilingAnnotation(descriptor));
	}

	@Override
	public void visitPermittedSubclass(String permittedSubclass) {
		permittedSubclasses.add(ClassType.valueOf(permittedSubclass));
	}

	@Override
	public ModuleVisitor visitModule(String name, int access, String version) {
		return moduleInfo = new ModuleInfo(name, access);
	}

	public @Nullable ClassType getOuterClassType() {
		return outerClassName == null ? null : ClassType.valueOf(outerClassName);
	}

	public @Nullable MethodDescriptor getEnclosingMethod() {
		return outerClassName == null || enclosingMethodName == null || enclosingMethodDesc == null ? null :
				MethodDescriptor.of(ClassType.valueOf(outerClassName), enclosingMethodName, enclosingMethodDesc);
	}

	public @Unmodifiable List<DecompilingField> getFields() {
		return fieldVisitors.values().stream().map(DecompilingField::new).toList();
	}

	public @Unmodifiable List<DecompilingMethod> getMethods() {
		return methodVisitors.stream().map(DecompilingMethod::new).toList();
	}

	public @Unmodifiable List<DecompilingAnnotation> getAnnotations() {
		return Collections.unmodifiableList(annotations);
	}

	public @Unmodifiable List<ClassType> getPermittedSubclasses() {
		return Collections.unmodifiableList(permittedSubclasses);
	}
}
