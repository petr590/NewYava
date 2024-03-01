package x590.newyava;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.Unmodifiable;

import static x590.newyava.Modifiers.*;
import static x590.newyava.Literals.*;

@Getter
public enum EntryType {
	CLASS(new Int2ObjectArrayMap<>(
			new int[]    { ACC_PUBLIC, ACC_FINAL, ACC_SUPER, ACC_INTERFACE, ACC_ABSTRACT, ACC_SYNTHETIC, ACC_ANNOTATION, ACC_ENUM, ACC_MODULE, ACC_RECORD, },
			new String[] { LIT_PUBLIC, LIT_FINAL, LIT_SUPER, LIT_INTERFACE, LIT_ABSTRACT, LIT_SYNTHETIC, LIT_ANNOTATION, LIT_ENUM, LIT_MODULE, LIT_RECORD, }
	)),
	
	FIELD(new Int2ObjectArrayMap<>(
			new int[]    { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_VOLATILE, ACC_TRANSIENT, ACC_SYNTHETIC, },
			new String[] { LIT_PUBLIC, LIT_PRIVATE, LIT_PROTECTED, LIT_STATIC, LIT_FINAL, LIT_VOLATILE, LIT_TRANSIENT, LIT_SYNTHETIC, }
	)),
	
	METHOD(new Int2ObjectArrayMap<>(
			new int[]    { ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED, ACC_BRIDGE, ACC_VARARGS, ACC_NATIVE, ACC_ABSTRACT, ACC_STRICT, ACC_SYNTHETIC, },
			new String[] { LIT_PUBLIC, LIT_PRIVATE, LIT_PROTECTED, LIT_STATIC, LIT_FINAL, LIT_SYNCHRONIZED, LIT_BRIDGE, LIT_VARARGS, LIT_NATIVE, LIT_ABSTRACT, LIT_STRICT, LIT_SYNTHETIC, }
	)),
	
	MODULE(new Int2ObjectArrayMap<>(
			new int[]    { ACC_OPEN, ACC_SYNTHETIC, ACC_MANDATED, },
			new String[] { LIT_OPEN, LIT_SYNTHETIC, LIT_MANDATED, }
	)),
	
	MODULE_REQUIRES(new Int2ObjectArrayMap<>(
			new int[]    { ACC_TRANSITIVE, ACC_STATIC_PHASE, ACC_SYNTHETIC, ACC_MANDATED, },
			new String[] { LIT_TRANSITIVE, LIT_STATIC_PHASE, LIT_SYNTHETIC, LIT_MANDATED, }
	)),
	
	MODULE_EXPORTS_OR_OPENS(new Int2ObjectArrayMap<>(
			new int[]    { ACC_SYNTHETIC, ACC_MANDATED, },
			new String[] { LIT_SYNTHETIC, LIT_MANDATED, }
	));

	private static final int HIDDEN_MODIFIERS = ACC_SYNTHETIC | ACC_BRIDGE | ACC_SUPER | ACC_VARARGS;

	private final @Unmodifiable Int2ObjectMap<String> allowedModifiers;

	EntryType(Int2ObjectMap<String> allowedModifiers) {
		this.allowedModifiers = Int2ObjectMaps.unmodifiable(allowedModifiers);
	}

	public String modifiersToString(int modifiers) {
		var str = new StringBuilder();

		for (var entry : allowedModifiers.int2ObjectEntrySet()) {
			if ((modifiers & entry.getIntKey()) != 0) {
				str.append(entry.getValue()).append(' ');
			}
		}

		return str.toString();
	}
}
