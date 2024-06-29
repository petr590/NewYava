package x590.newyava.test;

import org.jetbrains.annotations.Unmodifiable;
import org.junit.After;
import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;
import x590.newyava.io.BufferedFileWriterFactory;

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

	private static final String
			SOURCE = "/home/winch/IdeaProjects/NewYava/src/test/java/x590/newyava/example",
			COMPILED1 = "/tmp/newyava/compiled1",
			DECOMPILED = "/tmp/newyava/decompiled",
			COMPILED2 = "/tmp/newyava/compiled2";

	private static final @Unmodifiable List<String> WHITE_LIST = List.of(
			"x590/newyava/example/Main.java"
//			"x590/newyava/example/annotation/Annotations.java"
	);

	private static final @Unmodifiable List<String> BLACK_LIST = List.of(
			"x590/newyava/example/code/SwitchExample.java",
			"x590/newyava/example/annotation/AnnotationHangExample.java"
	);

	private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	private final StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

	private List<String> options(String outputDir) {
		return List.of(
				"-d", outputDir,
				"-Xlint:unchecked",
				"-classpath",
				"/home/winch/IdeaProjects/NewYava/src/fakeenv:" +
						"/home/winch/IdeaProjects/NewYava/src/test/java/:" +
						"/home/winch/IdeaProjects/NewYava/target/classes"
		);
	}



	private static @Unmodifiable List<File> getSourceFiles(String inputDir) {
		try (Stream<Path> paths = Files.walk(Paths.get(inputDir))) {
			return paths
					.filter(path ->
							WHITE_LIST.stream().anyMatch(path::endsWith) ||
							(path.toString().endsWith("Example.java") &&
							 BLACK_LIST.stream().noneMatch(path::endsWith)))
					.map(Path::toFile)
					.filter(File::isFile)
					.toList();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static @Unmodifiable List<String> getCompiledClassNames() {
		try (Stream<Path> paths = Files.walk(Paths.get(COMPILED1))) {
			return paths
					.filter(path -> path.toString().endsWith(".class"))
					.map(path -> path.toString()
							.replaceAll("^" + COMPILED1 + "/(.*)\\.class$", "$1"))
					.filter(name -> name.startsWith("x590/newyava/"))
					.toList();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}


	@Test
	public void run() throws IOException {
		compile(SOURCE, COMPILED1);

		new Decompiler(Config.defaultConfig(), new BufferedFileWriterFactory(DECOMPILED))
				.run(getCompiledClassNames().stream(), Decompiler.fileResourceGetter(COMPILED1));

		compile(DECOMPILED, COMPILED2);
	}


	private void compile(String inputDir, String outputDir) {
		Iterable<? extends JavaFileObject> compilationUnits =
				fileManager.getJavaFileObjectsFromFiles(getSourceFiles(inputDir));

		JavaCompiler.CompilationTask task = compiler.getTask(
				null, fileManager, null, options(outputDir), null, compilationUnits
		);

		task.call();
	}


	@After
	public void after() throws IOException {
		fileManager.close();
	}
}
