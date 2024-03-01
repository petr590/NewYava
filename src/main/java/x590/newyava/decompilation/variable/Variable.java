package x590.newyava.decompilation.variable;

import org.jetbrains.annotations.NotNull;
import x590.newyava.type.Type;

/**
 * Переменная. Содержит тип и имя.
 * Неизменяемый класс.
 */
public record Variable(@NotNull Type type, @NotNull String name) {}
