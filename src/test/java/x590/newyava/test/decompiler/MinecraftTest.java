package x590.newyava.test.decompiler;

import org.jetbrains.annotations.Unmodifiable;
import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;
import x590.newyava.io.BufferedFileWriterFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class MinecraftTest {
	static final String
			SRC_DIR = "/home/winch/IdeaProjects/MCP-Reborn/build/classes/java/main",
			DST_DIR = "/tmp/minecraft";


	static @Unmodifiable List<String> getCompiledClassNames() {
		try (Stream<Path> paths = Files.walk(Paths.get(SRC_DIR))) {
			return paths.map(Path::toString)
					.filter(path -> path.endsWith(".class"))
					.map(path -> path.substring(0, path.length() - ".class".length()))
					.toList();

		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/** @param className имя класса в формате {@code "java.lang.Object"} или {@code "java/lang/Object"} */
	static @Unmodifiable String getCompiledClassName(String className) {
		return Path.of(SRC_DIR, className.replace('.', '/')).toString();
	}

	@Test
	public void run() {
		new Decompiler(Config.defaultConfig(), new BufferedFileWriterFactory(DST_DIR))
				.run(getCompiledClassNames().stream(), Decompiler.fileResourceGetter(""));

//		new Decompiler(Config.defaultConfig(), new BufferedFileWriterFactory(DST_DIR))
//				.run(Stream.of("/home/winch/IdeaProjects/MCP-Reborn/build/classes/java/main/net/minecraft/world/level/levelgen/structure/structures/MineshaftPieces$MineShaftStairs"), Decompiler.fileResourceGetter(""));
	}
}
