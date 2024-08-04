package x590.newyava;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.annotation.DefaultValue;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.Context;
import x590.newyava.decompilation.code.Code;
import x590.newyava.decompilation.code.CodeGraph;
import x590.newyava.decompilation.code.CodeProxy;
import x590.newyava.decompilation.code.InvalidCode;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.modifiers.EntryType;
import x590.newyava.type.ArrayType;
import x590.newyava.type.ClassType;
import x590.newyava.type.ReferenceType;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.List;

import static x590.newyava.Literals.*;
import static x590.newyava.modifiers.Modifiers.*;

/**
 * Декомпилируемый метод
 */
@Getter
public class DecompilingMethod implements IMethod, ContextualWritable, Importable {
	private final int modifiers;

	/** Формальный дескриптор */
	private final MethodDescriptor descriptor;

	/** Видимый дескриптор. Инициализируется после декомпиляции */
	private MethodDescriptor visibleDescriptor;

	private final @Unmodifiable List<DecompilingAnnotation> annotations;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final @Nullable DefaultValue defaultValue;

	/** Код метода. Может быть {@code null}, если метод не имеет кода
	 * или во время декомпиляции произошло исключение. */
	@Getter(AccessLevel.NONE)
	private @Nullable CodeGraph codeGraph;

	/** Прокси, который хранит экземпляр {@link CodeGraph} или {@link InvalidCode}. */
	private final CodeProxy code;

	public DecompilingMethod(DecompileMethodVisitor visitor) {
		this.modifiers    = visitor.getModifiers();
		this.descriptor   = visitor.getDescriptor();

		this.annotations  = visitor.getAnnotations();
		this.exceptions   = visitor.getExceptions();
		this.defaultValue = visitor.getDefaultValue();
		this.codeGraph    = visitor.getCodeGraph();

		this.code = new CodeProxy(codeGraph != null ? codeGraph : InvalidCode.EMPTY);
	}

	public boolean isSynthetic() {
		return (modifiers & ACC_SYNTHETIC) != 0;
	}

	public Code getCode() {
		return code;
	}

	/** Если {@link #codeGraph} не {@code null}, то выполняет {@code action}.
	 * При возникновении исключения оно обрабатывается в {@link #handleException},
	 * при необходимости оно оборачивается в {@link DecompilationException}. */
	private void tryCatchOnCodeGraph(Context context, FailableConsumer<CodeGraph, DecompilationException> action) {
		if (codeGraph != null) {
			try {
				action.accept(codeGraph);

			} catch (DecompilationException ex) {
				handleException(ex, context);

			} catch (Exception ex) {
				handleException(new DecompilationException(ex), context);
			}
		}
	}

	/** Обрабатывает исключение. Если {@link Config#failOnDecompilationException()} равно {@code true},
	 * то выбрасывает это исключение ещё раз, иначе записывает в консоль стектрейс. */
	private void handleException(DecompilationException exception, Context context) {
		if (context.getConfig().failOnDecompilationException()) {
			exception.setMethodDescriptor(descriptor);
			throw exception;
		}

		Throwable ex = exception.getCause() != null ? exception.getCause() : exception;

		String  name = ex.getClass().getSimpleName(),
				message = ex.getMessage();

		int size = codeGraph == null ? 0 : codeGraph.getSize();

		codeGraph = null;
		code.setCode(new InvalidCode(StringUtils.isEmpty(message) ? name : name + ": " + message));

		exception.setMethodDescriptor(descriptor);

		if (context.getConfig().skipStackTrace()) {
			System.err.println(StringUtils.isEmpty(message) ?
					name + "| In method " + descriptor + "| " + size :
					name + "| In method " + descriptor + "| " + size + "| " + message
			);
		} else {
			ex.printStackTrace();
		}
	}

	public void decompile(Context context) {
		tryCatchOnCodeGraph(context, codeGraph -> codeGraph.decompile(descriptor, context));
		this.visibleDescriptor = getVisibleDescriptor(context);
	}

