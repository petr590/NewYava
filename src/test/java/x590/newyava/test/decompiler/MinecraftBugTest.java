package x590.newyava.test.decompiler;

import org.junit.Test;
import x590.newyava.Config;
import x590.newyava.Decompiler;
import x590.newyava.io.BufferedFileWriterFactory;

import java.nio.file.Path;
import java.util.stream.Stream;

public class MinecraftBugTest {
	private void run(String className) {
		new Decompiler(
				Config.builder().ignoreVariableTable(true).indent("\t").build(),
				new BufferedFileWriterFactory(MinecraftTest.DST_DIR)
		).run(
				Stream.of(getCompiledClassName(className)),
				Decompiler.fileResourceGetter("")
		);
	}

	/**
	 * @param className имя класса в формате {@code "java.lang.Object"} или {@code "java/lang/Object"}
	 * @return путь к классу (без расширения ".class")
	 */
	private static String getCompiledClassName(String className) {
		return Path.of(MinecraftTest.SRC_DIR, className.replace('.', '/')).toString();
	}


//	@Test
	@Bug
	public void bug1() {
		run("net.minecraft.server.players.StoredUserList");
	}

	@Bug(State.FIXED)
	public void bug2() {
		run("net.minecraft.advancements.AdvancementRequirements");
	}

//	@Test
	@Bug
	public void bug3() {
		run("net.minecraft.client.Minecraft");
	}

	@Bug(State.FIXED)
	public void bug4() {
		run("net.minecraft.server.level.DistanceManager");
	}
	
//	@Test
	@Bug
	public void bug5() {
		run("net.minecraft.server.rcon.thread.QueryThreadGs4");
	}

	@Bug(State.FIXED)
	public void bug6() {
		run("net.minecraft.network.VarLong");
	}

	@Bug(State.FIXED)
	public void bug7() {
		run("net.minecraft.client.MouseHandler");
	}

	@Bug(State.FIXED)
	public void bug8() {
		run("net.minecraft.data.worldgen.biome.OverworldBiomes");
	}
}
