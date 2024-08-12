package examples;

import static org.ugp.serialx.Utils.InvokeFunc;
import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.splitValues;

import java.lang.reflect.InvocationTargetException;

import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.ProtocolConverter;
import org.ugp.serialx.juss.JussSerializer;

/**
 * This is example of more advanced parser! It can be used for calling non-static methods from objects via "->" operator!<br>
 * For example with this parser registered with {@link JussSerializer#JUSS_PARSERS} you can print out hello world in JUSS like <code>System::out->println "Hello world"</code><br>
 * Note: This is only for demonstration purposes and not a real feature so its not fully compatible with JUSS syntax so you will have to use () quiet often depending on where you put this parser!
 * 
 * @author PETO
 *
 * @serial 1.3.5
 */
public class MemberInvokeOperator implements DataParser
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args)
	{
		int index;
		if ((index = indexOfNotInObj(str, 0, str.length(), -1, false, "->")) > 0)
		{
			Object obj = myHomeRegistry.parse(str.substring(0, index).trim(), args);
			String[] funcArgs = splitValues(str.substring(index+2).trim(), ' ');	

			try 
			{
				return InvokeFunc(obj, funcArgs[0], ProtocolConverter.parseAll(myHomeRegistry, funcArgs, 1, true, args));
			} 
			catch (InvocationTargetException e) 
			{
				throw new RuntimeException(e);
			}
			catch (Exception e2)
			{
				return null;
			}
		}
		return CONTINUE;
	}
}
