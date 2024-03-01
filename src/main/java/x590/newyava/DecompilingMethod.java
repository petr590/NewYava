package x590.newyava;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.decompilation.CodeGraph;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.DisassemblingException;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ReferenceType;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.List;

import static x590.newyava.Literals.*;
import static x590.newyava.Modifiers.*;

public class DecompilingMethod implements ContextualWritable, Importable {

	private final int modifiers;
	private final MethodDescriptor descriptor;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final @Nullable CodeGraph codeGraph;

	public DecompilingMethod(DecompileMethodVisitor visitor, ClassContext context) {
		this.modifiers  = visitor.getModifiers();
		this.descriptor = visitor.getDescriptor(context);
		this.exceptions = visitor.getExceptions();
		this.codeGraph  = visitor.getCodeGraph();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(descriptor).addImportsFor(exceptions).addImportsFor(codeGraph);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.ln().ln().indent();
		writeModifiers(out, context);

		boolean isStatic = (modifiers & ACC_STATIC) != 0;

		if (codeGraph == null) {
			descriptor.write(out, context, isStatic, null);
			out.record(';');

		} else {
			descriptor.write(out, context, isStatic, codeGraph.getMethodScope().getVariables());
			out.recordsp().record(codeGraph, context);
		}
	}

	private void writeModifiers(DecompilationWriter out, ClassContext context) {
		if ((modifiers & ACC_ABSTRACT) != 0 &&
			(modifiers & (ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE | ACC_STRICT)) != 0) {

			throw new IllegalModifiersException(modifiers, EntryType.METHOD);
		}

		int classModifiers = context.getDecompilingClass().getModifiers();

		if ((classModifiers & ACC_INTERFACE) != 0) {
			out.record(switch (modifiers & ACC_ACCESS) {
				case ACC_PUBLIC    -> "";
				case ACC_PRIVATE   -> LIT_PRIVATE + " ";
				default -> throw new IllegalModifiersException("In the interface: ", modifiers, EntryType.METHOD);
			});

			if ((modifiers & (ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE)) != 0) {
				throw new IllegalModifiersException("In the interface: ", modifiers, EntryType.METHOD);
			}

			if ((modifiers & (ACC_ABSTRACT | ACC_STATIC | ACC_PRIVATE)) == 0) out.record(LIT_DEFAULT + " ");
			if ((modifiers & ACC_STATIC)   != 0) out.record(LIT_STATIC + " ");
			if ((modifiers & ACC_STRICT)   != 0) out.record(LIT_STRICT + " ");

			return;
		}

		out.record(switch (modifiers & ACC_ACCESS) {
			case ACC_VISIBLE   -> "";
			case ACC_PUBLIC    -> LIT_PUBLIC + " ";
			case ACC_PRIVATE   -> LIT_PRIVATE + " ";
			case ACC_PROTECTED -> LIT_PROTECTED + " ";
			default -> throw new IllegalModifiersException(modifiers, EntryType.METHOD);
		});

		if ((modifiers & ACC_ABSTRACT)      != 0) out.record(LIT_ABSTRACT + " ");
		if ((modifiers & ACC_STATIC)        != 0) out.record(LIT_STATIC + " ");
		if ((modifiers & ACC_STRICT)        != 0) out.record(LIT_STRICT + " ");

		if ((modifiers & ACC_FINAL)         != 0) out.record(LIT_FINAL + " ");
		if ((modifiers & ACC_SYNCHRONIZED)  != 0) out.record(LIT_SYNCHRONIZED + " ");
		if ((modifiers & ACC_NATIVE)        != 0) out.record(LIT_NATIVE + " ");
	}

	public void decompile(ClassContext context) {
		if (codeGraph != null) {
			try {
				codeGraph.decompile(descriptor, context);
			} catch (DecompilationException | DisassemblingException ex) {
				throw new DecompilationException("In method " + descriptor, ex);
			}
		}
	}
}
