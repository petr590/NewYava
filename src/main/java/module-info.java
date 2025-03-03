module newyava {
	requires transitive org.objectweb.asm;
	requires transitive org.jetbrains.annotations;
	requires transitive org.apache.commons.text;
	requires transitive org.apache.commons.lang3;
	requires transitive org.apache.commons.collections4;

	requires org.checkerframework.checker.qual;
	requires it.unimi.dsi.fastutil;
	requires com.google.common;
	requires lombok;

	requires java.compiler;
}