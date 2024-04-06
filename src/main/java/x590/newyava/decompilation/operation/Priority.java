package x590.newyava.decompilation.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Priority { // В порядке увеличения приоритета
	ZERO,
	LAMBDA(Associativity.RIGHT),
	ASSIGNMENT(Associativity.RIGHT),
	TERNARY(Associativity.RIGHT),
	LOGICAL_OR,
	LOGICAL_AND,
	BIT_OR,
	BIT_XOR,
	BIT_AND,
	EQUALS_CMP,
	GREATER_LESS_CMP,
	SHIFT,
	ADD_SUB,
	MUL_DIV_REM,
	CAST(Associativity.RIGHT),
	PRE_INC_DEC(Associativity.RIGHT),
	POST_INC_DEC,
	DEFAULT;

	public static final Priority
			INSTANCEOF = GREATER_LESS_CMP,
			NEW = CAST,
			UNARY = PRE_INC_DEC;

	@Getter
	private final Associativity associativity;

	Priority() {
		this(Associativity.LEFT);
	}

	public boolean lessThan(Priority other) {
		return this.compareTo(other) < 0;
	}
}
