package x590.newyava.context;

import lombok.Getter;
import x590.newyava.type.Type;

@Getter
public class ConstantWriteContext extends ContextProxy {

	private final Type type;

	private final boolean implicitCastAllowed;


	public ConstantWriteContext(Context context) {
		this(context, null, false);
	}

	public ConstantWriteContext(Context context, Type type, boolean implicitCastAllowed) {
		super(context);
		this.type = type;
		this.implicitCastAllowed = implicitCastAllowed;
	}

	public ConstantWriteContext(ConstantWriteContext context, Type type) {
		super(context);
		this.type = type;
		this.implicitCastAllowed = context.implicitCastAllowed;
	}
}
