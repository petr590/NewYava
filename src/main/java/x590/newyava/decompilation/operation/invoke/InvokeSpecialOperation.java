package x590.newyava.decompilation.operation.invoke;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.DecompilingClass;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.modifiers.Modifiers;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.NewOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.ReferenceType;
import x590.newyava.type.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InvokeSpecialOperation extends InvokeNonstaticOperation {

	private enum InvokeType {
		PLAIN, NEW, THIS, SUPER, SUPER_INTERFACE
	}

	/** Представляет собой объект {@code super} или {@code Interface.super}. */
	private record SuperObject(
			@Getter VariableReference varRef,
			@Getter ReferenceType returnType,
			boolean isInterface
	) implements ILoadOperation {
		@Override
		public boolean isThisRef() {
			return true;
		}

		@Override
		public void write(DecompilationWriter out, MethodWriteContext context) {
			if (isInterface) {
				out.record(returnType, context).record('.');
			}

			out.record("super");
		}
	}

	private final InvokeType invokeType;

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
		};
	}


	private InvokeType getInvokeType(MethodContext context, MethodDescriptor descriptor) {
		if (object.isThisRef()) {
			var hostClass = descriptor.hostClass();

			return  hostClass.equals(context.getThisType()) ? InvokeType.THIS :
					hostClass.equals(context.getSuperType()) ? InvokeType.SUPER : InvokeType.SUPER_INTERFACE;

		} else if (descriptor.isConstructor() &&
				object instanceof NewOperation newOperation &&
				context.popIfSame(newOperation)) {

			return InvokeType.NEW;
		}

		return InvokeType.PLAIN;
	}

	@Override
	public Type getReturnType() {
		return returnType;
	}

	@Override
	public boolean isDefaultConstructor(MethodContext context) {
		return (invokeType == InvokeType.SUPER || invokeType == InvokeType.THIS) &&
				(arguments.isEmpty() || isDefaultEnumConstructor(context));
	}


	// Enum.<init>(String, int)
	private static final MethodDescriptor DEFAULT_ENUM_CONSTRUCTOR =
			new MethodDescriptor(ClassType.ENUM, MethodDescriptor.INIT, PrimitiveType.VOID,
					List.of(ClassType.STRING, PrimitiveType.INT));

	private boolean isDefaultEnumConstructor(MethodContext context) {
		return (context.getClassModifiers() & Modifiers.ACC_ENUM) != 0 &&
				descriptor.equals(DEFAULT_ENUM_CONSTRUCTOR);
	}


	/** @return {@code true}, если {@code enum}-константа не содержит аргументов конструктора
	 * и не переопределяет методы суперкласса. */
	public boolean canInlineEnumConstant() {
		return arguments.size() <= 2 && getAnonymousClassType() == null;
	}


	/** @return анонимный класс, если это вызов конструктора анонимного класса, иначе {@code null}. */
	private @Nullable ClassType getAnonymousClassType() {
		return  invokeType == InvokeType.NEW &&
				returnType instanceof ClassType classType && classType.isAnonymous() ?
				classType : null;
	}

	/** @return вложенный класс, если это вызов конструктора вложенного класса, иначе {@code null}. */
	private @Nullable ClassType getNestedClassType() {
		return  invokeType == InvokeType.NEW &&
		        returnType instanceof ClassType classType && classType.isNested() ?
		        classType : null;
	}

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
	 *    {@code new <type>(args) { class body }}
	 */
	public void writeNew(DecompilationWriter out, MethodWriteContext context, boolean isEnumConstant) {
		var foundClass = context.findClass(getAnonymousClassType());

		if (foundClass.isPresent()) {
			var anonymous = foundClass.get();

			var superType = anonymous.getVisibleSuperType();
			var interfaces = anonymous.getVisibleInterfaces();

			if (superType != null && interfaces.size() > 0 || interfaces.size() > 1) {
				throw new DecompilationException(
						"Anonymous class extends %s and implements %s",
						superType, interfaces.stream().map(Object::toString).collect(Collectors.joining(", "))
				);
			}

			var type = superType != null ? superType :
					!interfaces.isEmpty() ? interfaces.get(0) : ClassType.OBJECT;

			if (isEnumConstant) {
				writeEnumArgs(out, context);

			} else {
				out.recordSp("new").record(type, context);
				writeArgs(out, context, anonymous);
			}

			anonymous.writeBody(out.space());
			return;
		}

		if (isEnumConstant) {
			writeEnumArgs(out, context);

		} else {
			out.record(object, context, Priority.ZERO);
			writeArgs(out, context);
		}
	}

	private void writeArgs(DecompilationWriter out, MethodWriteContext context) {
		writeArgs(out, context, null);
	}

	/** Записывает аргументы.
	 * Если это вызов конструктора enum-класса, то пропускает первые два аргумента. */
	private void writeArgs(DecompilationWriter out, MethodWriteContext context,
	                       @Nullable DecompilingClass nestedClass) {

		if ((context.getClassModifiers() & Modifiers.ACC_ENUM) != 0 &&
			descriptor.isConstructor() &&
			descriptor.hostClass().equals(context.getThisType()) &&
			arguments.size() >= 2) {

			writeArgsSkipping2(out, context);

		} else {
			var visibleArgs = Optional.ofNullable(nestedClass)
					.or(() -> context.findClass(getNestedClassType()))
					.flatMap(clazz -> clazz.getClassContext().findMethod(descriptor))
					.map(method -> arguments.subList(method.getArgsStart(), method.getArgsEnd()))
					.orElse(arguments);

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
