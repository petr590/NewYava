package x590.newyava.test.decompiler;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;
import x590.newyava.io.BufferedFileWriterFactory;

import java.util.stream.Stream;

public class MinecraftBugTest {
	@Test
	@Bug
	public void bug1() {
		new Decompiler(Config.defaultConfig(), new BufferedFileWriterFactory(MinecraftTest.DST_DIR))
				.run(
						Stream.of(MinecraftTest.getCompiledClassName("net/minecraft/server/players/StoredUserList")),
						Decompiler.fileResourceGetter("")
				);
	}

//	@Test
	@Bug(State.FIXED)
	public void bug2() {
		new Decompiler(Config.defaultConfig(), new BufferedFileWriterFactory(MinecraftTest.DST_DIR))
				.run(
						Stream.of(MinecraftTest.getCompiledClassName("net/minecraft/advancements/AdvancementRequirements")),
						Decompiler.fileResourceGetter("")
				);
	}
}
