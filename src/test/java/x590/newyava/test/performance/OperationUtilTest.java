package x590.newyava.test.performance;

import org.openjdk.jmh.annotations.*;
import x590.newyava.constant.IntConstant;
import x590.newyava.decompilation.operation.other.LdcOperation;
import x590.newyava.decompilation.operation.Operation;
import x590.newyava.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode({Mode.AverageTime})
@Fork(value = 1, jvmArgs = {
//		"-XX:+PrintCompilation",
//		"-verbose:gc",
		"--enable-preview",
})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 2, time = 1)
public class OperationUtilTest {
	@SuppressWarnings("all")
	private static final List<Operation>
			operations1 = new ArrayList<>(),
			operations2 = new ArrayList<>(),
			operations3 = new ArrayList<>();

	private static final int[] sizes = {100, 1000, 10_000};
	private static final List<List<Operation>> operationsList = List.of(operations1, operations2, operations3);


	public static void main(String[] args) throws Exception {
		for (int i = 0; i < sizes.length; i++) {
			var operations = operationsList.get(i);
			int size = sizes[i];

			for (int j = 0; j < size; j++) {
				operations.add(new LdcOperation(IntConstant.ZERO));
			}
		}

		org.openjdk.jmh.Main.main(args);
	}

	@Benchmark
	public List<? extends Operation> testAddBefore1() {
		return Utils.addBefore(new LdcOperation(IntConstant.ZERO), operations1);
	}

	@Benchmark
	public List<? extends Operation> testAddBefore2() {
		return Utils.addBefore(new LdcOperation(IntConstant.ZERO), operations1);
	}

	@Benchmark
	public List<? extends Operation> testAddBefore3() {
		return Utils.addBefore(new LdcOperation(IntConstant.ZERO), operations1);
	}
}
