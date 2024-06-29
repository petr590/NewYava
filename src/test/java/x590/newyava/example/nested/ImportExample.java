package x590.newyava.example.nested;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.example.Main;
import x590.newyava.example.annotation.Annotations;

import java.util.Map;

@SuppressWarnings("all")
@Annotations.ClassAnnotation(val = ImportExample.Middle.class, arr = ImportExample.Middle.Inner.class)
public class ImportExample {
	@Test
	public void run() {
		Main.run(this, Config.builder().importNestedClasses(false).build());
	}

	private Map.Entry<?, ?> testImports(Middle middle, Middle.Inner inner) {
		return null;
	}

	private native x590.newyava.example.samename.ImportExample testConflicting1();
	private native x590.newyava.example.samename.Middle testConflicting2();
	private native x590.newyava.example.samename.Inner testConflicting3();
	private native NestedClassExample.Another testConflicting4();

	@Annotations.ClassAnnotation(val = Middle.Inner.class, arr = {})
	static class Middle {
		static class Inner {
			static {
				System.out.println(ImportExample.class);
				System.out.println(Middle.class);
				System.out.println(Inner.class);
			}
		}
	}
}
