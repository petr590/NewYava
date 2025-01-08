package x590.newyava.decompilation.scope;

import lombok.Getter;
import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.decompilation.code.Chunk;

import java.util.List;

public sealed class TryCatchScope extends Scope permits TryScope, CatchScope {
    @Getter
    private final JoiningTryCatchScope joiningScope;

    public TryCatchScope(@Unmodifiable List<Chunk> chunks, JoiningTryCatchScope joiningScope) {
        super(chunks);
        this.joiningScope = joiningScope;
    }
}
