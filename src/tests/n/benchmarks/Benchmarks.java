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
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.ugp.serialx.Utils;

@State(Benchmark)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 15)
@BenchmarkMode(
//	Mode.SingleShotTime
	Mode.Throughput
)
@Fork(1)
public class Benchmarks {
	
//	@Param({"0", "12345", "-14445", "0xff", "0b11111111", "011", "15.222", "16.8888", "1234567891011"})
//	String value;
	
//	@Param({"java.util.ArrayList 5 5 5", "java.util.concurrent.TimeUnit 1 2 3", "5hjdhjsakhdjsakhdjsahdjhdjak {} 59", "{hjdhjsakhdjsakhdjsahdjhdjak T T T"})
//	String str;
	
	@Param({"a"})
	char ch;
	
	@Param({"4", "16", "250", "500"})
	int count;
	
//	DataConverter benchSubject = new NumberConverter() {
//		public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args) {
//			if (arg.length() > 0)
//			{
//				char ch = arg.charAt(0);
//				if (ch == '+' || ch == '-' || ch == '.' || (ch >= '0' && ch <= '9'))
//					return decode(arg, 10, 0);
//			}
//			return CONTINUE;
//		};
//	};
//	DataConverter benchSubjectOld = new NumberConverter();
	
//	@Setup()
//	public void setup() {
//		
//	}
	
	@Benchmark
	public void bench()
	{
		Utils.multilpy(ch, count);
	}

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
		new Runner(ob).run();

//		Scope s = new Scope();
//		s.add(new Scope("hi".equalsIgnoreCase(null), 123));
//		Scope i = s.get(0, Scope.class);
//		System.out.println(i);
//		
//		String str = "0xff";
//		Object num = numberOf(str, 10, 0);
//		System.out.println(num + " | " + num.getClass().getSimpleName());
		
		System.out.println(Utils.multilpy("iop", 5));
	}
	
	public static boolean equalsLowerCase(CharSequence str, CharSequence lowerCaseOther, int from, int to)
	{
		for (; from < to; from++)
			if ((str.charAt(from) | ' ') != lowerCaseOther.charAt(from))
				return false;
		return true;
	}
	
	public static Number numberOf(CharSequence str, int base, int type)
	{
		char ch0 = str.charAt(0);
		int len = str.length(), start = 0, end = len - 1;

		if (ch0 == '#') //Determine base
		{
			base = 16;
			start++;
		}
		else if (ch0 == '0' && len > 1)
		{
			int ch1 = str.charAt(1) | ' ';
			if (ch1 == 'b')
			{
				base = 2;
				start++;
			}
			else if (ch1 == 'x')
			{
				base = 16;
				start++;
			}
			else if (ch1 != '.')
				base = 8;

			start++;
		}
		
		double result = 0;
		int chEnd = str.charAt(end--) | ' '; //Determine data type
		if (base == 10 ? chEnd >= 'd' : chEnd >= 'l')
			type = chEnd;
		else if (chEnd == '.')
			type = 'd';
		else
			result = chEnd > '9' ? chEnd - 'a' + 10 : chEnd - '0';

		double baseCof = base, exponent = 1;
		for (int ch; end >= start; end--) //Parsing
		{
			if ((ch = str.charAt(end)) == '-') // Neg
				result = -result;
			else if (ch == '.') //Decimal
			{
				result /= baseCof;
				baseCof = 1;
				if (type == 0)
					type = 'd';
			}
			else if (base == 10 && (ch | ' ') == 'e') //Handle E-notation
			{
				if ((exponent = Math.pow(base, result)) < 1 && type == 0);
					type = 'd';
				result = 0;
				baseCof = 1;
			}
			else if (ch != '_' && ch != '+')
			{
				result += (ch > '9' ? (ch | ' ') - 'a' + 10 : ch - '0') * baseCof;
				baseCof *= base;
			}
		}

		result *= exponent;
		
		if (type == 'd')
			return result;
		if (type == 'f')
			return (float) result;
		if (type == 'l' || result > 0x7fffffff || result < 0x80000000)
			return (long) result;
		if (type == 's')
			return (short) result;
		if (type == 'y')
			return (byte) result;
		return (int) result;
	}
}
