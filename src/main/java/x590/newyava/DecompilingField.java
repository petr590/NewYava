package x590.newyava;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.invoke.InvokeSpecialOperation;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;
import x590.newyava.visitor.DecompileFieldVisitor;

import java.util.List;

import static x590.newyava.Modifiers.*;
import static x590.newyava.Literals.*;

/**
 * Декомпилируемое поле
 */
@Getter
public class DecompilingField implements ContextualWritable, Importable {
	private final int modifiers;

	private final FieldDescriptor descriptor;

	private final @Unmodifiable List<DecompilingAnnotation> annotations;

	private @Nullable Operation initializer;

	public DecompilingField(DecompileFieldVisitor visitor, ClassContext context) {
		this.modifiers   = visitor.getModifiers();
		this.descriptor  = visitor.getDescriptor(context);
		this.annotations = visitor.getAnnotations();
		this.initializer = visitor.getInitializer();
	}

	/** Можно ли оставить поле в классе */
	public boolean keep() {
		return (modifiers & ACC_SYNTHETIC) == 0;
	}

	public boolean isEnum() {
		return (modifiers & ACC_ENUM) != 0;
	}

	public boolean setInitializer(Operation value) {
		if (initializer == null) {
			initializer = value;
			return true;
		}

		return false;
	}

	public void inferVariableTypes() {
		if (initializer != null) {
			initializer.inferType(descriptor.type());
		}
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(descriptor).addImportsFor(annotations).addImportsFor(initializer);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.ln().indent();

		DecompilingAnnotation.writeAnnotations(out, context, annotations);

		writeModifiers(out, context);

		out.record(descriptor, context);

		if (initializer != null) {
			out.record(" = ").record(
					initializer, context, Priority.ZERO,
					Type.isArray(descriptor.type()) ? Operation::writeAsArrayInitializer : Operation::write
			);
		}

		out.record(';');
	}

	private void writeModifiers(DecompilationWriter out, Context context) {
		int classModifiers = context.getClassModifiers();

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

	public boolean canInlineEnumConstant() {
		return isEnum() &&
				initializer instanceof InvokeSpecialOperation invokeSpecial &&
				invokeSpecial.canInlineEnumConstant();
	}

	public void writeAsEnumConstant(DecompilationWriter out, Context context, int minWidth) {
		out.record(descriptor.name());

		if (initializer instanceof InvokeSpecialOperation invokeSpecial &&
			!invokeSpecial.canInlineEnumConstant()) {

			out.record(" ".repeat(Math.max(0, minWidth - descriptor.name().length())));
			invokeSpecial.writeNew(out, context, true);
		}
	}
}
