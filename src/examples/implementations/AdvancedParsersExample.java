package examples.implementations;

import java.io.File;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.devtools.SerializationDebugger;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.juss.converters.ObjectConverter;
import org.ugp.serialx.juss.converters.VariableConverter;
import org.ugp.serialx.juss.protocols.UniversalObjectInstantiationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol;

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
 * @see TryParser
 * @see MemberInvokeOperator
 * 
 * @since 1.3.5
 */
public class AdvancedParsersExample 
{
	public static void main(String[] args) throws Exception 
	{
		JussSerializer.JUSS_PARSERS.get(ObjectConverter.class).getInvokableClasses().add(System.class); //This is necessary since 1.3.8
		SerializationProtocol.REGISTRY.add(new UniversalObjectInstantiationProtocol<>(Object.class));
		
		//In this case JussSerializer acts as an interpreter for our custom scripting language.
		JussSerializer interpreter = new JussSerializer();
		
		interpreter.setParsers(JussSerializer.JUSS_PARSERS_AND_OPERATORS.clone()); //Allowing usage of operators in our script!
		interpreter.getParsers().addAllAfter(VariableConverter.class, new TryParser(), new MemberInvokeOperator()); //Allowing method calls and try expressions in our script!
		
		interpreter = SerializationDebugger.debug(interpreter);

		LogProvider.instance.setReThrowException(true); //This allows us to implement custom exception handling!
		
		interpreter.LoadFrom(new File("src/examples/implementations/simpleScript.juss")); //Running our script from simpleScript.juss file!
		
		//Printing the results of our script...
		System.out.println(interpreter); //This is not necessary in this case!
	}
}
