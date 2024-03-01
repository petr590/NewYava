package x590.newyava.type;

/**
 * Константы вынесены в отдельный класс для того,
 * чтобы избежать циклической инициализации классов
 */
public final class Types {
	public static final Type ANY_TYPE = AnyType.INSTANCE;
	public static final Type ANY_ARRAY_TYPE = ArrayType.forType(ANY_TYPE);
	public static final Type ANY_OBJECT_TYPE = AnyObjectType.INSTANCE;

	private Types() {}


}
