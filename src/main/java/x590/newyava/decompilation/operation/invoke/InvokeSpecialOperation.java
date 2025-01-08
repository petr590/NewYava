package x590.newyava.decompilation.operation.invoke;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.DecompilingClass;
import x590.newyava.DecompilingField;
import x590.newyava.DecompilingMethod;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Code;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.other.NewOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.decompilation.scope.MethodScope;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.type.*;
import x590.newyava.util.Utils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class InvokeSpecialOperation extends InvokeNonstaticOperation {

	private enum InvokeType {
		PLAIN, NEW, THIS, SUPER, SUPER_INTERFACE
	}

	/** Представляет собой объект {@code super} или {@code Interface.super}. */
	private record SuperObject(
			@Getter VariableReference varRef,
			@Getter IClassArrayType returnType,
			boolean isInterface
	) implements ILoadOperation {
		@Override
		public boolean isThisRef() {
			return true;
		}

		@Override
		public void addImports(ClassContext context) {
			if (isInterface) {
				context.addImport(returnType);
			}
		}

		@Override
		public void write(DecompilationWriter out, MethodWriteContext context) {
			if (isInterface) {
				out.record(returnType, context).record('.');
			}

			out.record("super");
		}
	}

	@EqualsAndHashCode.Include
	private final InvokeType invokeType;

	@EqualsAndHashCode.Include
	private final Type returnType;

	public static Operation valueOf(MethodContext context, MethodDescriptor descriptor) {
		return new InvokeSpecialOperation(context, descriptor);
	}


	private InvokeSpecialOperation(MethodContext context, MethodDescriptor descriptor) {
		super(context, descriptor);

		this.invokeType = getInvokeType(context, descriptor);

		this.returnType = invokeType == InvokeType.NEW ?
				object.getReturnType() :
				descriptor.returnType();

		switch (invokeType) {
			case SUPER, SUPER_INTERFACE ->
					object = new SuperObject(
							((ILoadOperation)object).getVarRef(),
							descriptor.hostClass(),
							invokeType == InvokeType.SUPER_INTERFACE
					);
		}
	}


	private InvokeType getInvokeType(MethodContext context, MethodDescriptor descriptor) {
		if (object.isThisRef()) {
			var hostClass = descriptor.hostClass();

			return  hostClass.baseEquals(context.getThisType()) ? InvokeType.THIS :
					hostClass.baseEquals(context.getSuperType()) ? InvokeType.SUPER : InvokeType.SUPER_INTERFACE;

		} else if (descriptor.isConstructor() &&
				object instanceof NewOperation newOp &&
				context.popIfSame(newOp)) {

			return InvokeType.NEW;
		}

		return InvokeType.PLAIN;
	}

	/* ------------------------------------------------- properties ------------------------------------------------- */

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public boolean isDefaultConstructor(MethodContext context) {
		return (invokeType == InvokeType.SUPER || invokeType == InvokeType.THIS) &&
				(arguments.isEmpty() || isDefaultEnumConstructor(context));
	}


	private static final MethodDescriptor DEFAULT_ENUM_CONSTRUCTOR =
			MethodDescriptor.constructor(ClassType.ENUM, List.of(ClassType.STRING, PrimitiveType.INT));

	private boolean isDefaultEnumConstructor(MethodContext context) {
		return (context.getClassModifiers() & Modifiers.ACC_ENUM) != 0 &&
				descriptor.equals(DEFAULT_ENUM_CONSTRUCTOR);
	}

	public boolean isNew() {
		return invokeType == InvokeType.NEW;
	}

	public boolean isNew(MethodDescriptor descriptor) {
		return isNew() && this.descriptor.equals(descriptor);
	}


	/** @return {@code true}, если {@code enum}-константа не содержит аргументов конструктора
	 * и не переопределяет методы суперкласса. */
	public boolean canInlineEnumConstant() {
		return arguments.size() <= 2 && getAnonymousClassType() == null;
	}


	/** @return анонимный класс, если это вызов конструктора анонимного класса, иначе {@code null}. */
	private @Nullable IClassType getAnonymousClassType() {
		return  invokeType == InvokeType.NEW &&
				returnType instanceof IClassType classType && classType.isAnonymous() ?
				classType : null;
	}


	private @Nullable DecompilingClass anonymousClass;
	private @Nullable DecompilingMethod nestedClassConstructor;

	private boolean nestedClassConstructorInitialized;

	private @Nullable DecompilingMethod getNestedClassConstructor(Context context) {
		if (nestedClassConstructorInitialized)
			return nestedClassConstructor;

		nestedClassConstructorInitialized = true;
		return nestedClassConstructor = context.findMethod(descriptor).orElse(null);
	}

	@Override
	public boolean canUnite(MethodContext context, Operation prev) {
		return hasOuterInstance(context) && Utils.isFirst(arguments, arg -> OperationUtils.isNullCheck(prev, arg))
				|| super.canUnite(context, prev);
	}

	private boolean hasOuterInstance(Context context) {
		var constructor = getNestedClassConstructor(context);
		return constructor != null && constructor.hasOuterInstance();
	}

	@Override
	public void beforeVariablesInit(Context context, @Nullable MethodScope methodScope) {
		super.beforeVariablesInit(context, methodScope);

		var foundClass = context.findClass(getAnonymousClassType());

		anonymousClass = foundClass.orElse(null);

		// Связываем поля с внешними переменными

		Optional<Int2ObjectMap<DecompilingField>> outerVarTable = foundClass
				.map(clazz -> clazz.findMethods(method -> method.getDescriptor().isConstructor()))
				.filter(constructors -> constructors.size() == 1)
				.map(constructors -> constructors.get(0).getCode())
				.filter(Code::isValid)
				.map(code -> code.getMethodScope().getOuterVarTable());

		if (outerVarTable.isPresent()) {
			for (var entry : outerVarTable.get().int2ObjectEntrySet()) {
				if (arguments.get(entry.getIntKey()) instanceof ILoadOperation load) {
					entry.getValue().bindWithOuterVariable(load.getVarRef());
				}
			}
		}
	}

	@Override
	public boolean needEmptyLinesAround() {
		return anonymousClass != null && anonymousClass.isMultiline() || super.needEmptyLinesAround();
	}


	/* --------------------------------------------------- write ---------------------------------------------------- */

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		switch (invokeType) {
			case PLAIN -> {
				super.write(out, context);
				return;
			}

			case NEW -> {
				writeNew(out, context, false);
				return;
			}
		}

		out.record(object, context, getPriority());

		if (!descriptor.isConstructor()) {
			out.record('.').record(descriptor.name());
		}

		writeArgs(out, context);
	}

	/**
	 * Записывает операцию как создание нового объекта.
	 * Записывает тело анонимного класса, если оно есть.
	 * @param isEnumConstant если {@code true}, то записывает только аргументы
	 *    и тело анонимного класса, если они есть. Иначе записывает как<br>
	 *    {@code new Class(args) { class body }}
	 */
	public void writeNew(DecompilationWriter out, MethodWriteContext context, boolean isEnumConstant) {
		var anonymousClass = this.anonymousClass;

		if (anonymousClass != null) {
			if (isEnumConstant) {
				writeEnumArgs(out, context);

			} else {
				var superType = anonymousClass.getVisibleSuperType();
				var interfaces = anonymousClass.getVisibleInterfaces();

				if (superType != null && interfaces.size() > 0 || interfaces.size() > 1) {
					throw new DecompilationException(
							"Anonymous class extends %s and implements %s",
							superType, interfaces.stream().map(Object::toString).collect(Collectors.joining(", "))
					);
				}

				var type = superType != null ? superType :
						!interfaces.isEmpty() ? interfaces.get(0) : ClassType.OBJECT;

				out.record("new ").record(type, context);
				writeArgs(out, context);
			}

			anonymousClass.writeBody(out.space());
			return;
		}

		if (isEnumConstant) {
			writeEnumArgs(out, context);

		} else {
			var constructor = getNestedClassConstructor(context);

			if (constructor != null && constructor.hasOuterInstance()) {
				if (!arguments.get(0).isThisRef()) {
					out.record(arguments.get(0), context, Priority.DEFAULT).record('.');
				}

				out.record("new ").record(((ClassType)returnType).getSimpleName());

			} else {
				out.record(object, context, Priority.ZERO);
			}

			writeArgs(out, context);
		}
	}

	/** Записывает аргументы.
	 * Если это вызов конструктора enum-класса, то пропускает первые два аргумента. */
	private void writeArgs(DecompilationWriter out, MethodWriteContext context) {

		if ((context.getClassModifiers() & Modifiers.ACC_ENUM) != 0 &&
			descriptor.isConstructor() &&
			descriptor.hostClass().equals(context.getThisType()) &&
			arguments.size() >= 2) {

			writeArgsSkipping2(out, context);

		} else {
			var constructor = getNestedClassConstructor(context);
			var visibleArgs = constructor == null ? arguments :
					arguments.subList(constructor.getArgsStart(), constructor.getArgsEnd());

			out.record('(').record(visibleArgs, context, Priority.ZERO, ", ").record(')');
		}
	}

	/** Записывает все аргументы, кроме первых двух.
	 * Если аргументов всего два, то не записывает ничего. */
	private void writeEnumArgs(DecompilationWriter out, MethodWriteContext context) {
		if (arguments.size() > 2) {
			writeArgsSkipping2(out, context);
		}
	}

	private void writeArgsSkipping2(DecompilationWriter out, MethodWriteContext context) {
		var args = arguments;

		out .record('(')
			.record(args.subList(Math.min(args.size(), 2), args.size()), context, Priority.ZERO, ", ")
			.record(')');
	}
}
