package x590.newyava;

import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.visitor.DecompileFieldVisitor;

import static x590.newyava.Modifiers.*;
import static x590.newyava.Literals.*;

public class DecompilingField implements ContextualWritable, Importable {

	private final int modifiers;
	private final FieldDescriptor descriptor;

	private final @Nullable Object constantValue;

	public DecompilingField(DecompileFieldVisitor visitor, ClassContext context) {
		this.modifiers     = visitor.getModifiers();
		this.descriptor    = visitor.getDescriptor(context);
		this.constantValue = visitor.getConstantValue();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(descriptor);
	}

	@Override
	public void write(DecompilationWriter out, ClassContext context) {
		out.ln().indent();
		writeModifiers(out, context);
		out.record(descriptor, context);

		if (constantValue != null)
			out.record(" = ").record(constantValue.toString());

		out.record(';');
	}

	private void writeModifiers(DecompilationWriter out, ClassContext context) {
		int classModifiers = context.getDecompilingClass().getModifiers();

		if ((classModifiers & ACC_INTERFACE) != 0) {
			if ((modifiers & ACC_FIELD) != ACC_PUBLIC_STATIC_FINAL) {
				throw new IllegalModifiersException("In the interface: ", modifiers, EntryType.FIELD);
			}

			return;
		}

		if ((modifiers & ACC_ENUM) != 0) {
			if ((classModifiers & ACC_ENUM) == 0) {
				throw new IllegalModifiersException("Enum field in not enum class: ", modifiers, EntryType.FIELD);
			}

			if ((modifiers & ACC_PUBLIC_STATIC_FINAL) != ACC_PUBLIC_STATIC_FINAL) {
				throw new IllegalModifiersException("Enum field: ", modifiers, EntryType.FIELD);
			}

			return;
		}

		out.record(switch (modifiers & ACC_ACCESS) {
			case ACC_VISIBLE   -> "";
			case ACC_PUBLIC    -> LIT_PUBLIC + " ";
			case ACC_PRIVATE   -> LIT_PRIVATE + " ";
			case ACC_PROTECTED -> LIT_PROTECTED + " ";
			default -> throw new IllegalModifiersException(modifiers, EntryType.FIELD);
		});

		if ((modifiers & ACC_STATIC) != 0 && (modifiers & (ACC_ENUM | ACC_RECORD | ACC_INTERFACE)) == 0)
			out.record(LIT_STATIC + " ");

		if ((modifiers & (ACC_FINAL | ACC_VOLATILE)) == (ACC_FINAL | ACC_VOLATILE))
			throw new IllegalModifiersException(modifiers, EntryType.FIELD);

		if ((modifiers & ACC_FINAL)     != 0) out.record(LIT_FINAL + " ");
		if ((modifiers & ACC_VOLATILE)  != 0) out.record(LIT_VOLATILE + " ");
		if ((modifiers & ACC_TRANSIENT) != 0) out.record(LIT_TRANSIENT + " ");
	}
}
