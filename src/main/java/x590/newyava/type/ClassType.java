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

	/** Принимает название класса, начинающееся с {@code 'L'}
	 * @throws DecompilationException если название не начинается с {@code 'L'} */
	public static ClassType valueOfL(String classBinName) {
		if (!classBinName.startsWith("L"))
			throw new DecompilationException("Class bin name must start from 'L'");

		return valueOf(classBinName.substring(1));
	}

	public static ClassType valueOf(String classBinName) {
		if (classBinName.endsWith(";"))
			classBinName = classBinName.substring(0, classBinName.length() - 1);

		return CLASS_POOL.computeIfAbsent(classBinName, ClassType::new);
	}

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
			throw new DecompilationException("Class " + this + " has superclass " +
					this.superClass + " but got another superclass " + superClass);
		}
	}

	private void checkOrUpdateInterfaces(List<ClassType> interfaces) {
		if (this.interfaces == null) {
			this.interfaces = interfaces;

		} else if (!this.interfaces.equals(interfaces)) {
			throw new DecompilationException("Class " + this + " has interface list " +
					this.interfaces + " but got another interface list " + interfaces);
		}
	}


	public static void checkOrUpdateNested(String innerName, String outerName) {
		valueOf(innerName).checkOrUpdateOuter(outerName);
	}

	private void checkOrUpdateOuter(String outerName) {
		if (outer == null) {
			outer = valueOf(outerName);

			var thisBinSN = this.binSimpleName;
			var outerBinSN = outer.binSimpleName;

			if (thisBinSN.length() - outerBinSN.length() <= 1 ||
					!thisBinSN.startsWith(outerBinSN) ||
					!isNestedSeparator(thisBinSN.charAt(outerBinSN.length()))) {

				throw new DecompilationException("Class " + this + " has illegal outer class " + outer);
			}

			this.simpleName = thisBinSN.substring(outerBinSN.length() + 1);
			outer.addInnerClass(this);
			this.updateName();

		} else {
			if (!outer.binName.equals(outerName)) {
				throw new DecompilationException("Class " + this + " gets another outer class " + outerName);
			}
		}
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

	@Override
	public String getVarName() {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, simpleName);
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
