package tests.n.benchmarks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.NumberConverter;

public class SimpleBench {
	
	
	@SuppressWarnings("unchecked")
	public static <RESULT, DATA> RESULT[] benchmark(Callable<DATA> benchDataGenerator, Function<DATA, RESULT> action, int count) throws Exception {
		Object[]  results = new Object[count];
		
		double total = 0;
		for (int i = 0; i < count; i++) {
			DATA benchmarkDataData = benchDataGenerator.call();
			
			double t0 = System.nanoTime();
			
			results[i] = action.apply(benchmarkDataData);
			
			double t = System.nanoTime();
			
			total += t-t0;
		}
		
		System.out.println("Avarage time " + String.format("%.5f", total/count/1000000) + "ms");
		
		return (RESULT[]) results;
	}
	
	static Random rand = new Random(12345);
	public static void main(String[] args) throws Exception {
		Callable<Object> data = () -> rand.nextBoolean() ? rand.nextInt(100000) : rand.nextBoolean();
		
		ParserRegistry reg = DataParser.REGISTRY;
		DataConverter benchSubject = new NumberConverter();
		
		List<Object> lol = new ArrayList<>();


		double t0 = System.nanoTime();
		for (int i = 0; i < 1_000_000; i++) {
			lol.add(i);
		}
		double t = System.nanoTime();
		System.out.println((t-t0)/1000000);


//		Object[] results = benchmark(data, state -> {
//			return reg.toString(state);
//		}, 10);
//		System.out.println(results[5].toString());
	}
}