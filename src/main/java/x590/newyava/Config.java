package x590.newyava;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Config {

	private final boolean ignoreVariableTable;
	private final boolean alwaysWriteBrackets;

	private static Config defaultInstance;

	public static Config defaultConfig() {
		if (defaultInstance != null)
			return defaultInstance;

		return defaultInstance = Config.builder().build();
	}
}
