package x590.newyava.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import x590.newyava.context.Context;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Associativity;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.scope.Scope;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.function.ObjIntConsumer;

@RequiredArgsConstructor
public class DecompilationWriter extends Writer {

	private final WriterFactory writerFactory;

	private Writer out;

	public void openWriter(String className) throws IOException {
		out = writerFactory.getWriter(className);
	}

	public void closeWriter() throws IOException {
		if (out != null) {
			try {
				writerFactory.closeWriter(out);
			} finally {
				out = null;
			}
		}
	}

	/* --------------------------------------------------- indent --------------------------------------------------- */
	public static final String DEFAULT_INDENT = "    ";

	public static final int INDENTS_CACHE_SIZE = 64;

	private static final String[] INDENTS_CACHE = new String[INDENTS_CACHE_SIZE];

	static {
		INDENTS_CACHE[0] = "";
	}

	private int indentWidth = 0;

	@Getter
	private String indent = "";

	public DecompilationWriter incIndent() {
		return incIndent(1);
	}

	public DecompilationWriter decIndent() {
		return decIndent(1);
	}

	public DecompilationWriter incIndent(int num) {
		return setIndent(indentWidth + num);
	}

	public DecompilationWriter decIndent(int num) {
		return setIndent(indentWidth - num);
	}

	public DecompilationWriter setIndent(int width) {
		if (width < 0) {
			throw new IllegalStateException("Negative indent width " + width);
		}

		this.indentWidth = width;

		if (width < INDENTS_CACHE_SIZE) {
			String indent = INDENTS_CACHE[width];

			if (indent == null)
				indent = INDENTS_CACHE[width] = DEFAULT_INDENT.repeat(width);

			this.indent = indent;

		} else {
			indent = DEFAULT_INDENT.repeat(width);
		}

		return this;
	}

	public DecompilationWriter indent() {
		return record(indent);
	}

	@Override
	public void write(char @NotNull[] buffer, int off, int len) throws IOException {
		out.write(buffer, off, len);
	}

	/* --------------------------------------------------- record --------------------------------------------------- */

