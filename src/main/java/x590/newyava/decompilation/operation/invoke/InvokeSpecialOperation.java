package x590.newyava.decompilation.operation.invoke;

import org.jetbrains.annotations.Nullable;
import x590.newyava.Modifiers;
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
import x590.newyava.type.Type;

import java.util.List;
import java.util.stream.Collectors;

public class InvokeSpecialOperation extends InvokeNonstaticOperation {

	private enum InvokeType {
		PLAIN, NEW, THIS, SUPER, SUPER_INTERFACE
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
	}

	private InvokeType getInvokeType(MethodContext context, MethodDescriptor descriptor) {
		if (descriptor.isConstructor()) {
			if (object.isThisRef()) {
				var hostClass = descriptor.hostClass();

				return  hostClass.equals(context.getThisType()) ? InvokeType.THIS :
						hostClass.equals(context.getSuperType()) ? InvokeType.SUPER : InvokeType.SUPER_INTERFACE;

			} else if (object instanceof NewOperation newOperation && context.popIfSame(newOperation)) {
				return InvokeType.NEW;
			}

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

	public boolean isNew() {
		return invokeType == InvokeType.NEW;
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		switch (invokeType) {
			case PLAIN -> super.write(out, context);
			case NEW -> writeNew(out, context, false);
			case THIS -> out.record("this");
			case SUPER -> out.record("super");
			case SUPER_INTERFACE -> out.record(descriptor.hostClass(), context).record(".super");
		}

		if (invokeType != InvokeType.PLAIN && invokeType != InvokeType.NEW) {
			writeArgs(out, context);
		}
	}


	public boolean canInlineEnumConstant() {
		return arguments.size() <= 2 && getAnonymousClassType() == null;
	}

	private @Nullable ClassType getAnonymousClassType() {
		return  invokeType == InvokeType.NEW &&
				returnType instanceof ClassType classType && classType.isAnonymous() ?
				classType : null;
	}

	/**
	 * Записывает операцию как создание нового объекта.
	 * Записывает тело анонимного класса, если оно есть.
	 * @param isEnumConstant если {@code true}, то записывает только аргументы
	 *    и тело анонимного класса, если они есть. Иначе записывает как<br>
	 *    {@code new <type>(args) { class body }}
	 */
	public void writeNew(DecompilationWriter out, MethodWriteContext context, boolean isEnumConstant) {
		var classType = getAnonymousClassType();

		if (classType != null) {
			var foundClass = context.findClass(classType);

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
					writeArgs(out, context);
				}

				anonymous.writeBody(out.space());

				return;
			}
		}

		if (isEnumConstant) {
			writeEnumArgs(out, context);

		} else {
			out.record(object, context, Priority.ZERO);
			writeArgs(out, context);
		}
	}

	private void writeArgs(DecompilationWriter out, MethodWriteContext context) {
		out.record('(').record(arguments, context, Priority.ZERO, ", ").record(')');
	}

	private void writeEnumArgs(DecompilationWriter out, MethodWriteContext context) {
		var args = arguments;

		if (args.size() > 2) {
			out .record('(')
				.record(args.subList(Math.min(args.size(), 2), args.size()), context, Priority.ZERO, ", ")
				.record(')');
		}
	}
}
