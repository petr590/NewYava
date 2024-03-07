package x590.newyava;

import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
@Builder
public class Config {

	private final boolean ignoreVariableTable;
	private final boolean alwaysWriteBrackets;


	private static @Nullable Config instance;

	public static void init(Config config) {
		if (instance != null)
			throw new IllegalStateException("Config already initialized");

		instance = config;
	}

	public static Config getConfig() {
		if (instance == null)
			throw new IllegalStateException("Config yet not initialized");

		return instance;
	}
}
