package x590.newyava.context;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.type.Type;

@Getter
public class ConstantWriteContext extends ContextProxy {

	private final @Nullable Type type;

	/** Если {@code true}, то разрешено опустить приведение типов, которое может быть выполнено неявно. */
	private final boolean implicitCastAllowed;

	/** Если {@code true}, то разрешено опустить приведение констант к {@code byte} и {@code short}. */
	private final boolean implicitByteShortCastAllowed;

	public ConstantWriteContext(Context context) {
		this(context, null, false, true);
	}

	public ConstantWriteContext(Context context, @Nullable Type type,
	                            boolean implicitCastAllowed, boolean implicitByteShortCastAllowed) {
		super(context);
		this.type = type;
		this.implicitCastAllowed = implicitCastAllowed;
		this.implicitByteShortCastAllowed = implicitByteShortCastAllowed;
	}

	public ConstantWriteContext(ConstantWriteContext context, @Nullable Type type) {
		super(context);
		this.type = type;
		this.implicitCastAllowed = context.implicitCastAllowed;
		this.implicitByteShortCastAllowed = context.implicitByteShortCastAllowed;
	}
}