	public DecompilationWriter record(char ch) {
		try {
			out.write(ch);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return this;
	}

	public DecompilationWriter record(String str) {
		try {
			out.write(str);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return this;
	}

	public DecompilationWriter recordN(String str, int n) {
		for (int i = 0; i < n; i++) {
			record(str);
		}

		return this;
	}

	public DecompilationWriter record(Collection<? extends String> strings, String separator) {
		boolean wrote = false;

		for (String str : strings) {
			if (wrote)
				record(separator);

			record(str);
			wrote = true;
		}

		return this;
	}

	/* -------------------------------------------------- Writable -------------------------------------------------- */
	public DecompilationWriter record(Writable writable) {
		writable.write(this);
		return this;
	}

	public DecompilationWriter record(Collection<? extends Writable> writables) {
		writables.forEach(this::record);
		return this;
	}

	/* ---------------------------------------------- GenericWritable ----------------------------------------------- */
	public <C extends Context> DecompilationWriter record(GenericWritable<C> writable, C context) {
		writable.write(this, context);
		return this;
	}

	public <C extends Context> DecompilationWriter record(
			Collection<? extends GenericWritable<C>> writables,
			C context
	) {
		writables.forEach(writable -> record(writable, context));
		return this;
	}

	public <C extends Context> DecompilationWriter record(
			Collection<? extends GenericWritable<C>> writables,
	        C context, String separator
	) {
		return record(writables, separator, (writable, index) -> writable.write(this, context));
	}


	/* ---------------------------------------------- Common generics ---------------------------------------------- */

	/**
	 * Работает также, как и {@link #record(Collection, String, int, ObjIntConsumer)}.
	 * {@code separator} равен {@code ""}.
	 * {@code startIndex} равен 0.
	 */
	public <T> DecompilationWriter record(Collection<? extends T> writables, ObjIntConsumer<T> writer) {
		return record(writables, "", 0, writer);
	}

	/**
	 * Работает также, как и {@link #record(Collection, String, int, ObjIntConsumer)}.
	 * {@code startIndex} равен 0.
	 */
	public <T> DecompilationWriter record(Collection<? extends T> writables, String separator,
	                                      ObjIntConsumer<T> writer) {
		return record(writables, separator, 0, writer);
	}

	/**
	 * Вызывает функцию {@code writer} для каждого объекта из {@code writables},
	 * начиная с индекса {@code startIndex}. В функцию передаётся объект и его индекс в коллекции.
	 * Между вызовами записывает разделитель {@code separator}.
	 * @return {@code this}
	 */
	public <T> DecompilationWriter record(
			Collection<? extends T> writables, String separator, int startIndex, ObjIntConsumer<T> writer
	) {
		int i = 0;

		var iterator = writables.iterator();

		for (; i < startIndex && iterator.hasNext(); i++) {
			iterator.next();
		}

		for (; iterator.hasNext(); i++) {
			if (i > startIndex) record(separator);
			writer.accept(iterator.next(), i);
		}

		return this;
	}


	/* ---------------------------------------------- Operation, Scope ---------------------------------------------- */
	public DecompilationWriter record(Scope scope, MethodWriteContext context) {
		return record(scope, context, Priority.ZERO);
	}

	public DecompilationWriter record(Operation operation, MethodWriteContext context, Priority priority) {
		return record(operation, context, priority, priority.getAssociativity());
	}

	public <T extends Operation> DecompilationWriter record(
			T operation, MethodWriteContext context, Priority priority,
			TriConsumer<T, DecompilationWriter, MethodWriteContext> writer
	) {
		return record(operation, context, priority, priority.getAssociativity(), writer);
	}

	public DecompilationWriter record(
			Operation operation, MethodWriteContext context, Priority priority, Associativity side
	) {
		return record(operation, context, priority, side, Operation::write);
	}

	public <T extends Operation> DecompilationWriter record(
			T operation, MethodWriteContext context, Priority priority, Associativity side,
	        TriConsumer<T, DecompilationWriter, MethodWriteContext> writer
	) {
		var selfPriority = operation.getPriority();

		if (selfPriority.lessThan(priority) ||
			selfPriority == priority && side != priority.getAssociativity()) {

			writer.accept(operation, this.record('('), context);
			record(')');
		} else {
			writer.accept(operation, this, context);
		}

		return this;
	}

	public DecompilationWriter record(
			Collection<? extends Operation> operations,
			MethodWriteContext context, Priority priority, String separator
	) {
		return record(operations, context, priority, separator, Operation::write);
	}

	public <T extends Operation> DecompilationWriter record(
			Collection<? extends T> operations, MethodWriteContext context, Priority priority,
			String separator, TriConsumer<T, DecompilationWriter, MethodWriteContext> writer
	) {
		if (!operations.isEmpty()) {
			record(operations.iterator().next(), context, priority, priority.getAssociativity(), writer);

			if (operations.size() > 1) {
				var opposite = priority.getAssociativity().opposite();

				operations.stream().skip(1).forEach(operation -> record(separator)
						.record(operation, context, priority, opposite, writer));
			}
		}

		return this;
	}


	/* --------------------------------------------------- space --------------------------------------------------- */

	/** Записывает пробел */
	public DecompilationWriter space() {
		return record(' ');
	}

	/** Записывает строку, добавляя пробел в конце */
	public DecompilationWriter recordSp(String str) {
		return record(str).record(' ');
	}

	/** Записывает {@link ContextualWritable}, добавляя пробел в конце */
	public DecompilationWriter recordSp(ContextualWritable writable, Context context) {
		return record(writable, context).record(' ');
	}

	/** Записывает строку, добавляя пробелы в начале и в конце */
	public DecompilationWriter wrapSpaces(String str) {
		return record(' ').record(str).record(' ');
	}

	/* ----------------------------------------------------- ln ----------------------------------------------------- */

	/** Записывает перенос строки */
	public DecompilationWriter ln() {
		return record('\n');
	}

	/** Записывает перенос строки если выполнено условие */
	public boolean lnIf(boolean condition) {
		if (condition) ln();
		return condition;
	}

	/* ------------------------------------------------ flush, close ------------------------------------------------ */
	@Override
	public void flush() throws IOException {
		if (out != null) {
			out.flush();
		}
	}

	@Override
	public void close() throws IOException {
		writerFactory.close();
		out = null;
	}
}
