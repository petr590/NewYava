package x590.newyava.visitor;

import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.ContextualWritable;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ObjIntConsumer;

import static x590.newyava.Literals.*;
import static x590.newyava.modifiers.Modifiers.*;

public class ModuleInfo extends ModuleVisitor implements ContextualWritable, Importable {
	private final String name;
	private final int modifiers;

	private final List<RequireEntry> requires = new ArrayList<>();
	private final List<ExportEntry> exports = new ArrayList<>();
	private final List<OpenEntry> opens = new ArrayList<>();
	private final List<ClassType> uses = new ArrayList<>();
	private final List<ProvideEntry> provides = new ArrayList<>();

	protected ModuleInfo(String name, int modifiers) {
		super(Opcodes.ASM9);
		this.name = name;
		this.modifiers = modifiers;
	}

	@Override
	public void visitRequire(String module, int modifiers, String version) {
		requires.add(new RequireEntry(module, modifiers));
	}

	@Override
	public void visitExport(String pack, int modifiers, String... modules) {
		exports.add(new ExportEntry(pack, modules));
	}

	@Override
	public void visitOpen(String pack, int modifiers, String... modules) {
		opens.add(new OpenEntry(pack, modules));
	}

	@Override
	public void visitUse(String service) {
		uses.add(ClassType.valueOf(service));
	}

	@Override
	public void visitProvide(String service, String... providers) {
		provides.add(new ProvideEntry(
				ClassType.valueOf(service),
				Arrays.stream(providers).map(ClassType::valueOf).toList()
		));
	}


	@Override
	public void addImports(ClassContext context) {
		context.addImports(uses)
				.addImportsFor(provides);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if ((modifiers & ACC_OPEN) != 0)
			out.record(LIT_OPEN + " ");

		out.record(LIT_MODULE + " ").record(name).record(" {").incIndent();

		out.record(requires, context).lnIf(!requires.isEmpty());
		out.record(exports, context).lnIf(!exports.isEmpty());
		out.record(opens, context).lnIf(!opens.isEmpty());
		out.<ClassType>record(uses, (use, i) -> writeUse(out, use, context)).lnIf(!uses.isEmpty());
		out.record(provides, context).lnIf(!provides.isEmpty());

		out.decIndent().record('}');
	}

	private static void writeUse(DecompilationWriter out, ClassType use, Context context) {
		out.ln().indent().record(LIT_USES + " ").record(use, context).record(';');
	}

	private record RequireEntry(String module, int modifiers) implements ContextualWritable {
		@Override
		public void write(DecompilationWriter out, Context context) {
			out.ln().indent().record(LIT_REQUIRES + " ");

			if ((modifiers & ACC_TRANSITIVE) != 0) out.record(LIT_TRANSITIVE + " ");
			if ((modifiers & ACC_STATIC_PHASE) != 0) out.record(LIT_STATIC_PHASE + " ");

			out.record(module).record(';');
		}
	}

	private sealed interface ExportOrOpenEntry extends ContextualWritable
			permits ExportEntry, OpenEntry {

		@Override
		default void write(DecompilationWriter out, Context context) {
			out.ln().indent().recordSp(literal()).record(pack());
			writeList(out, "to", Arrays.asList(modules()), (module, i) -> out.record(module));
		}

		String literal();
		String pack();
		String[] modules();
	}

	private record ExportEntry(String pack, String... modules) implements ExportOrOpenEntry {
		@Override
		public String literal() {
			return LIT_EXPORTS;
		}
	}

	private record OpenEntry(String pack, String... modules) implements ExportOrOpenEntry {
		@Override
		public String literal() {
			return LIT_OPENS;
		}
	}

	private record ProvideEntry(ClassType service, @Unmodifiable List<ClassType> implementations)
			implements ContextualWritable, Importable {

		@Override
		public void addImports(ClassContext context) {
			context.addImport(service).addImports(implementations);
		}

		@Override
		public void write(DecompilationWriter out, Context context) {
			out.ln().indent().record(LIT_PROVIDES + " ").record(service, context);
			writeList(out, "with", implementations, (impl, i) -> out.record(impl, context));
		}
	}

	private static <T> void writeList(DecompilationWriter out, String literal, List<T> values, ObjIntConsumer<T> writer) {
		switch (values.size()) {
			case 0 -> {}
			case 1 -> out.space().record(literal).space().record(values, writer);
			default -> out.space().record(literal)
					.incIndent(2).ln().indent()
					.record(values, ",\n" + out.getIndent(), writer)
					.decIndent(2);
		}

		out.record(';');
	}
}
