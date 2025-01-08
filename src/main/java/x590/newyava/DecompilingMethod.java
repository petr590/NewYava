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
import x590.newyava.decompilation.operation.invoke.InvokeOperation;
import x590.newyava.decompilation.operation.terminal.ReturnValueOperation;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.IncompleteMethodDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.modifiers.EntryType;
import x590.newyava.type.*;
import x590.newyava.util.Utils;
import x590.newyava.visitor.DecompileMethodVisitor;

import java.util.List;
import java.util.Objects;

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
	private @Nullable MethodDescriptor visibleDescriptor;

	/** Индекс начала видимых аргументов метода. */
	private int argsStart = -1;

	/** Индекс конца видимых аргументов метода. */
	private int argsEnd = -1;

	private final Signature signature;


	@Getter(AccessLevel.NONE)
	private final List<DecompilingAnnotation> annotations;

	private final @Unmodifiable List<ReferenceType> exceptions;

	private final @Nullable DefaultValue defaultValue;

	/** Код метода. Может быть {@code null}, если метод не имеет кода
	 * или во время декомпиляции произошло исключение. */
	@Getter(AccessLevel.NONE)
	private @Nullable CodeGraph codeGraph;

	/** Прокси, который хранит экземпляр {@link CodeGraph} или {@link InvalidCode}. */
	private final CodeProxy code;

	public DecompilingMethod(DecompileMethodVisitor visitor) {
		this.modifiers         = visitor.getModifiers();
		this.signature         = visitor.getSignature();
		this.descriptor        = visitor.getDescriptor();
		this.visibleDescriptor = visitor.getVisibleDescriptor();
		this.annotations       = visitor.getAnnotations();
		this.exceptions        = visitor.getExceptions();
		this.defaultValue      = visitor.getDefaultValue();
		this.codeGraph         = visitor.getCodeGraph();

		this.code = new CodeProxy(codeGraph != null ? codeGraph : InvalidCode.EMPTY);
	}

	public boolean isSynthetic() {
		return (modifiers & ACC_SYNTHETIC) != 0;
	}

	public boolean isStatic() {
		return (modifiers & ACC_STATIC) != 0;
	}

	@Override
	public MethodDescriptor getVisibleDescriptor() {
		return Objects.requireNonNull(visibleDescriptor);
	}

	public Code getCode() {
		return code;
	}

	/** @return {@code true}, если первый аргумент метода - внешний экземпляр {@code this}. */
	public boolean hasOuterInstance() {
		return code.isValid() && code.getMethodScope().hasOuterInstance();
	}

	private MethodDescriptor requireVisibleDescriptor() {
		return Objects.requireNonNull(visibleDescriptor);
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
			System.err.println(name + "|" + message + "|" + size + "|" + descriptor);
		} else {
			ex.printStackTrace();
		}
	}

	public void decompile(Context context) {
		tryCatchOnCodeGraph(context, codeGraph -> codeGraph.decompile(descriptor, context));
		initArgsBounds(context);

		if (visibleDescriptor == null) {
			visibleDescriptor = descriptor.slice(argsStart, argsEnd);
		}
	}

	public void afterDecompilation(Context context) {
		if (!isStatic() && !descriptor.isConstructor() && hasSuperMethod(context, context.getDecompilingClass())) {
			annotations.add(new DecompilingAnnotation(ClassType.OVERRIDE));
		}

		tryCatchOnCodeGraph(context, CodeGraph::afterDecompilation);

		if ((modifiers & ACC_BRIDGE) != 0 && code.isValid()) {
			var operations = code.getMethodScope().getOperations();
			if (operations.size() != 1) return;

			var operation = operations.get(0);
			if (operation instanceof ReturnValueOperation ret) operation = ret.getValue();

			if (!(operation instanceof InvokeOperation invokeOp)) return;
			var invDescriptor = invokeOp.getDescriptor();

			if (invDescriptor.hostClass().equals(descriptor.hostClass()) &&
				invDescriptor.name().equals(descriptor.name()) &&
				invDescriptor.arguments().size() == descriptor.arguments().size()) {

				var foundMethod = context.getDecompilingClass().findMethod(invDescriptor);
				foundMethod.ifPresent(method -> method.annotations.add(new DecompilingAnnotation(ClassType.OVERRIDE)));
			}
		}
	}

	private boolean hasSuperMethod(Context context, @Nullable IClass clazz) {
		if (clazz == null || clazz.getThisType().equals(ClassType.OBJECT))
			return false;

		return hasMethod(context, clazz.getSuperType()) ||
				clazz.getInterfaces().stream().anyMatch(interf -> hasMethod(context, interf));
	}

	private boolean hasMethod(Context context, IClassArrayType type) {
		return context.findIMethod(new MethodDescriptor(
				type.base(), descriptor.name(), descriptor.returnType(), descriptor.arguments()
		)).isPresent() || hasSuperMethod(context, context.findIClass(type).orElse(null));
	}


	private void initArgsBounds(Context context) {
		if (descriptor.isConstructor() && context.isEnumClass()) {
			argsStart = 2;
			argsEnd = descriptor.arguments().size();

		} else if (code.isValid()) {
			var methodScope = code.getMethodScope();
			argsStart = methodScope.getArgsStart();
			argsEnd = methodScope.getArgsEnd();
		} else {

			argsStart = 0;
			argsEnd = descriptor.arguments().size();
		}
	}

	/** Можно ли оставить метод в классе.
	 * Должен вызываться только после {@link #decompile(Context)} */
	public boolean keep(ClassContext context, @Nullable @Unmodifiable List<DecompilingField> recordComponents) {
		if ((modifiers & ACC_SYNTHETIC) != 0) {
			return false;
		}

		var visDesc = requireVisibleDescriptor();

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
				boolean isRecordComponentGetter =
						recordComponents.stream().anyMatch(field -> field.getDescriptor().baseEquals(fieldDescriptor)) &&
						code.getMethodScope().isGetterOf(fieldDescriptor);

				return !isRecordComponentGetter;
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


	private @Nullable @Unmodifiable List<String> possibleNames;


	private static final IncompleteMethodDescriptor MAIN_DESCRIPTOR =
			new IncompleteMethodDescriptor("main", PrimitiveType.VOID, List.of(ArrayType.forType(ClassType.STRING)));

	public void inferVariableTypesAndNames(Context context) {
		if (descriptor.equalsIgnoreClass(MAIN_DESCRIPTOR) &&
			(modifiers & (ACC_ACCESS | ACC_STATIC)) == (ACC_PUBLIC | ACC_STATIC)) {

			possibleNames = List.of("args");

		} else if (isSetter()) {
			String name = Utils.safeToLowerCamelCase(descriptor.name().substring(3));
			possibleNames = List.of(name);
		}

		tryCatchOnCodeGraph(context, codeGraph -> codeGraph.inferVariableTypesAndNames(possibleNames));
	}

	private boolean isSetter() {
		String name = descriptor.name();

		return  name.length() > 3 &&
				name.startsWith("set") &&
				Character.isUpperCase(name.charAt(3)) &&
				descriptor.arguments().size() == 1;
	}


	@Override
	public void addImports(ClassContext context) {
		context .addImportsFor(visibleDescriptor).addImportsFor(exceptions)
				.addImportsFor(annotations).addImportsFor(defaultValue)
				.addImportsFor(signature).addImportsFor(code);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.ln().ln().indent();

		DecompilingAnnotation.writeAnnotations(out, context, annotations);

		writeModifiers(out, context);

		if (!signature.isEmpty()) {
			out.record(signature, context).space();
		}


		int startIndex =
				(isStatic() ? 0 : 1) +
				MethodDescriptor.slots(descriptor.arguments().subList(0, argsStart));

		var variables = code.isValid() ? code.getMethodScope().getVariables() : null;

		requireVisibleDescriptor().write(
				out, context, (modifiers & ACC_VARARGS) != 0,
				startIndex, variables, possibleNames
		);


		if (!exceptions.isEmpty()) {
			out.record(" throws ").record(exceptions, context, ", ");
		}

		if (defaultValue != null) {
			defaultValue.write(out, new ConstantWriteContext(context, descriptor.returnType(), true, true, true));
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
