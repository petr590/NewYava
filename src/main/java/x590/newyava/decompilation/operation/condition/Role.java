package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.WriteContext;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;

import java.util.function.Predicate;

public interface Role {

	default void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {}

	boolean canWrite();

	void write(DecompilationWriter out, WriteContext context);

	default boolean isBreakFor(Scope scope) {
		return false;
	}

	default boolean isContinueFor(Scope scope) {
		return false;
	}

	Role    UNKNOWN = Fixed.UNKNOWN,
			LOOP_CONDITION = Fixed.LOOP_CONDITION,
			IF_BRANCH = Fixed.IF_BRANCH,
			ELSE_BRANCH = Fixed.ELSE_BRANCH;

	@RequiredArgsConstructor
	enum Fixed implements Role {
		UNKNOWN, LOOP_CONDITION, IF_BRANCH, ELSE_BRANCH;

		@Override
		public boolean canWrite() {
			return false;
		}

		@Override
		public void write(DecompilationWriter out, WriteContext context) {
			throw new UnsupportedOperationException();
		}
	}


	static Role breakScope(Scope scope) {
		return new BreakContinue(BreakContinue.Literal.BREAK, scope);
	}

	static Role continueScope(Scope scope) {
		return new BreakContinue(BreakContinue.Literal.CONTINUE, scope);
	}

	@RequiredArgsConstructor
	final class BreakContinue implements Role {
		@RequiredArgsConstructor
		private enum Literal {
			BREAK("break", Scope::isBreakable),
			CONTINUE("continue", Scope::isContinuable);

			private final String value;
			private final Predicate<Scope> isScopeAcceptable;
		}

		private final Literal literal;
		private final Scope scope;
		private @Nullable String labelName;

		@Override
		public boolean isBreakFor(Scope scope) {
			return this.scope == scope && literal == Literal.BREAK;
		}

		@Override
		public boolean isContinueFor(Scope scope) {
			return this.scope == scope && literal == Literal.CONTINUE;
		}

		@Override
		public void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {
			for (Scope c = currentScope; c != null && c != scope; c = c.getParent()) {
				if (literal.isScopeAcceptable.test(c)) {
					labelName = scope.getLabelName(generator);
				}
			}
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public void write(DecompilationWriter out, WriteContext context) {
			out.record(literal.value);

			if (labelName != null) {
				out.recordsp().record(labelName);
			}
		}
	}
}
