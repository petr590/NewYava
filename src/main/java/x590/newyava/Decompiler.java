package x590.newyava;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.function.FailableFunction;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.objectweb.asm.ClassReader;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Главный класс, выполняющий декомпиляцию.
 * Хранит {@link Config} и список {@link DecompilingClass}.
 */
@RequiredArgsConstructor
public class Decompiler {

	@Getter
	private final Config config;

	private @Nullable @Unmodifiable Map<ClassType, DecompilingClass> classMap;

	public void run(String... classNames) {
		run(Decompiler.class.getClassLoader(), classNames);
	}

	public void run(ClassLoader classLoader, String... classNames) {
		run(Arrays.stream(classNames), name -> getResource(classLoader, name));
	}

	public void run(Class<?>... classes) {
		run(Arrays.stream(classes), Decompiler::getResource);
	}

	private <T> void run(Stream<T> stream, FailableFunction<T, InputStream, IOException> resourceGetter) {
		this.classMap = stream.map(value -> {
					try (var in = resourceGetter.apply(value)) {
						return new DecompilingClass(this, new ClassReader(in));
					} catch (IOException ex) {
						throw new UncheckedIOException(ex);
					}
				}).collect(Collectors.toMap(DecompilingClass::getThisType, clazz -> clazz));

		Collection<DecompilingClass> classes = classMap.values();


		classes.forEach(clazz -> clazz.initNested(classMap));

		classes.forEach(DecompilingClass::decompile);
		classes.forEach(DecompilingClass::processVariables);

		classes.forEach(DecompilingClass::addImports);
		classes.forEach(DecompilingClass::computeImports);

		var writer = new DecompilationWriter(new OutputStreamWriter(System.out));

		classes.stream().filter(DecompilingClass::keep).forEach(clazz -> {
			writer.record("\n\n----------------------------------------------------------------\n\n");
			clazz.write(writer);
		});

		try {
			writer.flush();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
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


	public Optional<DecompilingClass> findClass(ClassType classType) {
		if (classMap == null) {
			throw new UnsupportedOperationException("Class map has not been initialized yet");
		}

		return Optional.ofNullable(classMap.get(classType));
	}
}
