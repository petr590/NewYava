package x590.newyava.example.variable;

import org.junit.Test;
import x590.newyava.example.Main;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings("all")
public class AnnotationsExample {
	@Test
	public void run() {
		Main.run(this);
	}

	public void foo(@ParamAnnotation @TypeUseAnnotation int x) {
		@VarAnnotation
		@TypeUseAnnotation
		int y = x / 2;

		System.out.println(y);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.LOCAL_VARIABLE)
	private @interface VarAnnotation {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	private @interface ParamAnnotation {}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	private @interface TypeUseAnnotation {}
}
