package x590.newyava.example.annotation;

import x590.newyava.example.ExampleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class Annotations {

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface ByteAnnotation {
		byte val();
		byte[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface ShortAnnotation {
		short val();
		short[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface CharAnnotation {
		char val();
		char[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface IntAnnotation {
		int val();
		int[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface LongAnnotation {
		long val();
		long[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface FloatAnnotation {
		float val();
		float[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface DoubleAnnotation {
		double val();
		double[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface BooleanAnnotation {
		boolean val();
		boolean[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface StringAnnotation {
		String val();
		String[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface EnumAnnotation {
		ExampleEnum val();
		ExampleEnum[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface ClassAnnotation {
		Class<?> val();
		Class<?>[] arr();
	}

	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@Retention(RetentionPolicy.CLASS)
	@interface AnnotationAnnotation {
		SomeAnnotation val();
		SomeAnnotation[] arr();
	}

	@interface SomeAnnotation {}
}
