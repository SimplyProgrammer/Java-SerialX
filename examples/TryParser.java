package examples;

import static org.ugp.serialx.Serializer.indexOfNotInObj;

import org.ugp.serialx.converters.DataParser;

/**
 * This is another example of more "advanced" parser. This one allow you to use "try" keyword and catching exceptions!
 * Note: This is only for demonstration purposes and not a real feature so its not fully compatible with JUSS syntax so you will have to use () quiet often depending on where you put this parser!
 * 
 * @author PETO
 *
 * @since 1.3.5
 * 
 * @see MemberInvokeOperator
 */
public class TryParser implements DataParser 
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		if (indexOfNotInObj(str = str.trim(), "try") == 0)
		{
			try 
			{
				return myHomeRegistry.parse(str.substring(3).trim(), false, new Class<?>[] {getClass()}, args);
			}
			catch (Exception e) 
			{
				return e;
			}
		}
		return CONTINUE;
	}
}
