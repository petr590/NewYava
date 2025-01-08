package x590.newyava.decompilation.operation.condition;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.Importable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.GenericWritable;

import java.util.function.Predicate;

/**
 * Роль чанка. Только для чанков, у которых есть переход на другие чанки.
 */
public interface Role extends GenericWritable<MethodWriteContext>, Importable {
	Role    UNKNOWN = Fixed.UNKNOWN,
			LOOP_CONDITION = Fixed.LOOP_CONDITION,
			IF_BRANCH = Fixed.IF_BRANCH,
			ELSE_BRANCH = Fixed.ELSE_BRANCH;


	static Role breakScope(Scope scope) {
		return new BreakContinue(BreakContinue.BREAK, Scope::isBreakable, scope);
	}

	static Role continueScope(Scope scope) {
		return new BreakContinue(BreakContinue.CONTINUE, Scope::isContinuable, scope);
	}

	static Role yieldScope(Scope scope, Operation value) {
		return new Yield(scope, value);
	}


	default void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {}

	boolean canWrite();

	@Override
	default void addImports(ClassContext context) {}

	default boolean isBreakOf(Scope scope) {
		return false;
	}

	default boolean isContinueOf(Scope scope) {
		return false;
	}

	default boolean isYieldOf(Scope scope) {
		return false;
	}

	default Operation getYieldValue() {
		throw new UnsupportedOperationException();
	}


	@RequiredArgsConstructor
	enum Fixed implements Role {
		UNKNOWN, LOOP_CONDITION, IF_BRANCH, ELSE_BRANCH;

		@Override
		public boolean canWrite() {
			return false;
		}

		@Override
		public void write(DecompilationWriter out, MethodWriteContext context) {
			throw new UnsupportedOperationException();
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	final class BreakContinue implements Role {
		private static final String
				BREAK = "break",
				CONTINUE = "continue";

		private final String literal;
		private final Predicate<Scope> isScopeAcceptable;
		private final Scope scope;
		private @Nullable String labelName;

		@Override
		public boolean isBreakOf(Scope scope) {
			return this.scope == scope && literal.equals(BREAK);
		}

		@Override
		public boolean isContinueOf(Scope scope) {
			return this.scope == scope && literal.equals(CONTINUE);
		}

		@Override
		public void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {
			for (Scope sc = currentScope; sc != null && sc != scope; sc = sc.getParent()) {
				if (isScopeAcceptable.test(sc)) {
					labelName = scope.getLabelName(generator);
					break;
				}
			}
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public void write(DecompilationWriter out, MethodWriteContext context) {
			out.record(literal);

			if (labelName != null) {
				out.space().record(labelName);
			}
		}

		@Override
		public String toString() {
			return String.format("%s(%s)", literal, scope);
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	final class Yield implements Role {
		private final Scope scope;
		private final Operation value;

		@Override
		public boolean isYieldOf(Scope scope) {
			return this.scope == scope;
		}

		@Override
		public Operation getYieldValue() {
			return value;
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public void addImports(ClassContext context) {
			context.addImportsFor(value);
		}

		@Override
		public void write(DecompilationWriter out, MethodWriteContext context) {
			out.record("yield ").record(value, context, Priority.ZERO);
		}
	}
}
