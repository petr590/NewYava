package x590.newyava.example.annotation;

import org.junit.Test;
import x590.newyava.example.Main;

@SuppressWarnings("all")
public class AnnotationExample {
	@Test
	public void run() {
		Main.run(ExampleAnnotation.class);
	}
}
