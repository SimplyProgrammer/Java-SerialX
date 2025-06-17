package tests.n.benchmarks;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.juss.JussSerializer;

/**
 * StandardBenchmark for SerialX, single shot no warmup...
 * 
 * @version 1.1.1
 * 
 * @since 1.3.8
 * 
 * @author PETO
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 0)
//@Warmup(iterations = 2)
@Measurement(iterations = 1)
//@Measurement(iterations = 3)
@BenchmarkMode(
	Mode.SingleShotTime
)
@Fork(3)
//@Fork(1)
public class StandardBenchmark 
{
	static final int seed = 123; // DO NOT CHANGE
	
	public static class DataState<M>
	{
		protected final M medium;
		protected final Collection<Object> data;
		
		protected String strData; // Setup with setupMedium...
		
		public DataState(M medium, int count) // DO NOT CHANGE (without incr the bench version)
		{
			this.medium = medium;
			
			if (count > 0)
			{
				Object[] dataArr = new Object[count];
				Random rand = new Random(seed);
				for (int i = 0, rng = rand.nextInt(count/2)+2, bool = rng % 2; i < count; i++)
					switch (i % 4)
					{
						case 0:
							dataArr[i] = (++bool % 2 == 0 || bool % 11 == 0); break;
						case 1:
							dataArr[i] = (0.25 + rng + i); break;
						case 2:
							dataArr[i] = (rng + i); break;
						case 3:
							if (i % 11 == 0) 
							{
								dataArr[i] = null; break;
							}
							dataArr[i] = ("bench" + i); break;
					}
				data = Arrays.asList(dataArr);
			}
			else
				data = null;
			
			setupMedium();
			
//			double t0 = System.nanoTime();
//			long i = 4500000000l;
//			while (i-- > 1);
//			double t = System.nanoTime();
//			System.out.println(i + " | " + (t-t0)/1000000);
		}

		public void setupMedium() {}
	}
	
	public Serializer newSerializer(Map<String, ?> vars, Collection<?> data) 
	{
		JussSerializer srl = new JussSerializer(vars, data);
		srl.getParsers().resetCache(); // Cached
//		srl.getParsers().get(StringConverter.class).setParsingCache(new HashMap<>());
		return srl;
	}
	
	@Param({"8000000", "4000000"})
	protected int dataCount;
	
	protected DataState<?> state;
	
	@Setup
	public void setupState(BenchmarkParams params)
	{
		state = new DataState<File>(new File("src/tests/n/benchmarks/_" + dataCount + "_bench.juss"), params.getBenchmark().endsWith("write") ? dataCount : 0);
	}
	
	@Benchmark
	public void _0_write(Blackhole hole) throws IOException
	{
		JussSerializer serializer = (JussSerializer) newSerializer(null, state.data);
//		serializer.setGenerateComments(true);
		
		serializer.SerializeTo((File) state.medium);
		
		hole.consume(serializer);
	}
	
	@Benchmark
	public Object _1_read() throws IOException
	{
		JussSerializer deserializer = (JussSerializer) newSerializer(null, null);
		
		return deserializer.LoadFrom((File) state.medium);
	}
	
//	@Benchmark
//	public void readAndFormat(LargeFileData data, Blackhole hole) throws FileNotFoundException
//	{
//		hole.consume(new JussSerializer().readAndFormat(new FileReader(data.file), false));
//	}
	
	/* IO */
	
//	@Setup
//	public void setupState(BenchmarkParams params)
//	{
//		state = new DataState<File>(new File("src/tests/n/benchmarks/_" + dataCount + "_bench.juss"), dataCount) {
//			@Override
//			public void setupMedium() {
//				StringBuilder sb = new StringBuilder(dataCount*2);
//				for (Object object : data) {
//					sb.append(object);
//				}
//				strData = sb.toString();
//			}
//		};
//	}

//	@Benchmark
//	public void file_readChars(Blackhole hole) throws IOException
//	{
//		try (Reader reader = new FileReader((File) state.medium))
//		{
//			hole.consume(readChars(reader));
//		}
//	}
//	
//	@Benchmark
//	public void file_readLinesAndChars(Blackhole hole) throws IOException, InterruptedException 
//	{
//		try (Reader reader = new FileReader((File) state.medium))
//		{
//			hole.consume(readLinesAndChars(reader));
//		}
//	}
	
	
//	@Benchmark
//	public void file_readCharsArrayed(Blackhole hole) throws IOException, InterruptedException 
//	{
//		try (Reader reader = new FileReader((File) state.medium))
//		{
//			hole.consume(readCharsArrayed(reader));
//		}
//	}
	
//	@Benchmark
//	public void str_readChars(Blackhole hole) throws IOException
//	{
//		try (Reader reader = new StringReader(state.strData))
//		{
//			hole.consume(readChars(reader));
//		}
//	}
//	
//	@Benchmark
//	public void str_readLinesAndChars(Blackhole hole) throws IOException, InterruptedException 
//	{
//		try (Reader reader = new StringReader(state.strData))
//		{
//			hole.consume(readLinesAndChars(reader));
//		}
//	}
	
//	@Benchmark
//	public void str_readCharsArrayed(Blackhole hole) throws IOException, InterruptedException 
//	{
//		try (Reader reader = new StringReader(state.strData))
//		{
//			hole.consume(readCharsArrayed(reader));
//		}
//	}
	
	public static StringBuilder readChars(Reader r) throws IOException
	{
		StringBuilder sb = new StringBuilder();
        for (int ch; (ch = r.read()) != -1; ) {
        	if (ch > 31)
        		sb.append((char) ch);
        }
        
        return sb;
	}
	
	public static StringBuilder readLinesAndChars(Reader r) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(r);

        for (String line; (line = reader.readLine()) != null; ) {
            for (int i = 0, len = line.length(); i < len; i++)
            {
            	char ch = line.charAt(i);
            	if (ch > 31)
            		sb.append(ch);
            }
        }
		
        reader.close();
		return sb;
	}
	
	public static StringBuilder readCharsArrayed(Reader r) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		char[] arr = new char[128*2];

		for (int charsRead; (charsRead = r.read(arr)) != -1; ) {
			for (int i = 0; i < charsRead; i++) {
				if (arr[i] > 31)
					sb.append(arr[i]);
			}
		}
		
		return sb;
	}
	
	public static void main(String[] args) throws Exception 
	{
//		org.openjdk.jmh.Main.main(args);

		OptionsBuilder ob = new OptionsBuilder();
		ob.include(StandardBenchmark.class.getSimpleName());
		ob.jvm(System.getProperty("user.home") + "\\.sdkman\\candidates\\java\\21.0.7-graal\\bin\\java.exe");
		
//		ob.addProfiler(org.openjdk.jmh.profile.StackProfiler.class);
//		ob.addProfiler(org.openjdk.jmh.profile.JavaFlightRecorderProfiler.class, "dir=./bench_results/jfr_out_j8");
		ob.addProfiler(org.openjdk.jmh.profile.JavaFlightRecorderProfiler.class, "dir=./bench_results/jfr_out_j21g");
		
//		ob.result("bench_results/_StandardBenchmark_110_138_j8");
//		ob.result("bench_results/_StandardBenchmark_110_138_j21g");
//		ob.resultFormat(org.openjdk.jmh.results.format.ResultFormatType.TEXT);

		new Runner(ob).run();
		
		System.out.println("StandardBenchmark 1.1.1 | Cached - 1.3.8");
	}
}