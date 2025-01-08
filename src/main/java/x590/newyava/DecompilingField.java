package x590.newyava;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.annotation.DecompilingAnnotation;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.operation.invoke.InvokeSpecialOperation;
import x590.newyava.decompilation.operation.other.DummyOperation;
import x590.newyava.decompilation.variable.VariableReference;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.exception.IllegalModifiersException;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.modifiers.EntryType;
import x590.newyava.type.Type;
import x590.newyava.visitor.DecompileFieldVisitor;

import java.util.*;

import static x590.newyava.Literals.*;
import static x590.newyava.modifiers.Modifiers.*;

/**
 * Декомпилируемое поле
 */
@Getter
public class DecompilingField implements IField, ContextualWritable, Importable {
	private final int modifiers;

	private final FieldDescriptor descriptor, visibleDescriptor;

	private final @Unmodifiable Set<DecompilingAnnotation> annotations;


	private final @Nullable Object constantValue;

	/** Инициализатор поля. Если равен {@link x590.newyava.decompilation.operation.other.DummyOperation#INSTANCE
	 * DummyOperation.INSTANCE}, то поле не имеет и не может иметь инициализатор. */
	private @Nullable Operation initializer;

	private final Set<MethodDescriptor> constructors = new HashSet<>();

	/** Если {@code true}, то это поле является ссылкой на {@code this} внешнего класса. */
	private boolean isOuterInstance;

	/** Если не {@code null}, то это поле связано с внешней переменной. */
	private @Nullable VariableReference outerVarRef;

	public DecompilingField(DecompileFieldVisitor visitor) {
		this.modifiers         = visitor.getModifiers();
		this.descriptor        = visitor.getDescriptor();
		this.visibleDescriptor = visitor.getVisibleDescriptor();
		this.annotations       = visitor.getAnnotations();
		this.constantValue     = visitor.getConstantValue();
		this.initializer       = visitor.getInitializer();
	}

	/** Можно ли оставить поле в классе */
	public boolean keep() {
		return !isSynthetic();
	}


	/**
	 * Устанавливает инициализатор поля, если он не установлен.
	 * @return {@code true}, если инициализатор поля был установлен данным вызовом, иначе {@code false}.
	 * @throws IllegalStateException если поле нестатическое.
	 */
	public boolean addStaticInitializer(Operation value) {
		if (!isStatic()) {
			throw new IllegalStateException("Cannot add static initializer to non-static field");
		}

		if (initializer == null) {
			initializer = value;
			return true;
		}

		return false;
	}

	/**
	 * Устанавливает инициализатор поля, если он не установлен.
	 * @param constructor дескриптор конструктора, из которого поле инициализируется.
	 * @return {@code true}, если инициализатор поля был установлен данным вызовом, иначе {@code false}.
	 * @throws IllegalStateException если поле статическое.
	 * @throws IllegalArgumentException если дескриптор конструктора не подходит к классу, в котором объявлено поле.
	 */
	public boolean addInstanceInitializer(Operation value, MethodDescriptor constructor) {
		if (isStatic()) {
			throw new IllegalStateException("Cannot add instance initializer to static field");
		}

		if (!constructor.isConstructor() || !constructor.hostClass().equals(descriptor.hostClass())) {
			throw new IllegalArgumentException(String.format(
					"Illegal constructor descriptor for class %s: %s",
					descriptor.hostClass(), constructor
			));
		}

		if (initializer == null) { // Первая инициализация
			initializer = value;
			constructors.add(constructor);
			return true;
		}

		if (constructors.contains(constructor)) { // Повторная инициализация тем же конструктором
			return false;
		}

		if (initializer.equals(value)) { // Инициализация таким же значением из другого конструктора
			constructors.add(constructor);
			return true;
		}

		// Инициализация другим значением из другого конструктора
		initializer = DummyOperation.INSTANCE;
		return false;
	}

	public void afterDecompilation(@Unmodifiable Set<MethodDescriptor> constructors) {
		if (!isStatic() && hasInitializer() && !this.constructors.containsAll(constructors)) {
			initializer = DummyOperation.INSTANCE;
		}
	}

	public boolean hasInitializer() {
		return initializer != null && initializer != DummyOperation.INSTANCE;
	}


	/** Помечает поле как экземпляр внешнего класса. */
	public void makeOuterInstance() {
		isOuterInstance = true;
	}

	/** Связывает поле с внешней переменной. */
	public void bindWithOuterVariable(VariableReference outerVarRef) {
		this.outerVarRef = outerVarRef;
	}

	public boolean bindedWithOuterVariable() {
		return outerVarRef != null;
	}

	public VariableReference getOuterVarRef() {
		return Objects.requireNonNull(outerVarRef);
	}

	public void beforeVariablesInit(Context context) {
		if (initializer != null) {
			initializer.beforeVariablesInit(context, null);
		}
	}

	public void inferVariableTypes() {
		if (initializer != null) {
			initializer.inferType(visibleDescriptor.type());
			initializer.allowImplicitCast();
			initializer.denyConstantsUsing();
		}
	}


	/* -------------------------------------------------- Enum map -------------------------------------------------- */

	private @Nullable Int2ObjectMap<FieldDescriptor> enumMap;

	public @Nullable @UnmodifiableView Int2ObjectMap<FieldDescriptor> getEnumMap() {
		return enumMap == null ? null : Int2ObjectMaps.unmodifiable(enumMap);
	}


	/**
	 * Инициализирует {@link #enumMap}, если он ещё не инициализирован, и добавляет в него
	 * переданный дескриптор по id. Если в {@link #enumMap} уже есть дескриптор, то
	 * проверяет совпадение класса и типа.
	 */
	public void setEnumEntry(int id, FieldDescriptor descriptor) {
		if (enumMap == null) {
			enumMap = new Int2ObjectOpenHashMap<>();
			enumMap.put(id, descriptor);
			return;
		}

		var existsDescriptor = enumMap.values().iterator().next();

		if (existsDescriptor.hostClass().equals(descriptor.hostClass()) &&
			existsDescriptor.type().equals(descriptor.type())) {

			enumMap.put(id, descriptor);
		}
	}

	/* ---------------------------------------------------- write --------------------------------------------------- */

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(visibleDescriptor).addImportsFor(annotations).addImportsFor(initializer);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		out.ln().indent();

		DecompilingAnnotation.writeAnnotations(out, context, annotations);

		writeModifiers(out, context);

		out.record(visibleDescriptor, context);

		if (hasInitializer()) {
			out.record(" = ").record(
					initializer, new MethodWriteContext(context), Priority.ZERO,
					Type.isArray(descriptor.type()) ? Operation::writeAsArrayInitializer : Operation::write
			);
		}
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

			if (invokeSpecial.getArguments().size() > 2) {
				out.record(" ".repeat(Math.max(0, minWidth - descriptor.name().length())));
			}

			invokeSpecial.writeNew(out, new MethodWriteContext(context), true);
		}
	}

	public void writeAsRecordComponent(DecompilationWriter out, Context context) {
		DecompilingAnnotation.writeAnnotations(out, context, annotations, true);
		out.record(visibleDescriptor.type(), context).space().record(visibleDescriptor.name());
	}

	@Override
	public String toString() {
		return descriptor.toString();
	}

	public boolean canUnite(DecompilingField other) {
		return modifiers == other.modifiers &&
				descriptor.hostClass().equals(other.descriptor.hostClass()) &&
				descriptor.type().equals(other.descriptor.type()) &&
				annotations.equals(other.annotations) &&
				!hasInitializer() && !other.hasInitializer();
	}
}
