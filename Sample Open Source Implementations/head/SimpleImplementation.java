package head;

import java.util.Scanner;

import ugp.org.SerialX.Registry;
import ugp.org.SerialX.converters.DataParser;
import ugp.org.SerialX.converters.NumberConverter;
import ugp.org.SerialX.converters.OperationGroups;
import ugp.org.SerialX.converters.operators.ArithmeticOperators;

/**
 * Simple implementation of SerialX latest feature the recursive data parser!
 * In this example we will be creating simple evaluator of mathematical expressions!
 * 
 * @since 1.3.0
 */
public class SimpleImplementation 
{
	static Scanner scIn = new Scanner(System.in);
	
	public static void main(String[] args) 
	{
		/*
		 * We could easily just use DataParser.REGISTRY but there is tone of stuff we do not need and it will just slow it down!
		 */
		Registry<DataParser> parsersRequiredToEvaluateMath = new Registry<>(new OperationGroups(), new ArithmeticOperators(), new NumberConverter());

		/*
		 * This is an example of simple custom parser this one will allow us to reuse answers of out previous evaluations!
		 * We will access this old answer using 'ans' word!
		 * Old ans must be provided as first one of args!
		 */
		DataParser ansParser = new DataParser() 
		{
			@Override
			public Object parse(Registry<DataParser> myHomeRegistry, String str, Object... args) 
			{
				if (str.equalsIgnoreCase("ans"))
				{
					if (args.length > 0)
						return args[0];
					return null;
				}
				return CONTINUE;
			}
		};
		parsersRequiredToEvaluateMath.add(ansParser);
		
		Object oldAnd = null;
		while (true)
		{
			System.out.println("Please insert your math problem!"); //Ask for input!
			String input = scIn.nextLine() ;//Read console input
			if (!(input = input.trim()).isEmpty()) //Avoiding empty input!
				System.out.println(input + " = " + (oldAnd = DataParser.parseObj(parsersRequiredToEvaluateMath, input, oldAnd))+"\n"); //Parsing input!
		}
	}
}
