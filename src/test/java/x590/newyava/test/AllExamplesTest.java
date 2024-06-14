package x590.newyava.test;

import org.jetbrains.annotations.Unmodifiable;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class AllExamplesTest {

	private static final String SEARCH_DIRECTORY = "/home/winch/IdeaProjects/NewYava/src/test/java/x590/newyava/example";

	private static @Unmodifiable List<File> getSourceFiles() {
		try (Stream<Path> paths = Files.walk(Paths.get(SEARCH_DIRECTORY))) {
			return paths
					.filter(path -> path.toString().endsWith("Example.java") ||
							path.endsWith("x590/newyava/example/Main.java"))
					.map(Path::toFile)
					.filter(File::isFile)
					.toList();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Test
	public void run() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		var options = List.of(
				"-d", "/tmp",
				"-classpath", "/home/winch/IdeaProjects/NewYava/target/classes"
		);

		System.out.println(getSourceFiles());

		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(getSourceFiles());

		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);
		boolean result = task.call();

		if (result) {
			System.out.println("Compilation was successful");
		} else {
			System.out.println("Compilation failed");
		}

		fileManager.close();
	}
}
