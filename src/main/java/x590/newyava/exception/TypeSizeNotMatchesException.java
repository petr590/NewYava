package x590.newyava.exception;

import x590.newyava.type.TypeSize;

public class TypeSizeNotMatchesException extends DecompilationException {

	public TypeSizeNotMatchesException(TypeSize givenSize, TypeSize requiredSize) {
		super("Expected " + requiredSize + ", got " + givenSize);
	}
}
