package x590.newyava.modifiers;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import org.jetbrains.annotations.Unmodifiable;

import static x590.newyava.Literals.*;
import static x590.newyava.modifiers.Modifiers.*;

/**
 * Тип сущности, у которой есть модификаторы доступа.
 */
public enum EntryType {
	CLASS(new Int2ObjectArrayMap<>(
			new int[]    { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SUPER, ACC_INTERFACE, ACC_ABSTRACT, ACC_SYNTHETIC, ACC_ANNOTATION, ACC_ENUM, ACC_MODULE, ACC_RECORD, ACC_DEPRECATED, },
			new String[] { LIT_PUBLIC, LIT_PRIVATE, LIT_PROTECTED, LIT_STATIC, LIT_FINAL, LIT_SUPER, LIT_INTERFACE, LIT_ABSTRACT, LIT_SYNTHETIC, LIT_ANNOTATION, LIT_ENUM, LIT_MODULE, LIT_RECORD, LIT_DEPRECATED, }
	)),
	
	FIELD(new Int2ObjectArrayMap<>(
			new int[]    { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_VOLATILE, ACC_TRANSIENT, ACC_SYNTHETIC, ACC_DEPRECATED, },
			new String[] { LIT_PUBLIC, LIT_PRIVATE, LIT_PROTECTED, LIT_STATIC, LIT_FINAL, LIT_VOLATILE, LIT_TRANSIENT, LIT_SYNTHETIC, LIT_DEPRECATED, }
	)),
	
	METHOD(new Int2ObjectArrayMap<>(
			new int[]    { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, ACC_BRIDGE, ACC_VARARGS, ACC_NATIVE, ACC_ABSTRACT, ACC_STRICT, ACC_SYNTHETIC, ACC_DEPRECATED, },
			new String[] { LIT_PUBLIC, LIT_PRIVATE, LIT_PROTECTED, LIT_STATIC, LIT_FINAL, LIT_SYNCHRONIZED, LIT_BRIDGE, LIT_VARARGS, LIT_NATIVE, LIT_ABSTRACT, LIT_STRICT, LIT_SYNTHETIC, LIT_DEPRECATED, }
	));

	private final @Unmodifiable Int2ObjectMap<String> allowedModifiers;

	EntryType(Int2ObjectMap<String> allowedModifiers) {
		this.allowedModifiers = Int2ObjectMaps.unmodifiable(allowedModifiers);
	}

	/** @return строку, содержащую все флаги, установленные в {@code modifiers}.
	 * Не обрабатывает сочетания флагов.
	 * <br><br>
	 * Например, для интерфейса, который задаётся флагами {@code ACC_ABSTRACT} и {@code ACC_INTERFACE},
	 * метод вернёт {@code "abstract interface"}. */
	public String modifiersToString(int modifiers) {
		var str = new StringBuilder();

		boolean first = true;

		for (var entry : allowedModifiers.int2ObjectEntrySet()) {
			if ((modifiers & entry.getIntKey()) != 0) {
				if (!first) str.append(' ');
				first = false;

				str.append(entry.getValue());
			}
		}

		return str.toString();
	}
}
