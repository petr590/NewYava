package x590.newyava.io;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import x590.newyava.ContextualWritable;
import x590.newyava.Writable;
import x590.newyava.context.ClassContext;
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

	private final Writer out;

	public static final String DEFAULT_INDENT = "    ";

	public static final int INDENTS_CACHE_SIZE = 64;

	private static final String[] INDENTS_CACHE = new String[INDENTS_CACHE_SIZE];

	static {
		INDENTS_CACHE[0] = "";
	}

	private int indentWidth = 0;
	private String indent = "";

	@Override
	public void write(char @NotNull[] buffer, int off, int len) throws IOException {
		out.write(buffer, off, len);
	}

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

	public DecompilationWriter record(String str) {
		try {
			out.write(str);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return this;
	}

	public DecompilationWriter record(char ch) {
		try {
			out.write(ch);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}

		return this;
	}

	public DecompilationWriter record(Writable writable) {
		writable.write(this);
		return this;
	}

	public DecompilationWriter record(ContextualWritable writable, ClassContext context) {
		writable.write(this, context);
		return this;
	}

	public DecompilationWriter record(Collection<? extends Writable> writables) {
		writables.forEach(this::record);
		return this;
	}

	public DecompilationWriter record(Collection<? extends ContextualWritable> writables, ClassContext context) {
		writables.forEach(writable -> record(writable, context));
		return this;
	}

	public DecompilationWriter record(Collection<? extends ContextualWritable> writables, ClassContext context, String separator) {
		if (!writables.isEmpty()) {
			record(writables.iterator().next(), context);
			writables.stream().skip(1).forEach(writable -> record(separator).record(writable, context));
		}

		return this;
	}

	public <T extends ContextualWritable> DecompilationWriter record(
			Collection<? extends T> writables, ClassContext context,
	        String separator, WriteFunction<T> writer
	) {
		int i = 0;

		for (var iterator = writables.iterator(); iterator.hasNext(); i++) {
			if (i != 0) record(separator);
			writer.write(this, iterator.next(), context, i);
		}

		return this;
	}

	public <T extends ContextualWritable> DecompilationWriter record(
			Collection<? extends T> writables, String separator, ObjIntConsumer<T> writer
	) {
		int i = 0;

		for (var iterator = writables.iterator(); iterator.hasNext(); i++) {
			if (i != 0) record(separator);
			writer.accept(iterator.next(), i);
		}

		return this;
	}


	public DecompilationWriter record(Scope scope, ClassContext context) {
		return record(scope, context, Priority.ZERO);
	}

	public DecompilationWriter record(Operation operation, ClassContext context, Priority priority) {
		return record(operation, context, priority, priority.getAssociativity());
	}

	public DecompilationWriter record(Operation operation, ClassContext context,
	                                  Priority priority, Associativity side) {

		var selfPriority = operation.getPriority();

		if (selfPriority.lessThan(priority) ||
			selfPriority == priority && side != priority.getAssociativity()) {

			operation.write(record('('), context);
			record(')');
		} else {
			operation.write(this, context);
		}

		return this;
	}

	public DecompilationWriter record(Collection<? extends Operation> operations, ClassContext context,
	                                  Priority priority, String separator) {
		if (!operations.isEmpty()) {
			record(operations.iterator().next(), context, priority);
			operations.stream().skip(1).forEach(operation -> record(separator).record(operation, context, priority));
		}

		return this;
	}


	public DecompilationWriter recordsp() {
		return record(' ');
	}

	public DecompilationWriter recordsp(String str) {
		return record(str).record(' ');
	}

	public DecompilationWriter recordsp(Writable writable) {
		return record(writable).record(' ');
	}

	public DecompilationWriter recordsp(ContextualWritable writable, ClassContext context) {
		return record(writable, context).record(' ');
	}

	public DecompilationWriter recordsp(char ch) {
		return record(ch).record(' ');
	}

	public DecompilationWriter ln() {
		return record('\n');
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
