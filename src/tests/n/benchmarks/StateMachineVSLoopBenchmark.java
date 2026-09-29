package tests.n.benchmarks;

import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.ugp.serialx.utils.StrUtils;

/**
 * StandardBenchmark for SerialX, single shot no warmup...
 * 
 * @version 1.1.1
 * 
 * @since 4.0.0
 * 
 * @author PETO
 */
@State(Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
//@Measurement(iterations = 260)
@BenchmarkMode(
//	Mode.SingleShotTime
	Mode.Throughput
)
@Fork(1)
//@Fork(2)
/**
 * Sketch board for benchmarks
 */
public class StateMachineVSLoopBenchmark 
{
	@Param({"sh\"or\" {ta} q# asd0", "ad\"as[djas]kld\"j[akkkok]l#aldasl123", "hi tyu/\"djaslkdjak=sdj\"kdj { adasjdl-*aksdjaldjaklj } \"asd asd\"tt#a5a"})
	String strToSearch;
	
	public static int finteState_indexOfNotInObj(CharSequence s, int from, int to, int defaultReturn, boolean firstIndex, char... oneOf)
	{
		for (; from < to; from++)
		{
			int ch = s.charAt(from);
			if (ch == '"')
				while (++from < to && s.charAt(from) != '"');
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++from >= to)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + s);
					if ((ch = (s.charAt(from) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++from < to && s.charAt(from) != '"');
				}
			}
			else if (StrUtils.isOneOf(ch, oneOf))
			{
				if (firstIndex)
					return from;
				defaultReturn = from;
			}
		}
		return defaultReturn;
	}
	
	public static int oldLoop_indexOfNotInObj(CharSequence s, boolean firstIndex, char... oneOf)
	{
		int found = -1;
		for (int i = 0, brackets = 0, quote = 0, len = s.length(); i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
	
			if (quote % 2 == 0)
			{
				if (brackets == 0 && /*oneOf.length == 0 ? ch == oneOf[0] :*/ StrUtils.isOneOf(ch, oneOf))
				{
					found = i;
					if (firstIndex)
						return found;
				}
				else if (ch == '{' || ch == '[')
					brackets++;
				else if (ch == '}' || ch == ']')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing closing bracket in: " + s);
				}
			}
		}
		return found;
	}

	@Benchmark
	public Object _0_finteStateMachineAproach()
	{
		return finteState_indexOfNotInObj(strToSearch, 0, strToSearch.length(), -1, true, '#');
	}
	
	@Benchmark
	public Object _1_oldLoopAproach()
	{
		return oldLoop_indexOfNotInObj(strToSearch, true, '#');
	}
	
	public static void main(String[] args) throws Exception 
	{
//		org.openjdk.jmh.Main.main(args);
		
//		String jvmVersion = "8.0.412-tem";
		String jvmVersion = "21.0.12-graal";

		OptionsBuilder ob = new OptionsBuilder();
		ob.include(StateMachineVSLoopBenchmark.class.getSimpleName());
		ob.jvm(System.getProperty("user.home") + "\\.sdkman\\candidates\\java\\" + jvmVersion + "\\bin\\java.exe");

//		ob.addProfiler(org.openjdk.jmh.profile.StackProfiler.class);

//		ob.addProfiler(org.openjdk.jmh.profile.JavaFlightRecorderProfiler.class, "dir=./bench_results/_jfr-" + (VERSION + "-" + LIB_VERSION + "-j" + jvmVersion).replace(".", ""));

		ob.result("bench_results/_StateMachineVSLoopBenchmark-" + "j" + jvmVersion.replace(".", "") + ".bn");
		ob.resultFormat(org.openjdk.jmh.results.format.ResultFormatType.TEXT);

		new Runner(ob).run();
	
	}
}