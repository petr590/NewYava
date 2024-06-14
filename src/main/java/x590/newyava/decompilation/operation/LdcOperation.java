package x590.newyava.decompilation.operation;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.Constant;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

@Getter
public class LdcOperation implements Operation {

	private final Constant value;

	private Type returnType;

	private boolean implicitCastAllowed;

	public LdcOperation(Constant value) {
		this.value = value;
		this.returnType = value.getType();
	}

	@Override
	public void allowImplicitCast() {
		implicitCastAllowed = true;
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
	public void write(DecompilationWriter out, MethodWriteContext context) {
		value.write(out, new ConstantWriteContext(context, returnType, implicitCastAllowed));
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
