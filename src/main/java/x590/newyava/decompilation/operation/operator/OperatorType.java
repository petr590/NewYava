package x590.newyava.decompilation.operation.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import x590.newyava.decompilation.operation.Priority;

@Getter
@RequiredArgsConstructor
public enum OperatorType {
	ADD("+", true, Priority.ADD_SUB),
	SUB("-", true, Priority.ADD_SUB),
	MUL("*", true, Priority.MUL_DIV_REM),
	DIV("/", true, Priority.MUL_DIV_REM),
	REM("%", true, Priority.MUL_DIV_REM),
	SHL("<<", true, Priority.SHIFT),
	SHR(">>", true, Priority.SHIFT),
	USHR(">>>", true, Priority.SHIFT),
	AND("&", true, Priority.BIT_AND),
	XOR("^", true, Priority.BIT_XOR),
	OR("|", true, Priority.BIT_OR),
	NOT("~", false, Priority.UNARY),
	PRE_INC("++", false, Priority.PRE_INC_DEC, false),
	PRE_DEC("--", false, Priority.PRE_INC_DEC, false),
	POST_INC("++", false, Priority.PRE_INC_DEC, true),
	POST_DEC("--", false, Priority.PRE_INC_DEC, true);

	private final String value;
	private final boolean binary;
	private final Priority priority;
	private final boolean post;

	OperatorType(String value, boolean binary, Priority priority) {
		this(value, binary, priority, false);
	}

	public boolean isUnary() {
		return !binary;
	}

	public boolean isIncOrDec() {
		return switch (this) {
			case PRE_INC, PRE_DEC, POST_INC, POST_DEC -> true;
			default -> false;
		};
	}

	public boolean isShift() {
		return switch (this) {
			case SHL, SHR, USHR -> true;
			default -> false;
		};
	}

	public boolean isHexBitwise() {
		return switch (this) {
			case AND, XOR, OR -> true;
			default -> false;
		};
	}

	public OperatorType toPreIncOrDec() {
		return switch (this) {
			case ADD -> PRE_INC;
			case SUB -> PRE_DEC;
			default -> throw new IllegalStateException("Cannot convert " + this + " to pre inc or dec");
		};
	}

	public OperatorType toPostIncOrDec() {
		return switch (this) {
			case ADD -> POST_INC;
			case SUB -> POST_DEC;
			default -> throw new IllegalStateException("Cannot convert " + this + " to post inc or dec");
		};
	}
}
