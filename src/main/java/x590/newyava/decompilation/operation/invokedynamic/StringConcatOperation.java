package x590.newyava.decompilation.operation.invokedynamic;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import x590.newyava.util.JavaEscapeUtils;
import x590.newyava.constant.StringConstant;
import x590.newyava.context.ClassContext;
import x590.newyava.context.MethodContext;
import x590.newyava.context.MethodWriteContext;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.decompilation.operation.OperationUtils;
import x590.newyava.decompilation.operation.Priority;
import x590.newyava.exception.DecompilationException;
import x590.newyava.io.DecompilationWriter;
import x590.newyava.type.ClassType;
import x590.newyava.type.Type;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode
public class StringConcatOperation implements Operation {
	private static final LdcOperation EMPTY_STRING = new LdcOperation(StringConstant.valueOf(""));


	private final List<Operation> operands = new ArrayList<>();

	public StringConcatOperation(MethodContext context, List<String> bootstrapArgs, @Unmodifiable List<Type> argTypes) {
		var args = new LinkedList<Operation>();

		for (var iter = argTypes.listIterator(argTypes.size()); iter.hasPrevious(); ) {
			args.push(context.popAs(iter.previous()));
		}

		String pattern = bootstrapArgs.get(0);

		var bootstrapArgIter = bootstrapArgs.listIterator(1);
		int last = 0;

		for (int i = 0, s = pattern.length(); ; ++i) {
			if (i == s || pattern.charAt(i) == '\1' || pattern.charAt(i) == '\2') {
				if (i > last) {
					operands.add(new LdcOperation(StringConstant.valueOf(pattern.substring(last, i))));
				}

				last = i + 1;
			}

			if (i == s)
				break;

			switch (pattern.charAt(i)) {
				case '\1' -> {
					if (args.isEmpty()) {
						throw new DecompilationException(
								"pattern = \"%s\"; argTypes = %s",
								JavaEscapeUtils.escapeString(pattern), argTypes
						);
					}

					operands.add(OperationUtils.unwrapStringValueOfObject(args.pop()));
				}

				case '\2' -> {
					if (!bootstrapArgIter.hasNext()) {
						throw new DecompilationException(
								"pattern = \"%s\"; bootstrapArgs = %s",
								JavaEscapeUtils.escapeString(pattern), bootstrapArgs
						);
					}

					operands.add(new LdcOperation(StringConstant.valueOf(bootstrapArgIter.next())));
				}
			}
		}


		if (!args.isEmpty()) {
			throw new DecompilationException(
					"pattern = \"%s\"; argTypes = %s",
					JavaEscapeUtils.escapeString(pattern), argTypes
			);
		}

		if (bootstrapArgIter.hasNext()) {
			throw new DecompilationException(
					"pattern = \"%s\"; bootstrapArgs = %s",
					JavaEscapeUtils.escapeString(pattern), bootstrapArgs
			);
		}


		if (!operands.get(0).getReturnType().equals(ClassType.STRING) &&
			(operands.size() < 2 || !operands.get(1).getReturnType().equals(ClassType.STRING))) {

			operands.add(0, EMPTY_STRING);
		}
	}


	@Override
	public Type getReturnType() {
		return ClassType.STRING;
	}

	@Override
	public @UnmodifiableView List<? extends Operation> getNestedOperations() {
		return operands;
	}

	@Override
	public Priority getPriority() {
		return Priority.ADD_SUB;
	}

	@Override
	public void addImports(ClassContext context) {
		context.addImportsFor(operands);
	}

	@Override
	public void write(DecompilationWriter out, MethodWriteContext context) {
		out.record(operands, context, getPriority(), " + ");
	}

	@Override
	public String toString() {
		return String.format("StringConcatOperation(%s)",
				operands.stream().map(Operation::toString).collect(Collectors.joining(" + ")));
	}
}
