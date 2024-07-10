package x590.newyava.test.decompiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Элемент, который имеет эту аннотацию, содержит (или содержал) в себе баг декомпиляции.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Bug {
	State value() default State.NOT_FIXED;
}