	public void afterDecompilation(Context context) {
		tryCatchOnCodeGraph(context, CodeGraph::afterDecompilation);
	}

	private MethodDescriptor getVisibleDescriptor(Context context) {
		if (descriptor.isConstructor()) {
			if (context.isEnumClass()) {
				return descriptor.slice(2);
			}

			if (code.isValid()) {
				var methodScope = code.getMethodScope();
				return descriptor.slice(methodScope.getArgsStart(), methodScope.getArgsEnd());
			}
		}

		return descriptor;
	}

	/** @return индекс начала видимых аргументов метода. */
	public int getArgsStart() {
		return visibleDescriptor.fromIndex();
	}

	/** @return индекс конца видимых аргументов метода. */
	public int getArgsEnd() {
		return visibleDescriptor.fromIndex() + visibleDescriptor.arguments().size();
	}

	/** Можно ли оставить метод в классе.
	 * Должен вызываться только после {@link #decompile(Context)} */
	public boolean keep(ClassContext context, @Nullable @Unmodifiable List<DecompilingField> recordComponents) {
		if ((modifiers & ACC_SYNTHETIC) != 0) {
			return false;
		}

		var visDesc = visibleDescriptor;

		if (code.isValid() && code.getMethodScope().isEmpty()) {
			// Пустой static {}
			if (visDesc.isStaticInitializer()) {
				return false;
			}

			if (visDesc.isConstructor()) {
				// Одиночный пустой конструктор
				if (visDesc.arguments().isEmpty() &&
					context.findMethods(method -> method.getDescriptor().isConstructor()).count() == 1) {
					return false;
				}

				// Пустой дефолтный record-конструктор
				if (visDesc.isRecordDefaultConstructor(context)) {
					return false;
				}
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

		if ((context.getClassModifiers() & ACC_RECORD) != 0 && code.isValid()) {

			// Автосгенерированные методы hashCode, equals и toString в record
			if (code.getMethodScope().isRecordInvokedynamic()) {
				return false;
			}

			// Автосгенерированные геттеры полей в record
			if (recordComponents != null && visDesc.arguments().isEmpty() &&
				visDesc.hostClass() instanceof ClassType hostClass) {

				var fieldDescriptor = new FieldDescriptor(hostClass, visDesc.name(), visDesc.returnType());

				return recordComponents.stream().noneMatch(field -> field.getDescriptor().equals(fieldDescriptor)) ||
						!code.getMethodScope().isGetterOf(fieldDescriptor);
			}
		}

		return true;
	}

	public int getVariablesInitPriority() {
		return codeGraph == null ? 0 : codeGraph.getVariablesInitPriority();
	}

	public void beforeVariablesInit(Context context) {
		tryCatchOnCodeGraph(context, CodeGraph::beforeVariablesInit);
	}

	public void initVariables(Context context) {
		tryCatchOnCodeGraph(context, CodeGraph::initVariables);
	}

	public void inferVariableTypesAndNames(Context context) {
		tryCatchOnCodeGraph(context, CodeGraph::inferVariableTypesAndNames);
	}

	@Override
	public void addImports(ClassContext context) {
		context .addImportsFor(descriptor).addImportsFor(annotations)
				.addImportsFor(exceptions).addImportsFor(defaultValue)
				.addImportsFor(code);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.ln().ln().indent();

		DecompilingAnnotation.writeAnnotations(out, context, annotations);

		writeModifiers(out, context);

		boolean isStatic = (modifiers & ACC_STATIC) != 0;
		var variables = code.isValid() ? code.getMethodScope().getVariables() : null;

		visibleDescriptor.write(out, context, isStatic, variables);

		if (!exceptions.isEmpty()) {
			out.record(" throws ").record(exceptions, context, ", ");
		}

		if (defaultValue != null) {
			defaultValue.write(out, new ConstantWriteContext(context, descriptor.returnType(), true, true));
		}

		if (code.isValid()) {
			out.space().record(code, context);
		} else {
			out.record(';');

			if (code.caughtException()) {
				out.space().record(code, context);
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
