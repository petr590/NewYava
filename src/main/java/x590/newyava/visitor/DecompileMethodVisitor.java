package x590.newyava.visitor;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.*;
import x590.newyava.Decompiler;
import x590.newyava.io.SignatureReader;
import x590.newyava.type.*;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.annotation.DefaultValue;
import x590.newyava.decompilation.code.CodeGraph;
import x590.newyava.decompilation.instruction.*;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.modifiers.Modifiers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

/**
 * Визитор метода. Собирает все данные о методе и предоставляет к ним доступ.
 */
@Getter
public class DecompileMethodVisitor extends MethodVisitor {
	@Getter(AccessLevel.NONE)
	private final Decompiler decompiler;

	private final int modifiers;

	private final MethodDescriptor descriptor;
	private final @Nullable MethodDescriptor visibleDescriptor;

	private final Signature signature;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final List<DecompilingAnnotation> annotations = new ArrayList<>();

	private @Nullable DefaultValue defaultValue;

	// Является @Nullable, но это не указывается из-за кучи предупреждений
	@SuppressWarnings("all")
	private CodeGraph codeGraph;

	/** Список размеров всех переменных, объявленных в сигнатуре метода (в том числе this). */
	private final IntList argumentsSizes;

	public DecompileMethodVisitor(Decompiler decompiler, ClassType hostClass, int modifiers, String name,
	                              String argsAndReturnType, @Nullable String signatureStr, String @Nullable[] exceptionNames) {

		super(Opcodes.ASM9);
		this.decompiler = decompiler;

		this.modifiers = modifiers;
		this.descriptor = MethodDescriptor.of(hostClass, name, argsAndReturnType);

		var signature = Signature.EMPTY;

		List<ReferenceType> exceptions = exceptionNames == null ?
				Collections.emptyList() :
				Arrays.stream(exceptionNames).map(ReferenceType::valueOf).toList();

		if (signatureStr != null) {
			var reader = new SignatureReader(signatureStr);

			signature = Signature.parseOrEmpty(reader);

			var args = Type.parseMethodArguments(reader);
			var returnType = Type.parseReturnType(reader);

			this.visibleDescriptor = new MethodDescriptor(hostClass, name, returnType, args);


			List<ReferenceType> genericExceptions = new ArrayList<>();

			while (!reader.isEnd() && reader.eat('^')) {
				genericExceptions.add(ReferenceType.parse(reader));
			}

			if (!genericExceptions.isEmpty()) {
				exceptions = genericExceptions;
			}

			reader.checkEndForType();

		} else {
			visibleDescriptor = null;
		}

		this.signature = signature;
		this.exceptions = exceptions;


		boolean nonStatic = (modifiers & ACC_STATIC) == 0;

		this.argumentsSizes = new IntArrayList(descriptor.arguments().size() + (nonStatic ? 1 : 0));

		if (nonStatic) {
			argumentsSizes.add(TypeSize.WORD.slots());
		}

		for (var argType : descriptor.arguments()) {
			argumentsSizes.add(argType.getSize().slots());
		}
	}

	public @Nullable CodeGraph getCodeGraph() {
		return codeGraph;
	}


	/* ------------------------------------------------ Annotations ------------------------------------------------- */

	public List<DecompilingAnnotation> getAnnotations() {
		return annotations;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		return Utils.addAndGetBack(annotations, new DecompilingAnnotation(descriptor));
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		return defaultValue = new DefaultValue();
	}

//	@Override
//	public void visitParameter(String name, int access) {
//		System.out.printf("visitParameter(%s, %d)\n", name, access);
//	}
//
//	@Override
//	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//		System.out.printf("visitTypeAnnotation(%d, %s, %s, %b)\n", typeRef, typePath, descriptor, visible);
//		return null;
//	}
//
//	@Override
//	public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
//		System.out.printf("visitAnnotableParameterCount(%d, %b)\n", parameterCount, visible);
//	}
//
//	@Override
//	public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
//		System.out.printf("visitParameterAnnotation(%d, %s, %b)\n", parameter, descriptor, visible);
//		return null;
//	}
//
//	@Override
//	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//		System.out.printf("visitInsnAnnotation(%d, %s, %s, %b)\n", typeRef, typePath, descriptor, visible);
//		return null;
//	}
//
//	@Override
//	public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
//		System.out.printf("visitTryCatchAnnotation(%d, %s, %s, %b)\n", typeRef, typePath, descriptor, visible);
//		return null;
//	}
//
//	@Override
//	public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
//	                                                      int[] index, String descriptor, boolean visible) {
//
//		System.out.printf("visitLocalVariableAnnotation(%d, %s, %s, %b)\n", typeRef, typePath, descriptor, visible);
//		return null;
//	}

	/* ---------------------------------------------------- Code ---------------------------------------------------- */

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

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, @Nullable String type) {
		codeGraph.addTryCatchBlock(
				start, end, handler,
				type == null ? null : ClassType.valueOf(type)
		);
	}
}
