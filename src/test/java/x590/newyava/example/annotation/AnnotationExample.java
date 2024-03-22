package x590.newyava.example.annotation;

import java.util.List;

public @interface AnnotationExample {
	int i();

	long l() default -1;

	String[] strings() default { "gg", "bb" };

	Class<?> cls() default List.class;
}
