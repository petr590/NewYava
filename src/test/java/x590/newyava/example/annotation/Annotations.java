package x590.newyava.example.annotation;

import x590.newyava.example.enums.EnumExample;

import java.lang.annotation.*;

public final class Annotations {

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface ByteAnnotation {
		byte val();
		byte[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface ShortAnnotation {
		short val();
		short[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface CharAnnotation {
		char val();
		char[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface IntAnnotation {
		int val();
		int[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface LongAnnotation {
		long val();
		long[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface FloatAnnotation {
		float val();
		float[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface DoubleAnnotation {
		double val();
		double[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface BooleanAnnotation {
		boolean val();
		boolean[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface StringAnnotation {
		String val();
		String[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface EnumAnnotation {
		EnumExample val();
		EnumExample[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface ClassAnnotation {
		Class<?> val();
		Class<?>[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface AnnotationAnnotation {
		SomeAnnotation val();
		SomeAnnotation[] arr();
	}

	public @interface SomeAnnotation {}


	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	@Repeatable(RepeatableAnnotationContainer.class)
	public @interface RepeatableAnnotation {
		int value();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE})
	@Retention(RetentionPolicy.CLASS)
	public @interface RepeatableAnnotationContainer {
		RepeatableAnnotation[] value();
	}
}
