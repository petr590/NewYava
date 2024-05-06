package x590.newyava.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.TriConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import x590.newyava.ContextualTypeWritable;
import x590.newyava.ContextualWritable;
import x590.newyava.Writable;
import x590.newyava.context.Context;
import x590.newyava.decompilation.operation.Associativity;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.type.Type;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.function.ObjIntConsumer;

@RequiredArgsConstructor
public class DecompilationWriter extends Writer {

	private final Writer out;

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

	/* --------------------------------------------- ContextualWritable --------------------------------------------- */
	public DecompilationWriter record(ContextualWritable writable, Context context) {
		writable.write(this, context);
		return this;
	}

	public DecompilationWriter record(Collection<? extends ContextualWritable> writables, Context context) {
		writables.forEach(writable -> record(writable, context));
		return this;
	}
	public DecompilationWriter record(Collection<? extends ContextualWritable> writables,
	                                  Context context, String separator) {

		return record(writables, separator, 0, (writable, index) -> record(writable, context));
	}

	/* ------------------------------------------- ContextualTypeWritable ------------------------------------------- */
	public DecompilationWriter record(ContextualTypeWritable writable, Context context, @Nullable Type type) {
		writable.write(this, context, type);
		return this;
	}

	public DecompilationWriter record(Collection<? extends ContextualTypeWritable> writables,
	                                  Context context, @Nullable Type type, String separator) {
		return record(writables, separator, (writable, index) -> writable.write(this, context, type));
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
	public DecompilationWriter record(Scope scope, Context context) {
		return record(scope, context, Priority.ZERO);
	}

	public DecompilationWriter record(Operation operation, Context context, Priority priority) {
		return record(operation, context, priority, priority.getAssociativity());
	}

	public <T extends Operation> DecompilationWriter record(
			T operation, Context context, Priority priority,
			TriConsumer<T, DecompilationWriter, Context> writer
	) {
		return record(operation, context, priority, priority.getAssociativity(), writer);
	}

	public DecompilationWriter record(
			Operation operation, Context context, Priority priority, Associativity side
	) {
		return record(operation, context, priority, side, Operation::write);
	}

	public <T extends Operation> DecompilationWriter record(
			T operation, Context context, Priority priority, Associativity side,
	        TriConsumer<T, DecompilationWriter, Context> writer
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
			Context context, Priority priority, String separator
	) {
		return record(operations, context, priority, separator, Operation::write);
	}

	public <T extends Operation> DecompilationWriter record(
			Collection<? extends T> operations, Context context, Priority priority,
			String separator, TriConsumer<T, DecompilationWriter, Context> writer
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


	/* -------------------------------------------------- recordSp -------------------------------------------------- */
	public DecompilationWriter recordSp() {
		return record(' ');
	}

	public DecompilationWriter recordSp(String str) {
		return record(str).record(' ');
	}

	public DecompilationWriter recordSp(ContextualWritable writable, Context context) {
		return record(writable, context).record(' ');
	}

	public DecompilationWriter recordSp(char ch) {
		return record(ch).record(' ');
	}

	/* --------------------------------------------------- other --------------------------------------------------- */
	public DecompilationWriter ln() {
		return record('\n');
	}

	public boolean lnIf(boolean condition) {
		if (condition) ln();
		return condition;
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}
}
