package examples.implementations;

import java.io.File;

import org.ugp.serialx.JussSerializer;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.VariableConverter;

import examples.MemberInvokeOperator;
import examples.TryParser;

/**
 * In this example we will create our very own simple scripting language by using {@link MemberInvokeOperator} and {@link TryParser} 
 * together with {@link JussSerializer#JUSS_PARSERS_AND_OPERATORS}!
 * As you can see with SerialX capable of far more than parsing some JSON... 
 * Note: This is primarily for demonstrational purposes and might not be suitable for production...
 * 
 * @author PETO
 * 
 * @since 1.3.5
 */
public class AdvancedParsersExample 
{
	public static void main(String[] args) throws Exception 
	{
		//In this case JussSerializer acts as an interpreter for our custom scripting language.
		JussSerializer interpreter = new JussSerializer();
		
		interpreter.setParsers(JussSerializer.JUSS_PARSERS_AND_OPERATORS); //Allowing usage of operators in our script!
		interpreter.getParsers().addAllAfter(VariableConverter.class, new TryParser(), new MemberInvokeOperator()); //Allowing method calls and try expressions in our script!
		
		LogProvider.instance.setReThrowException(true); //This allows us to implement custom exception handling!
		
		interpreter.LoadFrom(new File("src/examples/implementations/simpleScript.juss")); //Running our script from simpleScript.juss file!
		
		//Printing the results of our script...
		//System.out.println(interpreter); //This is not necessary in this case!
	}
}
