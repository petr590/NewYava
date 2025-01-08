package x590.newyava.type;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.InvalidTypeException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;
import x590.newyava.util.Utils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.*;

/**
 * Это <b>класс</b>ика Java!
 */
@Getter
public final class ClassType implements IClassType {

	private static final Map<String, ClassType> CLASS_POOL = new HashMap<>();
	private static final Map<String, String> OUTER_CLASS_NAMES_POOL = new HashMap<>();

	public static final ClassType
			OBJECT = valueOf(Object.class),
			CLASS  = valueOf(Class.class),
			STRING = valueOf(String.class),
			ENUM   = valueOf(Enum.class),
			RECORD = valueOf(Record.class),
			COMPARABLE   = valueOf(Comparable.class),
			SERIALIZABLE = valueOf(Serializable.class),
			THROWABLE    = valueOf(Throwable.class),
			ANNOTATION   = valueOf(Annotation.class),
			REPEATABLE   = valueOf(Repeatable.class),
			OVERRIDE     = valueOf(Override.class),
			ASSERTION_ERROR     = valueOf(AssertionError.class),
			NO_SUCH_FIELD_ERROR = valueOf(NoSuchFieldError.class),

			BYTE      = valueOf(Byte.class),
			SHORT     = valueOf(Short.class),
			CHARACTER = valueOf(Character.class),
			INTEGER   = valueOf(Integer.class),
			LONG      = valueOf(Long.class),
			FLOAT     = valueOf(Float.class),
			DOUBLE    = valueOf(Double.class),
			BOOLEAN   = valueOf(Boolean.class),
			VOID      = valueOf(Void.class),
			MATH      = valueOf(Math.class);


	/** Полное бинарное имя класса, например {@code "java/lang/Object"} или {@code "java/util/Map$Entry"} */
	private final String classBinName;

	/** Бинарное имя класса, например {@code "Object"} или {@code "Map$Entry"} */
	private final String simpleBinName;

	/** Полное имя класса, например {@code "java.lang.Object"} или {@code "java.util.Map.Entry"} */
	private String name;

	/** Имя класса, например {@code "Object"}. Для вложенных классов включает
	 * только имя самого класса без имени внешнего класса. */
	private String simpleName;

	/** Имя пакета класса, например {@code "java.lang"} */
	private final String packageName;

	private boolean isEnclosedInMethod, isAnonymous;

	private @Nullable ClassType outer;
	private @Nullable Set<ClassType> innerClasses;

	private @Nullable ClassType superClass;
	private @Nullable @Unmodifiable List<ClassType> interfaces;

	private ClassType(String classBinName) {
		if (!isValidName(classBinName)) {
			throw new IllegalArgumentException(classBinName);
		}

		this.classBinName = classBinName;

		String name = classBinName.replace('/', '.');
		int index = name.lastIndexOf('.');

		this.name = name;
		this.simpleBinName = index < 0 ? name : name.substring(index + 1);
		this.simpleName = simpleBinName;
		this.packageName = index < 0 ? "" : name.substring(0, index);
	}

