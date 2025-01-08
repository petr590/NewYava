package x590.newyava.decompilation.operation.other;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import x590.newyava.constant.Constant;
import x590.newyava.constant.IntConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.Type;

@Getter
@EqualsAndHashCode
public class LdcOperation implements Operation {

	private final Constant value;

	private Type returnType;

	@EqualsAndHashCode.Exclude
	private boolean
			implicitCastAllowed = false,
			implicitByteShortCastAllowed = true,
			constantsUsingAllowed = true;

	public LdcOperation(Constant value) {
		this.value = value;
		this.returnType = value.getType();
	}

	@Override
	public Type getImplicitType() {
		return value.getImplicitType();
	}

	@Override
	public void allowImplicitCast() {
		implicitCastAllowed = true;
	}

	@Override
	public void denyByteShortImplicitCast() {
		implicitByteShortCastAllowed = false;
	}

	@Override
	public void denyConstantsUsing() {
		constantsUsingAllowed = false;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(value);
	}

	@Override
	public void inferType(Type requiredType) {
		returnType = Type.assignDown(returnType, requiredType);
	}


	private ConstantWriteContext createConstantWriteContext(MethodWriteContext context) {
		return new ConstantWriteContext(context, returnType,
				implicitCastAllowed, implicitByteShortCastAllowed, constantsUsingAllowed);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		value.write(out, createConstantWriteContext(context));
	}

	@Override
	public void writeIntAsChar(DecompilationWriter out, MethodWriteContext context) {
		value.writeIntAsChar(out, createConstantWriteContext(context));
	}

	@Override
	public void writeHex(DecompilationWriter out, MethodWriteContext context) {
		value.writeHex(out, createConstantWriteContext(context));
	}

	/** @return экземпляр {@link IntConstant}, если операция
	 * является константой {@code int}, иначе {@code null} */
	public static @Nullable IntConstant getIntConstant(@Nullable Operation operation) {
		return  operation instanceof LdcOperation ldc &&
				ldc.getValue() instanceof IntConstant intConstant ?
				intConstant : null;
	}

	@Override
	public String toString() {
		return String.format("LdcOperation(%s)", value);
	}
}
