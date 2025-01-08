package x590.newyava.io;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BufferedFileWriterFactory implements WriterFactory {
	private final String directory;

	public BufferedFileWriterFactory(String directory) {
		this.directory = directory.endsWith("/") ? directory : directory + "/";
	}

	@Override
	public Writer getWriter(String className) throws IOException {
		String javaFile = directory + className.replace('.', '/') + ".java";
		Files.createDirectories(Paths.get(javaFile).getParent());
		return new FileWriter(javaFile);
	}

	@Override
	public void closeWriter(Writer writer) throws IOException {
		writer.close();
	}
}
