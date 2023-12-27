package examples.implementations;

import java.util.Scanner;

import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.NumberConverter;
import org.ugp.serialx.converters.operators.ArithmeticOperators;
import org.ugp.serialx.juss.converters.OperationGroups;

/**
 * This example will show you simple implementation of SerialX latest feature the recursive data parser!
 * In this example we will be creating simple evaluator of mathematical expressions!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class SimpleCalculator
{
	static Scanner scIn = new Scanner(System.in);
	
	public static void main(String[] args) 
	{
		/*
		 * We could easily just use DataParser.REGISTRY but there is tone of stuff we do not need and it will just slow it down!
		 */
		ParserRegistry parsersRequiredToEvaluateMath = new ParserRegistry(new OperationGroups(), new ArithmeticOperators(), new NumberConverter());;
		
		/*
		 * This is an example of simple custom parser this one will allow us to reuse answers of out previous evaluations!
		 * We will access this old answer using 'ans' word!
		 * Old ans must be provided as first one of args!
		 */
		DataParser ansParser = new DataParser() 
		{
			@Override
			public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
			{
				if (str.equalsIgnoreCase("ans"))
				{
					if (args.length > 0)
						return args[0]; //First arg is old answer!
					return null;
				}
				return CONTINUE;
			}
		};
		parsersRequiredToEvaluateMath.add(ansParser);

		Object oldAns = null;
		while (true)
		{
			System.out.print("Please insert your math problem: "); //Ask for input!
			String input = scIn.nextLine() ;//Read console input
			if (!(input = input.trim()).isEmpty()) //Avoiding empty input!
			{
				double t0 = System.nanoTime(); //Performing simple benchmark
				oldAns = parsersRequiredToEvaluateMath.parse(input, oldAns); //Notice that we are inserting oldAns as compiler arguments for parseObj which are then picked up by our ansParser as well as every other registered DataParser.
				double t = System.nanoTime();
				
				System.out.println(input + " = " + oldAns +"\n" + (t-t0)/1000000 + "ms \n"); //Parsing input!
			}
		}
	}
}
