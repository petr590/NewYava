package x590.newyava.type;

import lombok.RequiredArgsConstructor;

/**
 * Размер типа на стеке:
 * <p>{@link TypeSize#VOID} - 0 байт</p>
 * <p>{@link TypeSize#WORD} - 4 байта</p>
 * <p>{@link TypeSize#LONG} - 8 байт</p>
 */
@RequiredArgsConstructor
public enum TypeSize {
	VOID(0),
	WORD(1),
	LONG(2);

	private final int slots;

	public int slots() {
		return slots;
	}
}
