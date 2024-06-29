package x590.newyava.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public enum ConsoleWriterFactory implements WriterFactory {
	INSTANCE;

	private final Writer writer = new BufferedWriter(new OutputStreamWriter(System.out));

	@Override
	public Writer getWriter(String className) throws IOException {
		writer.write("\n\n----------------------------------------------------------------\n\n");
		return writer;
	}

	@Override
	public void closeWriter(Writer writer) throws IOException {
		writer.flush();
	}
}
