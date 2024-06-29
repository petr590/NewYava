package x590.newyava.modifiers;

import org.objectweb.asm.Opcodes;

/**
 * Список модификаторов
 */
public class Modifiers {

	private Modifiers() {}

	public static final int
			ACC_VISIBLE      = 0,
			ACC_PUBLIC       = Opcodes.ACC_PUBLIC,
			ACC_PRIVATE      = Opcodes.ACC_PRIVATE,
			ACC_PROTECTED    = Opcodes.ACC_PROTECTED,
			ACC_STATIC       = Opcodes.ACC_STATIC,
			ACC_FINAL        = Opcodes.ACC_FINAL,
			ACC_SUPER        = Opcodes.ACC_SUPER,
			ACC_SYNCHRONIZED = Opcodes.ACC_SYNCHRONIZED,
			ACC_OPEN         = Opcodes.ACC_OPEN,
			ACC_TRANSITIVE   = Opcodes.ACC_TRANSITIVE,
			ACC_VOLATILE     = Opcodes.ACC_VOLATILE,
			ACC_BRIDGE       = Opcodes.ACC_BRIDGE,
			ACC_STATIC_PHASE = Opcodes.ACC_STATIC_PHASE,
			ACC_VARARGS      = Opcodes.ACC_VARARGS,
			ACC_TRANSIENT    = Opcodes.ACC_TRANSIENT,
			ACC_NATIVE       = Opcodes.ACC_NATIVE,
			ACC_INTERFACE    = Opcodes.ACC_INTERFACE,
			ACC_ABSTRACT     = Opcodes.ACC_ABSTRACT,
			ACC_STRICT       = Opcodes.ACC_STRICT,
			ACC_SYNTHETIC    = Opcodes.ACC_SYNTHETIC,
			ACC_ANNOTATION   = Opcodes.ACC_ANNOTATION,
			ACC_ENUM         = Opcodes.ACC_ENUM,
			ACC_MANDATED     = Opcodes.ACC_MANDATED,
			ACC_MODULE       = Opcodes.ACC_MODULE,
			ACC_RECORD       = Opcodes.ACC_RECORD,
			ACC_DEPRECATED   = Opcodes.ACC_DEPRECATED,

			ACC_ACCESS       = ACC_VISIBLE | ACC_PUBLIC | ACC_PRIVATE | ACC_PROTECTED,
			ACC_PUBLIC_STATIC_FINAL = ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
			ACC_FIELD        = ACC_ACCESS | ACC_STATIC | ACC_FINAL | ACC_VOLATILE | ACC_TRANSIENT,
			ACC_NONE         = 0;
}
