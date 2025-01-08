package x590.newyava.context;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.descriptor.FieldDescriptor;
import x590.newyava.type.Type;

@Getter
public class ConstantWriteContext extends ContextProxy {

	private final @Nullable Type type;

	/** Если {@code true}, то разрешено опустить приведение типов, которое может быть выполнено неявно. */
	private final boolean implicitCastAllowed;

	/** Если {@code true}, то разрешено опустить приведение констант к {@code byte} и {@code short}. */
	private final boolean implicitByteShortCastAllowed;

	/** Если {@code true}, то разрешено заменять значение константой. */
	private final boolean constantsUsingAllowed;

	public ConstantWriteContext(Context context) {
		this(context, null);
	}

	public ConstantWriteContext(Context context, @Nullable Type type) {
		this(context, type, false, true, true);
	}

	public ConstantWriteContext(
			Context context, @Nullable Type type,
	        boolean implicitCastAllowed, boolean implicitByteShortCastAllowed, boolean constantsUsingAllowed
	) {
		super(context);
		this.type = type;
		this.implicitCastAllowed = implicitCastAllowed;
		this.implicitByteShortCastAllowed = implicitByteShortCastAllowed;
		this.constantsUsingAllowed = constantsUsingAllowed;
	}

	public ConstantWriteContext(ConstantWriteContext context, @Nullable Type type) {
		super(context);
		this.type = type;
		this.implicitCastAllowed = context.implicitCastAllowed;
		this.implicitByteShortCastAllowed = context.implicitByteShortCastAllowed;
		this.constantsUsingAllowed = context.constantsUsingAllowed;
	}
	
	private @Nullable FieldDescriptor filterConstant(@Nullable FieldDescriptor constant) {
		return constantsUsingAllowed || constant == null || !constant.hostClass().equals(getThisType()) ?
				constant : null;
	}

	@Override
	public @Nullable FieldDescriptor getConstant(int value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(byte value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(short value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(char value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(long value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(float value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(double value) {
		return filterConstant(super.getConstant(value));
	}

	@Override
	public @Nullable FieldDescriptor getConstant(String value) {
		return filterConstant(super.getConstant(value));
	}
}
