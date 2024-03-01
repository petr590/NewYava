package x590.newyava.exception;

public class UnknownOpcodeException extends DisassemblingException {

	public UnknownOpcodeException(int opcode) {
		super("0x" + Integer.toHexString(opcode));
	}
}
