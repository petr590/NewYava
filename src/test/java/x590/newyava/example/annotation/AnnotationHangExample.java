package x590.newyava.example.annotation;

import org.junit.Test;
import x590.newyava.Main;
import x590.newyava.example.ExampleEnum;

import java.util.List;
import java.util.Map;

import static x590.newyava.example.annotation.Annotations.*;

@ByteAnnotation(val = 1, arr = {1})
@ShortAnnotation(val = 1, arr = {1})
@CharAnnotation(val = 1, arr = {1})
@IntAnnotation(val = 1, arr = {1})
@LongAnnotation(val = 1, arr = {1})
@FloatAnnotation(val = 1, arr = {1})
@DoubleAnnotation(val = 1, arr = {1})
@BooleanAnnotation(val = true, arr = {true})
@StringAnnotation(val = "hello", arr = {"hello"})
@EnumAnnotation(val = ExampleEnum.A, arr = {ExampleEnum.A})
@ClassAnnotation(val = int.class, arr = {int.class})
@AnnotationAnnotation(val = @SomeAnnotation, arr = {@SomeAnnotation})
@SuppressWarnings("all")
public class AnnotationHangExample {
	@Test
	public void run() {
		Main.run(this);
	}

	@ClassAnnotation(val = List.class, arr = {Map.class})
	public static int field;

	@ClassAnnotation(val = short.class, arr = {long[].class})
	public static void method() {}
}
