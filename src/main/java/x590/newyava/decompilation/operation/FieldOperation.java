package x590.newyava.decompilation.operation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.constant.ClassConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Операция записи/чтения поля из объекта/класса
 */
@Getter
public class FieldOperation extends AssignOperation {

	/** @return операцию чтения статического поля.
	 * Если это поле {@code TYPE} одного из классов-обёрток,
	 * оно заменяется на соответствующий класс примитива. */
	public static Operation getStatic(MethodContext context, FieldDescriptor descriptor) {
		if (descriptor.name().equals("TYPE")) {
			var hostClass = descriptor.hostClass();

			if (hostClass.equals(ClassType.BYTE))      return new LdcOperation(ClassConstant.BYTE);
			if (hostClass.equals(ClassType.SHORT))     return new LdcOperation(ClassConstant.SHORT);
			if (hostClass.equals(ClassType.CHARACTER)) return new LdcOperation(ClassConstant.CHAR);
			if (hostClass.equals(ClassType.INTEGER))   return new LdcOperation(ClassConstant.INT);
			if (hostClass.equals(ClassType.LONG))      return new LdcOperation(ClassConstant.LONG);
			if (hostClass.equals(ClassType.FLOAT))     return new LdcOperation(ClassConstant.FLOAT);
			if (hostClass.equals(ClassType.DOUBLE))    return new LdcOperation(ClassConstant.DOUBLE);
			if (hostClass.equals(ClassType.BOOLEAN))   return new LdcOperation(ClassConstant.BOOLEAN);
			if (hostClass.equals(ClassType.VOID))      return new LdcOperation(ClassConstant.VOID);
		}

		return new FieldOperation(context, descriptor, null, null);
	}

	/** @return операцию записи статического поля. Если возможно, то добавляет инициализатор
	 * статическому полю, и тогда возвращает {@code null} */
	public static @Nullable Operation putStatic(MethodContext context, FieldDescriptor descriptor, Operation value) {
		if (!value.usesAnyVariable() &&
			context.getDescriptor().isStaticInitializer() &&
			descriptor.hostClass().equals(context.getDescriptor().hostClass())) {

			var foundField = context.findField(descriptor);

			if (foundField.isPresent()) {
				var field = foundField.get();

				if (field.setInitializer(value) || !field.keep()) {
					return null;
				}
			}
		}

		return new FieldOperation(context, descriptor, value, null);
	}

	/** @return операцию чтения поля объекта. */
	public static Operation getField(MethodContext context, FieldDescriptor descriptor, Operation instance) {
		return new FieldOperation(context, descriptor, null, instance);
	}

	/** @return операцию записи поля объекта. */
	public static Operation putField(MethodContext context, FieldDescriptor descriptor,
	                                 Operation value, Operation instance) {

		return new FieldOperation(context, descriptor, value, instance);
	}


	private final FieldDescriptor descriptor;

	private final @Nullable Operation instance;

	private FieldOperation(MethodContext context, FieldDescriptor descriptor,
	                       @Nullable Operation value, @Nullable Operation instance) {
		super(
				context, value, descriptor.type(),
				operation -> operation instanceof FieldOperation getField &&
						getField.isGetter() &&
						getField.getDescriptor().equals(descriptor) &&
						Objects.equals(getField.getInstance(), instance)
		);

		this.descriptor = descriptor;
		this.instance = instance;
	}

	public boolean isGetter() {
		return value == null;
	}

	public boolean isSetter() {
		return value != null;
	}

	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);

		if (value != null) value.inferType(descriptor.type());
		if (instance != null) instance.inferType(descriptor.hostClass());
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		List<Operation> operations = new ArrayList<>();

		if (value != null) operations.add(value);
		if (instance != null) operations.add(instance);

		return operations;
	}

	@Override
	public void addImports(ClassContext context) {
		if (instance == null) {
			context.addImport(descriptor.hostClass());
		}

		context.addImportsFor(instance).addImportsFor(value);
	}


	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (value != null) {
			super.write(out, context);
		} else {
			writeTarget(out, context);
		}
	}

	@Override
	protected void writeTarget(DecompilationWriter out, MethodWriteContext context) {
		var instance = this.instance;
		var descriptor = this.descriptor;

		if (instance != null) {
			var foundField = context.findClass(descriptor.hostClass())
					.flatMap(clazz -> clazz.getFields().stream().filter(field -> field.getDescriptor().equals(descriptor)).findFirst());

			if (foundField.isPresent()) {
				var field = foundField.get();

				if (field.isOuterInstance()) {
					out.record(descriptor.type(), context).record(".this");
					return;
				}

				if (field.isOuterVariable()) {
					out.record(field.getOuterVarName());
					return;
				}
			}
		}

		if (context.getConfig().canOmitThisAndClass() &&
			isThisOrThisClass(context) &&
			!context.hasVarWithName(descriptor.name())) {

			out.record(descriptor.name());
			return;
		}

		if (instance != null) {
			out.record(instance, context, getPriority());
		} else {
			out.record(descriptor.hostClass(), context);
		}

		out.record('.').record(descriptor.name());
	}

	private boolean isThisOrThisClass(MethodWriteContext context) {
		return instance != null ?
				instance.isThisRef() :
				descriptor.hostClass().equals(context.getThisType());
	}

	@Override
	public String toString() {
		var target = instance != null ? instance : descriptor.hostClass();

		return value == null ?
				String.format("FieldOperation %08x(%s.%s)", hashCode(), target, descriptor.name()) :
				String.format("FieldOperation %08x(%s.%s = %s)", hashCode(), target, descriptor.name(), value);
	}
}
