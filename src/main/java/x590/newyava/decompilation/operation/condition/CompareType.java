package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import x590.newyava.decompilation.operation.Priority;

@Getter
@RequiredArgsConstructor
public enum CompareType {
	EQUALS           ("==", Priority.EQUALS_CMP),
	NOT_EQUALS       ("!=", Priority.EQUALS_CMP),
	LESS             ("<",  Priority.GREATER_LESS_CMP),
	LESS_OR_EQUAL    ("<=", Priority.GREATER_LESS_CMP),
	GREATER          (">",  Priority.GREATER_LESS_CMP),
	GREATER_OR_EQUAL (">=", Priority.GREATER_LESS_CMP);

	private final String operator;
	private final Priority priority;

	private @Nullable CompareType opposite;


	static {
		setOpposites(EQUALS, NOT_EQUALS);
		setOpposites(LESS, GREATER_OR_EQUAL);
		setOpposites(GREATER, LESS_OR_EQUAL);
	}

	private static void setOpposites(CompareType type1, CompareType type2) {
		type1.opposite = type2;
		type2.opposite = type1;
	}
}
