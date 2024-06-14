package x590.newyava.visitor;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;
import x590.newyava.Decompiler;
import x590.newyava.Modifiers;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.annotation.DefaultValue;
import x590.newyava.decompilation.CodeGraph;
import x590.newyava.decompilation.instruction.*;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Getter
public class DecompileMethodVisitor extends MethodVisitor {
	@Getter(AccessLevel.NONE)
	private final Decompiler decompiler;

	private final int modifiers;
	private final MethodDescriptor descriptor;
	private final @Nullable String signature;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final List<DecompilingAnnotation> annotations = new ArrayList<>();

	private @Nullable DefaultValue defaultValue;

	// Является @Nullable, но это не указывается из-за кучи предупреждений
	private CodeGraph codeGraph;

	/** Слот, на котором начинаются переменные, объявленные в методе */
	private final int localVarsStart;

	public DecompileMethodVisitor(Decompiler decompiler, ClassType hostClass, int modifiers, String name,
	                              String argsAndReturnType, @Nullable String signature, String @Nullable[] exceptions) {

		super(Opcodes.ASM9);

		this.decompiler = decompiler;

		this.modifiers = modifiers;
		this.descriptor = MethodDescriptor.of(hostClass, name, argsAndReturnType);
		this.signature = signature;
		this.exceptions = exceptions == null ?
				Collections.emptyList() :
				Arrays.stream(exceptions).map(ReferenceType::valueOf).toList();

		this.localVarsStart =
				((modifiers & ACC_STATIC) == 0 ? 1 : 0) +
				descriptor.arguments().stream().mapToInt(type -> type.getSize().slots()).sum();
	}

	public @Nullable CodeGraph getCodeGraph() {
		return codeGraph;
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

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return defaultValue = new DefaultValue();
	}

	// ----------------------------------------------------- code ------------------------------------------------------

	@Override
	public void visitCode() {
		codeGraph = new CodeGraph(this);
	}

	@Override
	public void visitLocalVariable(String name, String descriptor, @Nullable String signature,
	                               Label start, Label end, int slotId) {

		if (!decompiler.getConfig().ignoreVariableTable()) {
			codeGraph.setVariable(slotId, Type.valueOf(descriptor), name, start, end);
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		codeGraph.initVariables(maxLocals, descriptor, (modifiers & Modifiers.ACC_STATIC) != 0);
	}

	@Override
	public void visitInsn(int opcode) {
		codeGraph.addInstruction(switch (opcode) {
			case IRETURN -> TerminalInsn.IRETURN;
			case LRETURN -> TerminalInsn.LRETURN;
			case FRETURN -> TerminalInsn.FRETURN;
			case DRETURN -> TerminalInsn.DRETURN;
			case ARETURN -> TerminalInsn.ARETURN;
			case RETURN  -> TerminalInsn.RETURN;
			case ATHROW  -> TerminalInsn.ATHROW;
			default -> JustInsn.of(opcode);
		});
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		codeGraph.addInstruction(new IntInsn(opcode, operand));
	}

	@Override
	public void visitVarInsn(int opcode, int slotId) {
		codeGraph.addInstruction(new VarInsn(opcode, slotId));
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		codeGraph.addInstruction(new TypeInsn(opcode, type));
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
		codeGraph.addInstruction(new FieldInsn(opcode, owner, name, descriptor));
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
		codeGraph.addInstruction(new MethodInsn(opcode, owner, name, descriptor));
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
	                                   Object... bootstrapMethodArguments) {

		codeGraph.addInstruction(new InvokeDynamicInsn(name, descriptor,
				bootstrapMethodHandle, bootstrapMethodArguments));
	}

	@Override
	public void visitJumpInsn(int opcode, Label label) {
		codeGraph.addInstruction(new JumpInsn(opcode, label));
	}

	@Override
	public void visitLdcInsn(Object value) {
		codeGraph.addInstruction(new LdcInsn(value));
	}

	@Override
	public void visitIincInsn(int slotId, int increment) {
		codeGraph.addInstruction(new IIncInsn(slotId, increment));
	}

	@Override
	public void visitTableSwitchInsn(int min, int max, Label defaultLabel, Label... labels) {
		codeGraph.addInstruction(new SwitchInsn(min, max, defaultLabel, labels));
	}

	@Override
	public void visitLookupSwitchInsn(Label defaultLabel, int[] keys, Label[] labels) {
		codeGraph.addInstruction(new SwitchInsn(defaultLabel, keys, labels));
	}

	@Override
	public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
		codeGraph.addInstruction(new MultiANewArrayInsn(descriptor, numDimensions));
	}

	@Override
	public void visitLabel(Label label) {
		codeGraph.addLabel(label);
	}
}
