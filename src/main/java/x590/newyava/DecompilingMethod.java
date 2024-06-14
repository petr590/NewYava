package x590.newyava;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.annotation.DefaultValue;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.CodeGraph;
import x590.newyava.decompilation.ReadonlyCode;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
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

/**
 * Декомпилируемый метод
 */
@Getter
public class DecompilingMethod implements ContextualWritable, Importable {
	private final int modifiers;

	/** Формальный дескриптор */
	private final MethodDescriptor descriptor;

	/** Видимый дескриптор. Инициализируется после декомпиляции */
	private MethodDescriptor visibleDescriptor;

	private final @Unmodifiable List<DecompilingAnnotation> annotations;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final @Nullable DefaultValue defaultValue;

	@Getter(AccessLevel.NONE)
	private @Nullable CodeGraph codeGraph;

	/** Сообщение об исключении, произошедшем во время декомпиляции */
	@Getter(AccessLevel.NONE)
	private @Nullable String exceptionMessage;

	public DecompilingMethod(DecompileMethodVisitor visitor) {
		this.modifiers    = visitor.getModifiers();
		this.descriptor   = visitor.getDescriptor();

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
				handleException(ex, context);

			} catch (Exception ex) {
				handleException(new DecompilationException(ex), context);
			}
		}

		this.visibleDescriptor = getVisibleDescriptor(context);
	}

	private void handleException(DecompilationException ex, ClassContext context) {
		if (context.getConfig().failOnDecompilationException()) {
			ex.setMethodDescriptor(descriptor);
			throw ex;
		}

		exceptionMessage = ex.getMessage();
		codeGraph = null;
		ex.setMethodDescriptor(descriptor);
		ex.printStackTrace();
	}

	private MethodDescriptor getVisibleDescriptor(ClassContext context) {
		if (descriptor.isConstructor()) {
			if (context.isEnumClass()) {
				return descriptor.slice(2);
			}

			if (codeGraph != null) {
				return descriptor.slice(codeGraph.getMethodScope().getArgsStart());
			}
		}

		return descriptor;
	}

	/** Можно ли оставить метод в классе.
	 * Должен вызываться только после {@link #decompile(ClassContext)} */
	public boolean keep(ClassContext context) {
		if ((modifiers & ACC_SYNTHETIC) != 0) {
			return false;
		}

		var visDesc = visibleDescriptor;

		if (codeGraph != null && codeGraph.isEmpty()) {
			// Пустой static {}
			if (visDesc.isStaticInitializer()) {
				return false;
			}

			// Одиночный пустой конструктор
			if (visDesc.isConstructor() && visDesc.arguments().isEmpty() &&
					context.findMethods(method -> method.getDescriptor().isConstructor()).count() == 1) {

				return false;
			}
		}

		// Конструктор в анонимном классе
		if (visDesc.isConstructor() && visDesc.hostClass().isAnonymous()) {
			return false;
		}

		// Автосгенерированные методы enum-класса
		if (context.isEnumClass()) {
			var enumType = context.getThisType();

			return  !visDesc.equals(enumType, "valueOf", enumType, List.of(ClassType.STRING)) &&
					!visDesc.equals(enumType, "values", ArrayType.forType(enumType));
		}

		// Геттеры полей в record
		if ((context.getClassModifiers() & ACC_RECORD) != 0) {
			return codeGraph == null || !codeGraph.getMethodScope().isRecordInvokedynamic();
		}

		return true;
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

	public void inferVariableTypesAndNames() {
		if (codeGraph != null) {
			codeGraph.inferVariableTypesAndNames();
		}
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

		visibleDescriptor.write(out, context, isStatic, variables);

		if (!exceptions.isEmpty()) {
			out.record(" throws ").record(exceptions, context, ", ");
		}

		if (defaultValue != null) {
			out.record(" default ").record(defaultValue.getAnnotationValue(), new ConstantWriteContext(context, descriptor.returnType(), true));
		}

		if (codeGraph != null && exceptionMessage == null) {
			out.space().record(codeGraph, context);
		} else {
			out.record(';');

			if (exceptionMessage != null) {
				out.record(" /* ").record(exceptionMessage).record(" */");
			}
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

		if ((modifiers & ACC_ABSTRACT)     != 0) out.record(LIT_ABSTRACT + " ");
		if ((modifiers & ACC_STATIC)       != 0) out.record(LIT_STATIC + " ");
		if ((modifiers & ACC_STRICT)       != 0) out.record(LIT_STRICT + " ");

		if ((modifiers & ACC_FINAL)        != 0) out.record(LIT_FINAL + " ");
		if ((modifiers & ACC_SYNCHRONIZED) != 0) out.record(LIT_SYNCHRONIZED + " ");
		if ((modifiers & ACC_NATIVE)       != 0) out.record(LIT_NATIVE + " ");
	}
}