	private static boolean isValidName(String binName) {
		if (binName.equals("module-info")) {
			return true;
		}

		if (binName.endsWith("/package-info")) {
			binName = binName.substring(0, binName.length() - "/package-info".length());
		}


		int len = binName.length();

		if (len == 0 ||
			!Character.isJavaIdentifierStart(binName.charAt(0)) ||
			!Character.isJavaIdentifierPart(binName.charAt(len - 1))) {
			return false;
		}

		for (int i = 1; i < len; i++) {
			char c = binName.charAt(i);

			if (c == '/') {
				if (binName.charAt(i - 1) == '/')
					return false;

			} else if (!Character.isJavaIdentifierPart(c)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Принимает бинарное название класса, начинающееся с {@code 'L'}
	 * (такое, как {@code "Ljava/lang/Object;"}).
	 * @apiNote Точка с запятой в конце необязательна.
	 * @return класс, соответствующий этому названию.
	 * @throws DecompilationException если название не начинается с {@code 'L'}
	 */
	public static ClassType valueOfL(String classBinName) {
		if (!classBinName.startsWith("L"))
			throw new DecompilationException("Class bin name must start from 'L'");

		return valueOf(classBinName.substring(1));
	}

	/**
	 * Принимает бинарное название класса, <b>не</b> начинающееся с {@code 'L'}
	 * (такое, как {@code "java/lang/Object;"}).
	 * @apiNote Точка с запятой в конце необязательна.
	 * @return класс, соответствующий этому названию.
	 */
	public static ClassType valueOf(String classBinName) {
		if (classBinName.endsWith(";"))
			classBinName = classBinName.substring(0, classBinName.length() - 1);

		return CLASS_POOL.computeIfAbsent(classBinName, ClassType::new);
	}

	/**
	 * Принимает объект {@link Class}.
	 * @return {@link ClassType}, соответствующий переданному классу.
	 * @throws IllegalArgumentException если класс представляет примитив или массив.
	 */
	public static ClassType valueOf(Class<?> clazz) {
		if (clazz.isPrimitive() || clazz.isArray()) {
			throw new IllegalArgumentException("Class " + clazz + " not represents pure class type");
		}

		var classType = valueOf(clazz.getName().replace('.', '/'));

		classType.checkOrUpdateSuper(valueOfNullable(clazz.getSuperclass()));
		classType.checkOrUpdateInterfaces(
				Arrays.stream(clazz.getInterfaces()).map(ClassType::valueOf).toList()
		);

		return classType;
	}

	/** @return {@code null}, если переданный класс равен {@code null}.
	 * Иначе - результат вызова {@link #valueOf(Class)}. */
	private static @Nullable ClassType valueOfNullable(@Nullable Class<?> clazz) {
		return clazz == null ? null : valueOf(clazz);
	}

	/** @deprecated используйте {@link IClassType#parse(SignatureReader)} */
	@Deprecated(since = "0.9.27", forRemoval = true)
	public static ClassType parse(SignatureReader reader) {
		var binName = new StringBuilder();

		for (;;) {
			char ch = reader.next();

			if (Character.isJavaIdentifierPart(ch) || ch == '/') {
				binName.append(ch);
				continue;
			}

			if (ch == ';') {
				break;
			}

			throw new InvalidTypeException(binName + reader.dec().nextAll());
		}

		return valueOf(binName.toString());
	}

	@Override
	public ClassType base() {
		return this;
	}

	@Override
	public String getBinName() {
		return "L" + classBinName + ";";
	}

	public boolean isPackageInfo() {
		return simpleName.equals("package-info");
	}

	@Override
	public boolean isNested() {
		return outer != null;
	}


	/** @return {@code true}, если этот класс объявлен внутри переданного класса
	 * на любом уровне вложенности, т.е. если есть иерархия:
	 * <pre>
	 * {@code class X {
	 *      class Y {
	 *          class Z {}
	 *      }
	 * }}
	 * </pre>get
	 * то {@code Z.isInside(X)} вернёт {@code true}. */
	public boolean isInside(ClassType other) {
		return outer != null && (outer.equals(other) || outer.isInside(other));
	}

	/** @return класс верхнего уровня, который содержит данный класс. */
	public ClassType getTopLevelClass() {
		return outer == null ? this : outer.getTopLevelClass();
	}


	@Override
	public @Nullable ReferenceType getSuperType() {
		return superClass;
	}

	@Override
	public @Unmodifiable List<? extends ReferenceType> getInterfaces() {
		return interfaces == null ? Collections.emptyList() : interfaces;
	}


	private void checkOrUpdateSuper(@Nullable ClassType superClass) {
		if (superClass == null) {
			return;
		}

		if (this.superClass == null) {
			this.superClass = superClass;

		} else if (!this.superClass.equals(superClass)) {
			throw new DecompilationException(
					"Class %s has superclass %s but got another superclass %s",
					this, this.superClass, superClass
			);
		}
	}

	private void checkOrUpdateInterfaces(@Unmodifiable List<ClassType> interfaces) {
		if (this.interfaces == null) {
			this.interfaces = interfaces;

		} else if (!this.interfaces.equals(interfaces)) {
			throw new DecompilationException(
					"Class %s has interface list %s but got another interface list %s",
					this, this.interfaces, interfaces
			);
		}
	}


	/** Вызывает {@link #checkOrUpdateNested(String, String, boolean)}
	 * с параметром {@code isEnclosedInMethod = true} */
	public static void checkOrUpdateNested(String innerName, String outerName) {
		checkOrUpdateNested(innerName, outerName, false);
	}

	/**
	 * Если внешний класс не инициализирован, то инициализирует его.
	 * Иначе проверяет, что внешний класс совпадает.
	 * @param innerName бинарное имя вложенного класса.
	 * @param outerName бинарное имя внешнего класса.
	 * @param isEnclosedInMethod является ли класс анонимным.
	 * @throws DecompilationException при несовпадении внешнего класса с переданным.
	 */
	public static void checkOrUpdateNested(String innerName, String outerName, boolean isEnclosedInMethod) {
		valueOf(innerName).checkOrUpdateOuter(outerName, isEnclosedInMethod);
	}

	private void checkOrUpdateOuter(String outerName, boolean isEnclosedInMethod) {
		if (outer != null) {
			if (outer.classBinName.equals(outerName)) {
				return;
			}

			throw new DecompilationException("Class %s gets another outer class %s", this, outerName);
		}

		outer = valueOf(outerName);

		var thisBinSN = this.simpleBinName;
		var outerBinSN = outer.simpleBinName;

		if (thisBinSN.length() >= outerBinSN.length() + 1 &&
			thisBinSN.startsWith(outerBinSN) &&
			isNestedSeparator(thisBinSN.charAt(outerBinSN.length()))) {

			this.simpleName = getInnerSimpleName(outerBinSN);
			this.isEnclosedInMethod = isEnclosedInMethod;
			this.isAnonymous = isEnclosedInMethod && StringUtils.isNumeric(simpleName);

			outer.addInnerClass(this);
			updateName();

		} else {
			throw new DecompilationException("Class %s has illegal outer class %s", this, outer);
		}
	}

	private String getInnerSimpleName(String outerSimpleBinName) {
		var simpleBinName = this.simpleBinName;

		int i = outerSimpleBinName.length() + 1;
		int len = simpleBinName.length();

		for (; i < len; i++) {
			if (!Character.isDigit(simpleBinName.charAt(i)))
				break;
		}

		// Если имя состоит только из цифр, то возвращаем его
		return simpleBinName.substring(i < len ? i : outerSimpleBinName.length() + 1);
	}


	private static boolean isNestedSeparator(char c) {
		return c == '.' || c == '$';
	}

	private void addInnerClass(ClassType classType) {
		if (innerClasses == null)
			innerClasses = new HashSet<>();

		innerClasses.add(classType);
	}

	private void updateName() {
		assert outer != null;
		name = outer.getName() + '.' + simpleName;

		if (innerClasses != null)
			innerClasses.forEach(ClassType::updateName);
	}


	@Getter(lazy = true)
	private final String varName = computeVarName();

	private String computeVarName() {
		if (isAnonymous) {
			var interfaces = getInterfaces();
			var superClass = getSuperClass();

			return (interfaces.size() == 1 ? interfaces.get(0) :
					superClass != null ? superClass : OBJECT).getVarName();
		}

		assert simpleName != null;
		return Utils.safeToLowerCamelCase(simpleName);
	}

	// Метод equals оставлен без изменений, так как все объекты кешируются,
	// и для проверки равенства надо просто сравнить ссылки.
	// Переопределённый hashCode даёт лучшую производительность.
	@Override
	public int hashCode() {
		return classBinName.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImport(this);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		if (context.imported(this)) {
			out.record(isAnonymous ? "var" : simpleName);
			return;
		}

		if (outer != null) {
			out.record(outer, context).record('.').record(simpleName);
			return;
		}

		out.record(name);
	}
}
