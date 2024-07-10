package x590.newyava.annotation;

import x590.newyava.io.GenericWritable;
import x590.newyava.Importable;
import x590.newyava.constant.*;
import x590.newyava.context.ConstantWriteContext;
import x590.newyava.type.ClassType;
import x590.newyava.type.PrimitiveType;
import x590.newyava.type.Type;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Представляет значение параметра аннотации, которое может быть примитивом,
 * строкой, классом, enum-ом или одноуровневым массивом, содержащим один из вышеперечисленных типов.
 */
public interface AnnotationValue extends GenericWritable<ConstantWriteContext>, Importable {
	static AnnotationValue of(Object value) {
		return value.getClass().isArray() ?
				new ArrayValue(asConstantList(value), elementTypeFor(value)) :
				Constant.fromObject(value);
	}

	private static List<AnnotationValue> asConstantList(Object value) {
		return switch (value) {
			case int[] arr -> Arrays.stream(arr)
					.mapToObj(IntConstant::valueOf).collect(Collectors.toList());

			case long[] arr -> Arrays.stream(arr)
					.mapToObj(LongConstant::valueOf).collect(Collectors.toList());

			case float[] arr -> IntStream.range(0, arr.length)
					.mapToObj(i -> FloatConstant.valueOf(arr[i])).collect(Collectors.toList());

			case double[] arr -> Arrays.stream(arr)
					.mapToObj(DoubleConstant::valueOf).collect(Collectors.toList());

			case byte[]    arr -> asListTroughIntStream(arr.length, i -> arr[i]);
			case short[]   arr -> asListTroughIntStream(arr.length, i -> arr[i]);
			case char[]    arr -> asListTroughIntStream(arr.length, i -> arr[i]);
			case boolean[] arr -> asListTroughIntStream(arr.length, i -> arr[i] ? 1 : 0);

			default ->
					throw new IllegalArgumentException("Value " + value + " of type " + value.getClass());
		};
	}

	private static List<AnnotationValue> asListTroughIntStream(int length, IntUnaryOperator indexer) {
		return IntStream.range(0, length).map(indexer)
				.mapToObj(IntConstant::valueOf).collect(Collectors.toList());
	}

	static Type typeFor(Object value) {
		return switch (value) {
			case Boolean   ignored -> PrimitiveType.BOOLEAN;
			case Byte      ignored -> PrimitiveType.BYTE;
			case Short     ignored -> PrimitiveType.SHORT;
			case Character ignored -> PrimitiveType.CHAR;
			case Integer   ignored -> PrimitiveType.INT;
			case Long      ignored -> PrimitiveType.LONG;
			case Float     ignored -> PrimitiveType.FLOAT;
			case Double    ignored -> PrimitiveType.DOUBLE;
			case org.objectweb.asm.Type ignored -> ClassType.CLASS;
			default -> Type.valueOf(value.getClass());
		};
	}

	static Type elementTypeFor(Object value) {
		return switch (value) {
			case boolean[] ignored -> PrimitiveType.BOOLEAN;
			case byte[]    ignored -> PrimitiveType.BYTE;
			case short[]   ignored -> PrimitiveType.SHORT;
			case char[]    ignored -> PrimitiveType.CHAR;
			case int[]     ignored -> PrimitiveType.INT;
			case long[]    ignored -> PrimitiveType.LONG;
			case float[]   ignored -> PrimitiveType.FLOAT;
			case double[]  ignored -> PrimitiveType.DOUBLE;
			default ->
					throw new IllegalArgumentException("Value " + value + " of type " + value.getClass());
		};
	}
}
