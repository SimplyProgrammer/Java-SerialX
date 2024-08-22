package tests.n.benchmarks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.juss.JussSerializer;

/**
 * StandardBenchmark for SerialX
 * 
 * @since 1.3.8
 * 
 * @author PETO
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@BenchmarkMode(
	Mode.SingleShotTime
//	Mode.Throughput
)
@Fork(3) // 3 or 4
public class StandardBenchmark 
{
	static final int seed = 123; // DO NOT CHANGE
	
	public static abstract class FileDataState
	{
		final File file;
		final List<Object> data;
		
		public FileDataState(File file, int count) // DO NOT CHANGE
		{
			this.file = file;
			
			data = new ArrayList<>();
			Random rand = new Random(seed);
			for (int i = 1; i <= count; i++)
				switch (rand.nextInt(4))
				{
					case 0:
						data.add(rand.nextBoolean()); break;
					case 1:
						data.add(0.25 + rand.nextInt(i)); break;
					case 2:
						data.add(rand.nextInt(i)); break;
					case 3:
						data.add("bench" + i); break;
				}

			try {
				if (file.createNewFile())
					new JussSerializer(null, data).SerializeTo(file);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			long i = 4000000000l;
//			while (i-- > 1);
//			System.out.println(i);
		}
		
		public abstract Serializer newSerializer(Map<String, ?> vars, List<?> data);
	}
	
	@State(Scope.Benchmark)
	public static class LargeFileData extends FileDataState
	{
		public LargeFileData()
		{
			super(new File("src/tests/n/benchmarks/large_bench.juss"), 8000000);
		}

		@Override
		public Serializer newSerializer(Map<String, ?> vars, List<?> data) 
		{
			JussSerializer srl = new JussSerializer(vars, data);
			srl.getParsers().resetCache();
			return srl;
		}
	}

	@State(Scope.Benchmark)
	public static class MediumFileData extends FileDataState
	{
		public MediumFileData()
		{
			super(new File("src/tests/n/benchmarks/medium_bench.juss"), 4000000);
		}
		
		@Override
		public Serializer newSerializer(Map<String, ?> vars, List<?> data) 
		{
			JussSerializer srl = new JussSerializer(vars, data);
			srl.getParsers().resetCache();
			return srl;
		}
	}
	
	@Benchmark
	public void fileLarge_write(LargeFileData data, Blackhole hole) throws IOException
	{
		JussSerializer serializer = (JussSerializer) data.newSerializer(null, data.data);
//		serializer.setGenerateComments(true);
		
		serializer.SerializeTo(data.file);
		
		hole.consume(serializer);
	}
	
	@Benchmark
	public void fileLarge_read(LargeFileData data, Blackhole hole) throws FileNotFoundException
	{
		JussSerializer deserializer = (JussSerializer) data.newSerializer(null, null);
		
		hole.consume(deserializer.LoadFrom(data.file));
	}
	
	@Benchmark
	public void fileMedium_write(MediumFileData data, Blackhole hole) throws IOException
	{
		JussSerializer serializer = (JussSerializer) data.newSerializer(null, data.data);
//		serializer.setGenerateComments(true);
		
		serializer.SerializeTo(data.file);
		
		hole.consume(serializer);
	}

	@Benchmark
	public void fileMedium_read(MediumFileData data, Blackhole hole) throws FileNotFoundException
	{
		JussSerializer deserializer = (JussSerializer) data.newSerializer(null, null);
		
		hole.consume(deserializer.LoadFrom(data.file));
	}
	
//	@Benchmark
//	public void readAndFormat(LargeFileData data, Blackhole hole) throws FileNotFoundException
//	{
//		hole.consume(new JussSerializer().readAndFormat(new FileReader(data.file), false));
//	}
	
	/* IO */
	
//	@Benchmark
//	public StringBuilder file_readChars(LargeFileData data) throws IOException
//	{
//        try (Reader reader = new FileReader(data.file))
//        {
//            return readChars(reader);
//        }
//	}
//	
//	@Benchmark
//    public StringBuilder file_readLinesAndChars(LargeFileData data) throws IOException {
//        try (Reader reader = new FileReader(data.file))
//        {
//            return readLinesAndChars(reader);
//        }
//    }
//	
//	
//	@Benchmark
//	public StringBuilder file_readCharsArrayed(LargeFileData data) throws IOException {
//        try (Reader reader = new FileReader(data.file))
//        {
//            return readCharsArrayed(reader);
//        }
//	}

////StringBuilder sb = new StringBuilder();
////char[] arr = new char[256*4];
////try (Reader reader = new FileReader(fileLarge)) {
////	for (; reader.read(arr) != -1; ) {
////		sb.append(arr);
////	}
////}
////strLarge = sb.toString();

//	@Benchmark
//	public int str_readChars(Blackhole hole) throws IOException
//	{
//		try (Reader reader = new StringReader(strLarge))
//		{
//			return readChars(reader);
//		}
//	}
//	
//	@Benchmark
//	public int str_readLinesAndChars() throws IOException, InterruptedException {
//		try (Reader reader = new StringReader(strLarge))
//		{
//			return readLinesAndChars(reader);
//		}
//	}
//	
//	
//	@Benchmark
//	public int str_readCharsArrayed() throws IOException, InterruptedException {
//		try (Reader reader = new StringReader(strLarge))
//		{
//			return readCharsArrayed(reader);
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
		char[] arr = new char[256];

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

		new Runner(ob).run();
		
		System.out.println("Cached - 1.3.8");
	}
}