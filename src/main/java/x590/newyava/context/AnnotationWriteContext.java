package x590.newyava.context;

import lombok.Getter;

public class AnnotationWriteContext extends ConstantWriteContext {
	@Getter
	private final boolean inline;

	public AnnotationWriteContext(Context context, boolean inline) {
		super(context);
		this.inline = inline;
	}
}
