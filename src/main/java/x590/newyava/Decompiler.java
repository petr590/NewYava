package x590.newyava;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import x590.newyava.io.ConsoleWriterFactory;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.io.WriterFactory;
import x590.newyava.type.ClassArrayType;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Главный класс, выполняющий декомпиляцию.
 * Хранит {@link Config} и карту всех {@link DecompilingClass}.
 */
@RequiredArgsConstructor
public class Decompiler {

	@Getter
	private final Config config;

	private final WriterFactory writerFactory;

	public Decompiler(Config config) {
		this(config, ConsoleWriterFactory.INSTANCE);
	}

	private @Nullable @Unmodifiable Map<ClassArrayType, DecompilingClass> classMap;

	private final Map<ClassArrayType, Optional<ReflectClass>> reflectClassMap = new HashMap<>();

	/**
	 * Декомпилирует все классы с переданными именами.
	 * @param classLoader загрузчик классов, используемый для поиска классов для декомпиляции.
	 * @param classNames список полных имён классов, в качестве разделителя можно использовать
	 *                   {@literal .} или {@literal /}.
	 */
	public void run(ClassLoader classLoader, String... classNames) {
		run(Arrays.stream(classNames), name -> getResource(classLoader, name));
	}

	/**
	 * Декомпилирует все переданные классы.
	 * @param classes список классов для декомпиляции.
	 */
	public void run(Class<?>... classes) {
		run(Arrays.stream(classes), Decompiler::getResource);
	}

	/**
	 * Декомпилирует поток классов.
	 * @param stream список классов для декомпиляции.
	 */
	public void run(Stream<Class<?>> stream) {
		run(stream, Decompiler::getResource);
	}


	/**
	 * Декомпилирует поток объектов.
	 * @param stream поток объектов.
	 * @param resourceGetter функция, принимающая объект и возвращающая {@code InputStream} соответствующего class-файла.
	 */
	public <T> void run(
			Stream<? extends T> stream,
	        FailableFunction<T, ? extends InputStream, ? extends IOException> resourceGetter
	) {
		try {
			run0(stream, resourceGetter);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private <T> void run0(
			Stream<? extends T> stream,
	        FailableFunction<T, ? extends InputStream, ? extends IOException> resourceGetter
	) throws IOException {

		this.classMap = Streams.failableStream(stream)
				.map(value -> {
					try (var in = resourceGetter.apply(value)) {
						return new DecompilingClass(this, new ClassReader(in));
					} catch (Throwable throwable) {
						System.err.printf("Exception while creating class %s\n", value);
						throw throwable;
					}
				}).collect(Collectors.toMap(DecompilingClass::getThisType, clazz -> clazz));

		Collection<DecompilingClass> classes = classMap.values();

		classes.forEach(tryCatch(clazz -> clazz.initNested(classMap), "initNested"));

		classes.forEach(tryCatch(DecompilingClass::decompile, "decompile"));
		classes.forEach(tryCatch(DecompilingClass::afterDecompilation, "afterDecompilation"));
		classes.forEach(tryCatch(DecompilingClass::processVariables, "processVariables"));

		classes.forEach(tryCatch(DecompilingClass::addImports, "addImports"));
		classes.forEach(tryCatch(DecompilingClass::computeImports, "computeImports"));


		try (var writer = new DecompilationWriter(writerFactory, config)) {
			Streams.failableStream(classes.stream())
					.filter(DecompilingClass::isTopLevel)
					.forEach(clazz -> {
						try {
							writer.openWriter(clazz.getThisType().getName());
							clazz.write(writer);
							writer.closeWriter();

						} catch (Throwable throwable) {
							writer.flush();
							System.err.println("Exception while writing class " + clazz.getThisType());
							throw throwable;
						}
					});
		}
	}

	private Consumer<DecompilingClass> tryCatch(Consumer<DecompilingClass> method, String stage) {
		return decompilingClass -> {
			try {
				method.accept(decompilingClass);
			} catch (Throwable throwable) {
				System.err.printf(
						"Exception on stage %s while processing class %s\n",
						stage, decompilingClass
				);
				throw throwable;
			}
		};
	}


	private static InputStream getResource(Class<?> clazz) throws IOException {
		String path = clazz.getName().substring(clazz.getPackageName().length() + 1) + ".class";

		return throwIOExceptionIfNull(clazz.getResourceAsStream(path), clazz.getName());
	}

	private static InputStream getResource(ClassLoader classLoader, String className) throws IOException {
		String path = className.replace('.', '/') + ".class";

		return throwIOExceptionIfNull(classLoader.getResourceAsStream(path), className);
	}


	private static InputStream throwIOExceptionIfNull(@Nullable InputStream inputStream, String className) throws IOException {
		if (inputStream == null) {
			throw new IOException("Resource is not found: " + className);
		}

		return inputStream;
	}


	public static FailableFunction<String, InputStream, IOException> fileResourceGetter(String dir) {
		return name -> new BufferedInputStream(new FileInputStream(
				Path.of(dir, name + ".class").toFile()
		));
	}


	public Optional<DecompilingClass> findClass(@Nullable ClassArrayType type) {
		if (classMap == null) {
			throw new UnsupportedOperationException("Class map has not been initialized yet");
		}

		return Optional.ofNullable(classMap.get(type));
	}

	public Optional<? extends IClass> findIClass(@Nullable ClassArrayType type) {
		return findClass(type).<IClass>map(c -> c).or(() -> findReflectClass(type));
	}

	private Optional<ReflectClass> findReflectClass(@Nullable ClassArrayType classArrayType) {
		if (classArrayType == null)
			return Optional.empty();

		return reflectClassMap.computeIfAbsent(classArrayType, type -> {
			try {
				return Optional.of(new ReflectClass(Class.forName(type.getCanonicalBinName())));
			} catch (ClassNotFoundException ex) {
				Log.warn("Cannot find class %s", type);
				return Optional.empty();
			}
		});
	}
}
