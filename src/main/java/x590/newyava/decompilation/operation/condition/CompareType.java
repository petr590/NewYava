package x590.newyava.decompilation.operation.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

	private CompareType opposite;


	static {
		setOpposite(EQUALS, NOT_EQUALS);
		setOpposite(LESS, GREATER_OR_EQUAL);
		setOpposite(GREATER, LESS_OR_EQUAL);
	}

	private static void setOpposite(CompareType type1, CompareType type2) {
		type1.opposite = type2;
		type2.opposite = type1;
	}
}
