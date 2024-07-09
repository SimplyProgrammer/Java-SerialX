package org.ugp.serialx.converters.operators;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.multilpy;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataParser;

/**
 * This parser provides logical operators to evaluate boolean expressions such as <code>false || true && true ^ false</code> = true!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class LogicalOperators implements DataParser
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String s, Object... args)
	{
		int len;
		if ((len = s.length()) < 3)
			return CONTINUE;

		Object result = CONTINUE;
		int i = 0, index, op = 0, nextOpIndex;
		try
		{
			do
			{
				if ((index = indexOfNotInObj(s, i, len, -1, true, '&', '|', '^')) == -1)
					return result; //CONTINUE;
				if ((op = s.charAt(i = index)) != '^' && (++i >= len || s.charAt(i) != op)) // Ensure && and || and refuse & and |
					continue;
				
				if (result == CONTINUE) // Beginning
					result = myHomeRegistry.parse(s.substring(0, index).trim(), args);
				
				if (op == '|')
				{
					if (TRUE.equals(result))
						return TRUE;

					nextOpIndex = indexOfNotInObj(s, ++i, len, len, true, '&', '|', '^');
					result = orOperator(result, myHomeRegistry.parse(s.substring(i, nextOpIndex).trim(), args));
				}
				else if (op == '&')
				{
					if (FALSE.equals(result))
					{
						i = indexOfNotInObj(s, ++i, len, len, true, '|', '^');
						continue;
					}

					nextOpIndex = indexOfNotInObj(s, ++i, len, len, true, '&', '|', '^');
					result = andOperator(result, myHomeRegistry.parse(s.substring(i, nextOpIndex).trim(), args));
				}
				else
				{
					nextOpIndex = indexOfNotInObj(s, ++i, len, len, true, '&', '|', '^');
					result = xorOperator(result, myHomeRegistry.parse(s.substring(i, nextOpIndex).trim(), args));
				}

				i = nextOpIndex;
			}
			while (i < len);
		}
		catch (ClassCastException ex)
		{
			LogProvider.instance.logErr("Logical operator " + multilpy((char) op, op != '^' ? 2 : 1) + " is undefined between provided operands because " + ex.getMessage() + "!", ex);
		}

		return result;
	}
	
	/**
	 * @return True if obj1 and obj2 is true!
	 * 
	 * @since 1.3.2
	 */
	public Object andOperator(Object obj1, Object obj2)
	{
		return (boolean) toBool(obj1) && (boolean) toBool(obj2);
	}
	
	/**
	 * @return True if obj1 or obj2 is true!
	 * 
	 * @since 1.3.2
	 */
	public Object orOperator(Object obj1, Object obj2)
	{
		return (boolean) toBool(obj1) || (boolean) toBool(obj2);
	}
	
	/**
	 * @return True if obj1 xor obj2 is true!
	 * 
	 * @since 1.3.2
	 */
	public Object xorOperator(Object obj1, Object obj2)
	{
		return (boolean) toBool(obj1) ^ (boolean) toBool(obj2);
	}
	
	/**
	 * @return null -> false or if number > 0. Otherwise obj.
	 * 
	 * @since 1.3.0
	 */
	public static Object toBool(Object obj)
	{
		if (obj == null)
			return FALSE;
		if (obj instanceof Number)
			return ((Number) obj).doubleValue() != 0;
		if (obj instanceof Character)
			return (char) obj != 0;
//		if (obj instanceof CharSequence)
//			return ((CharSequence) obj).length() > 0;
//		if (obj instanceof Map)
//			return !((Map<?, ?>) obj).isEmpty();
//		if (obj instanceof Collection)
//			return !((Collection<?>) obj).isEmpty();
//		if (obj instanceof Scope)
//			return !((Scope) obj).isEmpty();
//		if (obj.getClass().isArray())
//			return Array.getLength(obj) > 0;
		return obj;
	}
}