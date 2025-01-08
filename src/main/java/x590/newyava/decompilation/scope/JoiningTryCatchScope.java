package x590.newyava.decompilation.scope;

import org.jetbrains.annotations.Unmodifiable;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.code.Chunk;
import x590.newyava.io.DecompilationWriter;

import java.util.List;

public class JoiningTryCatchScope extends Scope {

    public JoiningTryCatchScope(@Unmodifiable List<Chunk> chunks) {
        super(chunks);
    }

    @Override
    public boolean canShrink() {
        return false;
    }

    @Override
    public void write(DecompilationWriter out, MethodWriteContext context) {
        super.writeBody(out, context);
    }
}
