package x590.newyava.decompilation.operation;

import lombok.RequiredArgsConstructor;
import x590.newyava.context.Context;
import x590.newyava.decompilation.operation.array.ArrayLengthOperation;
import x590.newyava.decompilation.operation.array.NewArrayOperation;
import x590.newyava.decompilation.operation.invoke.InvokeStaticOperation;
import x590.newyava.decompilation.operation.other.FieldOperation;
import x590.newyava.descriptor.MethodDescriptor;
import x590.newyava.type.Type;
import x590.newyava.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class Template {
	private final Node<?> node;

	public boolean test(Context context, Operation operation) {
		return node.test(context, operation);
	}

	public static Node<FieldOperation> staticSetter(Node<?> valueNode) {
		return new Node<FieldOperation>(FieldOperation.class)
				.and(FieldOperation::isStatic)
				.and(FieldOperation::isSetter)
				.field(FieldOperation::getValue, valueNode);
	}

	public static Node<NewArrayOperation> newArray(Type elementType, Node<?> indexNode) {
		return new Node<NewArrayOperation>(NewArrayOperation.class)
				.and(array -> array.getReturnType().getElementType().equals(elementType))
				.field(array -> Utils.getSingleOrNull(array.getSizes()), indexNode);
	}

	public static Node<ArrayLengthOperation> arrayLength(Node<?> arrayNode) {
		return new Node<ArrayLengthOperation>(ArrayLengthOperation.class)
				.field(ArrayLengthOperation::getArray, arrayNode);
	}

	public static Node<InvokeStaticOperation> invokeStatic(
			Predicate<MethodDescriptor> predicate, Node<?>... argsNodes
	) {
		return new Node<InvokeStaticOperation>(InvokeStaticOperation.class)
				.and(invokeStatic -> predicate.test(invokeStatic.getDescriptor()))
				.and((context, invokeStatic) -> {
					var args = invokeStatic.getArguments();
					int s = Math.min(argsNodes.length, args.size());

					for (int i = 0; i < s; i++) {
						if (!argsNodes[i].test(context, args.get(i))) {
							return false;
						}
					}

					return true;
				});
	}

	@RequiredArgsConstructor
	public static class Node<T extends Operation> {
		private final Class<T> requiredClass;

		private final List<BiPredicate<Context, T>> predicates = new ArrayList<>();

		public Node<T> and(Predicate<T> predicate) {
			return and((context, operation) -> predicate.test(operation));
		}

		public Node<T> and(BiPredicate<Context, T> predicate) {
			predicates.add(predicate);
			return this;
		}

		public <U extends Operation> Node<T> field(Function<T, Operation> getter, Node<U> fieldNode) {
			predicates.add((context, operation) -> fieldNode.test(context, getter.apply(operation)));
			return this;
		}

		public Template make() {
			return new Template(this);
		}

		public boolean test(Context context, Operation operation) {
			@SuppressWarnings("unchecked")
			T op = (T) operation;

			return requiredClass.isInstance(operation) &&
					predicates.stream().allMatch(predicate -> predicate.test(context, op));
		}
	}
}
