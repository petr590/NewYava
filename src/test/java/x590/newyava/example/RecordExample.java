package x590.newyava.example;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@SuppressWarnings("unused")
public record RecordExample(
		int i, long l, float f, double d,
        @Nullable String s,
        @FieldAnnotation @RecordComponentAnnotation List<String> list
) {
	private static final int CONSTANT = 1;

	public RecordExample() {
		this(1, 1, 1, 1, "", List.of());
	}

	public RecordExample {
		System.out.println(d);
	}

	public static void main(String[] args) {
		Main.run(RecordExample.class);
	}

//	@Override
//	public @Nullable List<String> list() {
//		return list;
//	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface FieldAnnotation {}

	@Target(ElementType.RECORD_COMPONENT)
	@Retention(RetentionPolicy.RUNTIME)
	private @interface RecordComponentAnnotation {}
}
