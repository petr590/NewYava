package x590.newyava.example.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static java.lang.annotation.ElementType.*;

@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT})
public @interface ExampleAnnotation {
	int i();

	long l() default -1;

	float f() default 0.5f;

	String[] strings() default { "gg", "bb" };

	Class<?> cls() default List.class;
}
