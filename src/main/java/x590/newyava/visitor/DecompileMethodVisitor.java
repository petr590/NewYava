package x590.newyava.visitor;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.Config;
import x590.newyava.Modifiers;
import x590.newyava.context.ClassContext;
import x590.newyava.decompilation.CodeGraph;
import x590.newyava.decompilation.instruction.*;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.io.SignatureReader;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@Getter
public class DecompileMethodVisitor extends MethodVisitor {

	private final String className;
	private final int modifiers;
	private final String name, descriptor;
	private final @Nullable String signature;
	private final String @Nullable[] exceptions;

	private CodeGraph codeGraph;

	public DecompileMethodVisitor(String className, int modifiers, String name, String descriptor,
	                              @Nullable String signature, String @Nullable[] exceptions) {

		super(Opcodes.ASM9);

		this.className = className;
		this.modifiers = modifiers;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.exceptions = exceptions;
	}

	public MethodDescriptor getDescriptor(ClassContext context) {
		return MethodDescriptor.of(context.getThisType(), name, descriptor);
	}

	public @Unmodifiable List<ReferenceType> getExceptions() {
		return exceptions == null ?
				Collections.emptyList() :
				Arrays.stream(exceptions).map(ReferenceType::valueOf).toList();
	}


	@Override
	public void visitCode() {
		codeGraph = new CodeGraph(this);
	}

	@Override
	public void visitLocalVariable(String name, String descriptor, @Nullable String signature,
	                               Label start, Label end, int slotId) {

		if (!Config.getConfig().isIgnoreVariableTable()) {
			codeGraph.setVariable(slotId, Type.valueOf(descriptor), name, start, end);
		}
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals) {
		codeGraph.initVariables(maxLocals,
				Type.parseMethodArguments(new SignatureReader(descriptor)),
				(modifiers & Modifiers.ACC_STATIC) != 0,
				ReferenceType.valueOf(className));
	}

	@Override
	public void visitInsn(int opcode) {
		codeGraph.addInstruction(switch (opcode) {
			case IRETURN -> ReturnInsn.IRETURN;
			case LRETURN -> ReturnInsn.LRETURN;
			case FRETURN -> ReturnInsn.FRETURN;
			case DRETURN -> ReturnInsn.DRETURN;
			case ARETURN -> ReturnInsn.ARETURN;
			case RETURN  -> ReturnInsn.RETURN;
			case ATHROW  -> ThrowInsn.INSTANCE;
			default -> new JustInsn(opcode);
		});
	}

	@Override
	public void visitIntInsn(int opcode, int operand) {
		codeGraph.addInstruction(new IntInsn(opcode, operand));
	}

	@Override
	public void visitVarInsn(int opcode, int varIndex) {
		codeGraph.addInstruction(new VarInsn(opcode, varIndex));
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
		codeGraph.addInstruction(new MethodInsn(opcode, owner, name, descriptor, isInterface));
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
	public void visitIincInsn(int varIndex, int increment) {
		codeGraph.addInstruction(new IIncInsn(varIndex, increment));
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
