package x590.newyava.decompilation.operation.condition;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.Context;
import x590.newyava.decompilation.scope.LabelNameGenerator;
import x590.newyava.decompilation.scope.Scope;
import x590.newyava.io.DecompilationWriter;

import java.util.function.Predicate;

/**
 * Роль чанка. Только для чанков, у которых есть переход на другие чанки
 */
public interface Role {

	default void resolveLabelNames(Scope currentScope, LabelNameGenerator generator) {}

	boolean canWrite();

	void write(DecompilationWriter out, Context context);

	default boolean isBreakOf(Scope scope) {
		return false;
	}

	default boolean isContinueOf(Scope scope) {
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
		public void write(DecompilationWriter out, Context context) {
			throw new UnsupportedOperationException();
		}
	}


	static Role breakScope(Scope scope) {
		return new BreakContinue(BreakContinue.BREAK, Scope::isBreakable, scope);
	}

	static Role continueScope(Scope scope) {
		return new BreakContinue(BreakContinue.CONTINUE, Scope::isContinuable, scope);
	}

	@RequiredArgsConstructor
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
			for (Scope c = currentScope; c != null && c != scope; c = c.getParent()) {
				if (isScopeAcceptable.test(c)) {
					labelName = scope.getLabelName(generator);
				}
			}
		}

		@Override
		public boolean canWrite() {
			return true;
		}

		@Override
		public void write(DecompilationWriter out, Context context) {
			out.record(literal);

			if (labelName != null) {
				out.space().record(labelName);
			}
		}
	}
}
