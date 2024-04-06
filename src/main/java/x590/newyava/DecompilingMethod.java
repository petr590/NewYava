package x590.newyava;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.annotation.DefaultValue;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.CodeGraph;
import x590.newyava.decompilation.ReadonlyCode;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.DisassemblingException;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.List;
import java.util.Objects;

import static x590.newyava.Literals.*;
import static x590.newyava.Modifiers.*;

@Getter
public class DecompilingMethod implements ContextualWritable, Importable {
	private final int modifiers;

	private final MethodDescriptor descriptor;

	private final @Unmodifiable List<DecompilingAnnotation> annotations;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final @Nullable DefaultValue defaultValue;

	@Getter(AccessLevel.NONE)
	private final @Nullable CodeGraph codeGraph;

	public DecompilingMethod(DecompileMethodVisitor visitor, ClassContext context) {
		this.modifiers    = visitor.getModifiers();
		this.descriptor   = visitor.getDescriptor(context);
		this.annotations  = visitor.getAnnotations();
		this.exceptions   = visitor.getExceptions();
		this.defaultValue = visitor.getDefaultValue();
		this.codeGraph    = visitor.getCodeGraph();
	}

	public @NotNull ReadonlyCode getCode() {
		return Objects.requireNonNull(codeGraph);
	}

	public void decompile(ClassContext context) {
		if (codeGraph != null) {
			try {
				codeGraph.decompile(descriptor, context);

			} catch (DecompilationException ex) {
				ex.setMethodDescriptor(descriptor);
				throw ex;

			} catch (DisassemblingException ex) {
				throw new DecompilationException(ex, descriptor);
			}
		}
	}

	public int getVariablesInitPriority() {
		return codeGraph == null ? 0 : codeGraph.getVariablesInitPriority();
	}

	public void beforeVariablesInit() {
		if (codeGraph != null) {
			codeGraph.beforeVariablesInit();
		}
	}

	public void initVariables() {
		if (codeGraph != null) {
			codeGraph.initVariables();
		}
	}

	/** Можно ли оставить метод в классе.
	 * Должен вызываться только после {@link #decompile(ClassContext)} */
	public boolean keep(ClassContext context) {
		if ((modifiers & ACC_SYNTHETIC) != 0) {
			return false;
		}

		if (codeGraph != null && codeGraph.isEmpty()) {
			// Пустой static {}
			if (descriptor.isStaticInitializer()) {
				return false;
			}

			// Одиночный пустой конструктор
			if (descriptor.isConstructor() &&
					context.findMethods(method -> method.getDescriptor().isConstructor()).count() == 1) {

				return false;
			}
		}

		if (context.isEnumClass()) {
			var enumType = context.getThisType();

			return  !descriptor.equals(enumType, "valueOf", enumType, List.of(ClassType.STRING)) &&
					!descriptor.equals(enumType, "values", ArrayType.forType(enumType), List.of());
		}

		if ((context.getClassModifiers() & ACC_RECORD) != 0) {
			return codeGraph == null || !codeGraph.getMethodScope().isRecordInvokedynamic();
		}

		return true;
	}

	@Override
	public void addImports(ClassContext context) {
		context .addImportsFor(descriptor).addImportsFor(annotations)
				.addImportsFor(exceptions).addImportsFor(defaultValue)
				.addImportsFor(codeGraph);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.ln().ln().indent();

		DecompilingAnnotation.writeAnnotations(out, context, annotations);

		writeModifiers(out, context);

		boolean isStatic = (modifiers & ACC_STATIC) != 0;
		var variables = codeGraph == null ? null : codeGraph.getMethodScope().getVariables();

		descriptor.write(out, context, isStatic, variables);

		if (!exceptions.isEmpty()) {
			out.record(" throws ").record(exceptions, context, ", ");
		}

		if (defaultValue != null) {
			out.record(" default ").record(defaultValue.getAnnotationValue(), context, descriptor.returnType());
		}

		if (codeGraph != null) {
			out.recordSp().record(codeGraph, context);
		} else {
			out.record(';');
		}
	}

	private void writeModifiers(DecompilationWriter out, Context context) {
		if (descriptor.isStaticInitializer()) {
			if (modifiers != ACC_STATIC) {
				throw new IllegalModifiersException("In the static initializer: ", modifiers, EntryType.METHOD);
			}

			return;
		}

		if ((modifiers & ACC_ABSTRACT) != 0 &&
			(modifiers & (ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE | ACC_STRICT)) != 0) {

			throw new IllegalModifiersException(modifiers, EntryType.METHOD);
		}

		int classModifiers = context.getClassModifiers();

		if ((classModifiers & ACC_INTERFACE) != 0) {
			out.record(switch (modifiers & ACC_ACCESS) {
				case ACC_PUBLIC  -> "";
				case ACC_PRIVATE -> LIT_PRIVATE + " ";
				default -> throw new IllegalModifiersException("In the interface: ", modifiers, EntryType.METHOD);
			});

			if ((modifiers & (ACC_FINAL | ACC_SYNCHRONIZED | ACC_NATIVE)) != 0) {
				throw new IllegalModifiersException("In the interface: ", modifiers, EntryType.METHOD);
			}

			if ((modifiers & (ACC_ABSTRACT | ACC_STATIC | ACC_PRIVATE)) == 0) out.record(LIT_DEFAULT + " ");
			if ((modifiers & ACC_STATIC) != 0) out.record(LIT_STATIC + " ");
			if ((modifiers & ACC_STRICT) != 0) out.record(LIT_STRICT + " ");

			return;
		}

		if (context.isEnumClass() && descriptor.isConstructor()) {
			if ((modifiers & ACC_ACCESS) != ACC_PRIVATE) {
				throw new IllegalModifiersException("In the enum constructor: ", modifiers, EntryType.METHOD);
			}

		} else {
			out.record(switch (modifiers & ACC_ACCESS) {
				case ACC_VISIBLE -> "";
				case ACC_PUBLIC -> LIT_PUBLIC + " ";
				case ACC_PRIVATE -> LIT_PRIVATE + " ";
				case ACC_PROTECTED -> LIT_PROTECTED + " ";
				default -> throw new IllegalModifiersException(modifiers, EntryType.METHOD);
			});
		}

		if ((modifiers & ACC_ABSTRACT)      != 0) out.record(LIT_ABSTRACT + " ");
		if ((modifiers & ACC_STATIC)        != 0) out.record(LIT_STATIC + " ");
		if ((modifiers & ACC_STRICT)        != 0) out.record(LIT_STRICT + " ");

		if ((modifiers & ACC_FINAL)         != 0) out.record(LIT_FINAL + " ");
		if ((modifiers & ACC_SYNCHRONIZED)  != 0) out.record(LIT_SYNCHRONIZED + " ");
		if ((modifiers & ACC_NATIVE)        != 0) out.record(LIT_NATIVE + " ");
	}
}
