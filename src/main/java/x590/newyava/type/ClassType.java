package x590.newyava.type;

import com.google.common.base.CaseFormat;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.exception.DecompilationException;
import x590.newyava.exception.InvalidTypeException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.SignatureReader;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Это <b>класс</b>ика Java!
 */
@Getter
public class ClassType implements ReferenceType {
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

			BYTE      = valueOf(Byte.class),
			SHORT     = valueOf(Short.class),
			CHARACTER = valueOf(Character.class),
			INTEGER   = valueOf(Integer.class),
			LONG      = valueOf(Long.class),
			FLOAT     = valueOf(Float.class),
			DOUBLE    = valueOf(Double.class),
			BOOLEAN   = valueOf(Boolean.class),
			VOID      = valueOf(Void.class);


	private final String binName, binSimpleName;
	private String name, simpleName;
	private final String packageName;

	private boolean isEnclosedInMethod;

	private @Nullable ClassType outer;
	private @Nullable Set<ClassType> innerClasses;

	private @Nullable ClassType superClass;
	private @Nullable List<ClassType> interfaces;

	private ClassType(String binName) {
		this.binName = binName;

		String name = binName.replace('/', '.');
		int index = name.lastIndexOf('.');

		this.name = name;
		this.binSimpleName = index < 0 ? name : name.substring(index + 1);
		this.simpleName = binSimpleName;
		this.packageName = index < 0 ? "" : name.substring(0, index);
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
	 * Принимает бинарное название класса, не начинающееся с {@code 'L'}
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

	public static ClassType parse(SignatureReader reader) {
		var binName = new StringBuilder();

		for (;;) {
			char ch = reader.next();

			if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '$' || ch == '/') {
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


	public boolean isAnonymous() {
		return isEnclosedInMethod && simpleName.isEmpty();
	}


	@Override
	public @Nullable ReferenceType getSuperType() {
		return superClass;
	}

	@Override
	public List<? extends ReferenceType> getInterfaces() {
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

	private void checkOrUpdateInterfaces(List<ClassType> interfaces) {
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
	 * с параметром {@code isAnonymous = true} */
	public static void checkOrUpdateNested(String innerName, String outerName) {
		checkOrUpdateNested(innerName, outerName, false);
	}

	/**
	 * Если внешний класс не инициализирован, то инициализирует его.
	 * Иначе проверяет, что внешний класс совпадает.
	 * @param innerName бинарное имя вложенного класса.
	 * @param outerName бинарное имя внешнего класса.
	 * @param isAnonymous является ли класс анонимным.
	 * @throws DecompilationException при несовпадении внешнего класса с переданным.
	 */
	public static void checkOrUpdateNested(String innerName, String outerName, boolean isAnonymous) {
		valueOf(innerName).checkOrUpdateOuter(outerName, isAnonymous);
	}

	private void checkOrUpdateOuter(String outerName, boolean isAnonymous) {
		if (outer != null) {
			if (outer.binName.equals(outerName)) {
				return;
			}

			throw new DecompilationException("Class %s gets another outer class %s", this, outerName);
		}

		outer = valueOf(outerName);

		var thisBinSN = this.binSimpleName;
		var outerBinSN = outer.binSimpleName;

		if (thisBinSN.length() >= outerBinSN.length() + 1 &&
			thisBinSN.startsWith(outerBinSN) &&
			isNestedSeparator(thisBinSN.charAt(outerBinSN.length()))) {

			this.simpleName = getInnerSimpleName(outerBinSN);
			this.isEnclosedInMethod = isAnonymous;

			outer.addInnerClass(this);
			updateName();

		} else {
			throw new DecompilationException("Class %s has illegal outer class %s", this, outer);
		}
	}

	private String getInnerSimpleName(String outerBinSimpleName) {
		String simpleName = binSimpleName.substring(outerBinSimpleName.length() + 1);

		int i = 0;
		for (int len = simpleName.length(); i < len; i++) {
			if (!Character.isDigit(simpleName.charAt(i)))
				break;
		}

		return simpleName.substring(i);
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
	private final @Nullable String varName = computeVarName();

	private String computeVarName() {
		assert simpleName != null;
		String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, simpleName);

		return switch (name) {
			case "boolean"    -> PrimitiveType.BOOLEAN.getVarName();
			case "byte"       -> PrimitiveType.BYTE.getVarName();
			case "short"      -> PrimitiveType.SHORT.getVarName();
			case "char"       -> PrimitiveType.CHAR.getVarName();
			case "int"        -> PrimitiveType.INT.getVarName();
			case "long"       -> PrimitiveType.LONG.getVarName();
			case "float"      -> PrimitiveType.FLOAT.getVarName();
			case "double"     -> PrimitiveType.DOUBLE.getVarName();
			case "void"       -> PrimitiveType.VOID.getVarName();

			case "abstract"   -> "abs";
			case "assert"     -> "assrt";
			case "break"      -> "brk";
			case "case"       -> "cs";
			case "catch"      -> "ctch";
			case "class"      -> "clazz";
			case "const"      -> "cns";
			case "continue"   -> "cont";
			case "default"    -> "def";
			case "do"         -> "d";
			case "else"       -> "els";
			case "enum"       -> "en";
			case "extends"    -> "ext";
			case "false"      -> "fls";
			case "final"      -> "fin";
			case "finally"    -> "finl";
			case "for"        -> "fr";
			case "goto"       -> "gt";
			case "if"         -> "f";
			case "implements" -> "impl";
			case "import"     -> "imp";
			case "instanceof" -> "inst";
			case "interface"  -> "interf";
			case "native"     -> "nat";
			case "new"        -> "mew"; // ^•ﻌ•^
			case "null"       -> "nll";
			case "package"    -> "pack";
			case "private"    -> "priv";
			case "protected"  -> "prot";
			case "public"     -> "pub";
			case "return"     -> "ret";
			case "static"     -> "stat";
			case "strictfp"   -> "strict";
			case "super"      -> "sup";
			case "switch"     -> "swt";
			case "this"       -> "ths";
			case "throw"      -> "thr";
			case "throws"     -> "thrs";
			case "transient"  -> "trans";
			case "true"       -> "tr";
			case "try"        -> "tr";
			case "volatile"   -> "vol";
			case "while"      -> "whl";
			case "_"          -> "__"; // Шта?
			default           -> name;
		};
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
		out.record(context.imported(this) ? simpleName : name);
	}
}
