package x590.newyava.decompilation.operation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.Constant;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.Context;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

public class LdcOperation implements Operation {

	@Getter
	private final Constant value;

	@Getter
	private Type returnType;

	public LdcOperation(Constant value) {
		this.value = value;
		this.returnType = value.getType();
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);
	}

	@Override
	public void inferType(Type requiredType) {
		returnType = Type.assignDown(returnType, requiredType);
	}

	@Override
	public void write(DecompilationWriter out, Context context) {
		value.write(out, context, returnType);
	}


	/** @return экземпляр {@link IntConstant}, если операция
	 * является константой {@code int}, иначе {@code null} */
	public static @Nullable IntConstant getIntConstant(Operation operation) {
		return  operation instanceof LdcOperation ldc &&
				ldc.getValue() instanceof IntConstant intConstant ?
				intConstant : null;
	}

	@Override
	public String toString() {
		return String.format("LdcOperation %08x(%s)", hashCode(), value);
	}
}
