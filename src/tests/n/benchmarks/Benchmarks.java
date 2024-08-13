package tests.n.benchmarks;

import static org.openjdk.jmh.annotations.Scope.Benchmark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.ugp.serialx.GenericScope;
import org.ugp.serialx.converters.NumberConverter;

@State(Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 100)
@BenchmarkMode(
	Mode.SingleShotTime
//	Mode.Throughput
)
@Fork(2) // 1 or 2
public class Benchmarks {
	
//	@Param({"0", "0b11l", "12345", "-14445", "1 1", "0xff", "0b11111111", "011", "15.222", "16.88e2", "1234_5678_91011"})
//	String nvalue;
	
//	@Param({"java.util.ArrayList 5 5 5", "java.util.concurrent.TimeUnit 1 2 3", "5hjdhjsakhdjsakhdjsahdjhdjak {} 59", "{hjdhjsakhdjsakhdjsahdjhdjak T T T"})
//	String str;
	
//	@Param({"a"})
//	char ch;
//	
//	@Param({"4", "16", "250", "500"})
//	int count;
//	
//	@Param({"true", "f", "TRue", "FaLse", "tru0", "asdasdzxc", "falsr"})
//	String bvalue;
	
	@Param({"adsadas {adsa {asdasd adasdsa asdas} adasdsdas } adsad adas", "{a{b{c}}aaaaaa}"})
	String objs;
	
	GenericScope<Object, Number> scope;
	
	static final Function<Number, Number> trans = val -> ((Number)val).doubleValue()+5;
	
//	DataConverter benchSubject = new NumberConverter();
//	
//	DataConverter benchSubjectOld = new NumberConverter() {
//		public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args ) 
//		{
//			if (arg.length() > 0)
//			{
//				char ch = arg.charAt(0);
//				if (ch == '+' || ch == '-' || ch == '.' || (ch >= '0' && ch <= '9'))
//				{
//					arg = normFormatNum(arg.toLowerCase());
//					ch = arg.charAt(arg.length()-1); //ch = last char
//					
//					if (ch == '.')
//						return CONTINUE;
//					if (Utils.contains(arg, '.') || (!arg.startsWith("0x") && ch == 'f' || ch == 'd'))
//					{
//						if (ch == 'f')
//							return new Float(fastReplace(arg, "f", ""));
//						return new Double(fastReplace(arg, "d", ""));
//					}
//					 
//					try
//					{
//						if (ch == 'l')
//							return new Long(Long.parseLong(fastReplace(fastReplace(fastReplace(arg, "l", ""), "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
//						if (ch == 's')
//							return new Short(Short.parseShort(fastReplace(fastReplace(fastReplace(arg, "s", ""), "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
//						if (ch == 'y')
//							return new Byte(Byte.parseByte(fastReplace(fastReplace(arg, "y", ""), "0b", ""), arg.startsWith("0b") ? 2 : 10));
//						return new Integer(Integer.parseInt(fastReplace(fastReplace(arg, "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
//					}
//					catch (NumberFormatException e)
//					{
//						if (arg.matches("[0-9.]+"))
//							try
//							{	
//								return new Long(Long.parseLong(fastReplace(fastReplace(fastReplace(arg, "l", ""), "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
//							}
//							catch (NumberFormatException e2)
//							{
//								LogProvider.instance.logErr("Number " + arg + " is too big for its datatype! Try to change its datatype to double (suffix D)!", e2);
//								return null;
//							}
//					}
//				}
//			}
//			return CONTINUE;
//		}
//	};
//	
//	BooleanConverter boolConv = new BooleanConverter();
//	
//	BooleanConverter boolConvOld = new BooleanConverter() {
//		public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args) {
//			if (arg.equalsIgnoreCase("T") || arg.equalsIgnoreCase("true"))
//				return new Boolean(true);
//			if (arg.equalsIgnoreCase("F") || arg.equalsIgnoreCase("false"))
//				return new Boolean(false);
//			return CONTINUE;
//		};
//	};
	
//	@Setup()
//	public void setup() {
//		scope = new GenericScope<Object, Number>(null, nums(1000_000));
//	}
	
//	@Benchmark
//	public void bench(Blackhole hole)
//	{
//		hole.consume(benchSubject.parse(null, nvalue));
//	}
//	
//	@Benchmark
//	public void benchOld(Blackhole hole)
//	{
////		hole.consume(Integer.valueOf(value));
////		hole.consume(Double.valueOf(value));
//		hole.consume(benchSubjectOld.parse(null, nvalue));
//	}
	
	@Benchmark
	public void bench(Blackhole hole)
	{
		hole.consume(scope.map(trans));
//		hole.consume(boolConv.parse(null, bvalue));
	}
	
//	@Benchmark
//	public void benchOld(Blackhole hole)
//	{
////		hole.consume(Integer.valueOf(value));
////		hole.consume(Double.valueOf(value));
////		hole.consume(boolConvOld.parse(null, bvalue));
//	}

	public static void main(String[] args) throws Exception {
		OptionsBuilder ob = new OptionsBuilder();
		ob.measurementTime(TimeValue.milliseconds(100));

//		ParserRegistry reg = Operators.install(new ParserRegistry(new OperationGroups(), new VariableConverter(), new StringConverter(), new ObjectConverter(), new ArrayConverter(), new NumberConverter(), new BooleanConverter(), new CharacterConverter(), new NullConverter(), new SerializableBase64Converter()));
//		
//		JussSerializer srl = new JussSerializer();
//		srl.setParsers(reg);
//		System.out.println(reg.parse("-1 + 1"));
//		System.out.println(reg.parse("10 * 2 + 5"));
//		System.out.println(reg.parse("2 ** 4 - 6"));
//		System.out.println(reg.parse("18 ++ 20 -+ 8"));
//		System.out.println(reg.parse("--5 --9"));
//		
//		ob.verbosity(VerboseMode.SILENT);
		Collection<RunResult> runResults = new Runner(ob).run();

//		Scope s = new Scope();
//		s.add(new Scope("hi".equalsIgnoreCase(null), 123));
//		Scope i = s.get(0, Scope.class);
//		System.out.println(i);
//		
//		String str = "0xff";
//		Object num = numberOf(str, 10, 0);
//		System.out.println(num + " | " + num.getClass().getSimpleName());
		
//        for (char i = 0; i < 128; i++)
//            System.out.println((int)i + " " + i + " | " + (i | ' ') + " " + (char)(i | ' '));
		System.out.println(NumberConverter.numberOf("0b11l", '0', 4, 10, 0) + " " + 0b11l);
		System.out.println(NumberConverter.numberOf("1_0_0", '1', 4, 10, 0) + " " + 1_0_0);
		System.out.println(NumberConverter.numberOf(".1e2", '1', 3, 10, 0) + " " + .1e2);
		System.out.println(NumberConverter.numberOf("10e2", '1', 3, 10, 0) + " " + 10e2);
		
//		for (String bval : Arrays.asList("true", "f", "TRue", "FaLse", "tru0", "asdasdzxc", "falsr")) {
//			System.out.println(new BooleanConverter().parse(null, bval));
//		}
	}
	
	public static List<Number> nums(int size)
	{
		ArrayList<Number> nums = new ArrayList<>();
		for (int i = 0; i < size; i++)
			nums.add(i);
		return nums;
	}
}
