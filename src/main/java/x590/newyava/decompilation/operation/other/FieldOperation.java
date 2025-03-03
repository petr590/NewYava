package x590.newyava.decompilation.operation.other;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.DecompilingField;
import x590.newyava.constant.ClassConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.variable.ILoadOperation;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Операция записи/чтения поля из объекта/класса
 */
@Getter
@EqualsAndHashCode(callSuper = true)
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

	/** @return операцию записи статического поля. */
	public static Operation putStatic(MethodContext context, FieldDescriptor descriptor, Operation value) {
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


	/** Дескриптор поля */
	private final FieldDescriptor descriptor;

	/** Экземпляр класса или {@code null}, если поле статическое */
	private final @Nullable Operation instance;

	/** Экземпляр класса, через который происходит обращение к статическому полю */
	@EqualsAndHashCode.Exclude
	private @Nullable ILoadOperation staticInstance;


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

	public boolean isStatic() {
		return instance == null;
	}


	/** @return {@code true}, если экземпляр класса является ссылкой на {@code this}. */
	public boolean isThisField() {
		return instance != null && instance.isThisRef();
	}

	@Override
	public boolean canUnite(MethodContext context, Operation prev) {
		if (isStatic()) {
			staticInstance = OperationUtils.getStaticInstance(descriptor.hostClass(), prev);
		}

		return staticInstance != null || super.canUnite(context, prev);
	}


	/** Поле, которое инициализирует данная операция */
	private @Nullable DecompilingField field;

	@Override
	public boolean initializeField(MethodContext context) {
		if ((instance == null || instance.isThisRef()) &&
			value != null && !value.usesAnyVariable() &&
			descriptor.hostClass().equals(context.getDescriptor().hostClass())) {

			var foundField = context.findField(descriptor);

			if (foundField.isPresent()) {
				this.field = foundField.get();
				return field.isStatic() ?
						field.addStaticInitializer(value) :
						field.addInstanceInitializer(value, context.getDescriptor());
			}
		}

		return false;
	}

	@Override
	public boolean isFieldInitialized() {
		return field != null && field.hasInitializer();
	}


	@Override
	public void inferType(Type ignored) {
		super.inferType(ignored);

		if (value != null) value.inferType(descriptor.type());
		if (instance != null) instance.inferType(descriptor.hostClass());
	}

	@Override
	public void initPossibleVarNames() {
		super.initPossibleVarNames();

		if (value != null) {
			value.addPossibleVarName(descriptor.name());
		}
	}

	@Override
	public Optional<String> getPossibleVarName() {
		return Optional.of(descriptor.name());
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
		super.addImports(context);

		if (instance != null) {
			context.addImportsFor(instance);
		} else {
			context.addImport(descriptor.hostClass());
		}
	}


	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		if (shortValue != null) {
			super.write(out, context);
		} else {
			writeTarget(out, context);
		}
	}

	@Override
	protected void writeTarget(DecompilationWriter out, MethodWriteContext context) {
		if (staticInstance != null) {
			out.record(staticInstance, context, Priority.DEFAULT).record('.').record(descriptor.name());
			return;
		}

		var instance = this.instance;
		var descriptor = this.descriptor;

		if (instance != null) {
			var foundField = context.findField(descriptor);

			if (foundField.isPresent()) {
				var field = foundField.get();

				if (field.isOuterInstance()) {
					out.record(descriptor.type(), context).record(".this");
					return;
				}

				if (field.bindedWithOuterVariable()) {
					out.record(field.getOuterVarRef().getName());
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
				instance.isThisRef() && (isGetter() || !context.isConstructor()) :
				descriptor.hostClass().equals(context.getThisType());
	}

	@Override
	public String toString() {
		Object target = instance != null ? instance : descriptor.hostClass();

		return value == null ?
				String.format("FieldOperation(%s.%s)", target, descriptor.name()) :
				String.format("FieldOperation(%s.%s = %s)", target, descriptor.name(), value);
	}
}
