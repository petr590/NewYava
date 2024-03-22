package x590.newyava.annotation;

import x590.newyava.ContextualTypeWritable;
import x590.newyava.Importable;
import x590.newyava.constant.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface AnnotationValue extends ContextualTypeWritable, Importable {
	static AnnotationValue of(Object value) {
		return value.getClass().isArray() ?
				new ArrayValue(asConstantList(value)) :
				Constant.fromObject(value);
	}

	private static List<AnnotationValue> asConstantList(Object value) {
		return switch (value) {
			case byte[] arr -> IntStream.range(0, arr.length).map(i -> arr[i])
					.mapToObj(IntConstant::valueOf).collect(Collectors.toList());

			case short[] arr -> IntStream.range(0, arr.length).map(i -> arr[i])
					.mapToObj(IntConstant::valueOf).collect(Collectors.toList());

			case char[] arr -> IntStream.range(0, arr.length).map(i -> arr[i])
					.mapToObj(IntConstant::valueOf).collect(Collectors.toList());

			case int[] arr -> Arrays.stream(arr)
					.mapToObj(IntConstant::valueOf).collect(Collectors.toList());

			case long[] arr -> Arrays.stream(arr)
					.mapToObj(LongConstant::valueOf).collect(Collectors.toList());

			case float[] arr -> IntStream.range(0, arr.length).mapToDouble(i -> arr[i])
					.mapToObj(f -> FloatConstant.valueOf((float) f)).collect(Collectors.toList());

			case double[] arr -> Arrays.stream(arr)
					.mapToObj(DoubleConstant::valueOf).collect(Collectors.toList());

			case boolean[] arr -> IntStream.range(0, arr.length).map(i -> arr[i] ? 1 : 0)
					.mapToObj(IntConstant::valueOf).collect(Collectors.toList());

			default ->
					throw new IllegalArgumentException("value " + value + " of type " + value.getClass());
		};
	}
}
