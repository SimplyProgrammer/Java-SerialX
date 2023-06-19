package org.ugp.serialx.converters.operators;

import static org.ugp.serialx.Serializer.indexOfNotInObj;
import static org.ugp.serialx.converters.operators.ArithmeticOperators.getTerms;

import java.util.List;

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
		if (s.length() > 2 && indexOfNotInObj(s, '&', '|', '^') > -1)
		{
			List<Object>[] terms = getTerms(s, '&', '|', '^');
			List<Object> cofs = terms[0];
			if (cofs.size() <= 1)
				return CONTINUE;
			
			List<Object> oprs = terms[1]; 

			Object cof1 = null, cof2 = null, opr = null;
			try 
			{
				for (int i = 0, index = 0, size = oprs.size(); i < size; index = 0, i++) 
				{	
					opr = oprs.remove(index);
					if (opr.equals("&&"))
					{
						cof1 = cofs.get(index); 
						if (cof1 instanceof String)
							cof1 = myHomeRegistry.parse(cof1.toString().trim(), i > 0, new Class[] {getClass()}, args);
						if (cof1.equals(false))
						{
							cofs.remove(index + 1);
							cofs.set(index, false);
						}
						else
						{
							cof2 = cofs.remove(index + 1);
							if (cof2 instanceof String)
								cof2 = myHomeRegistry.parse(cof2.toString().trim(), i > 0, new Class[] {getClass()}, args);
							cofs.set(index, andOperator(cof1, cof2));
						}
					}
					else if (opr.equals("||"))
					{
						cof1 = cofs.get(index); 
						if (cof1 instanceof String)
							cof1 = myHomeRegistry.parse(cof1.toString().trim(), i > 0, new Class[] {getClass()}, args);
						if (cof1.equals(true))
						{
							return true;
						}
						else
						{
							cof2 = cofs.remove(index + 1);
							if (cof2 instanceof String)
								cof2 = myHomeRegistry.parse(cof2.toString().trim(), i > 0, new Class[] {getClass()}, args);
							cofs.set(index, orOperator(cof1, cof2));
						}
					}
					else if (opr.equals("^"))
					{
						cof1 = cofs.get(index); 
						if (cof1 instanceof String)
							cof1 = myHomeRegistry.parse(cof1.toString().trim(), i > 0, new Class[] {getClass()}, args);
						
						cof2 = cofs.remove(index + 1);
						if (cof2 instanceof String)
							cof2 = myHomeRegistry.parse(cof2.toString().trim(), i > 0, new Class[] {getClass()}, args);
						cofs.set(index, xorOperator(cof1, cof2));
					}
				}
			}
			catch (ClassCastException ex)
			{
				LogProvider.instance.logErr("Logical operator " + opr + " is undefined between " + cof1.getClass().getName() + " and " + cof2.getClass().getName() + "!", ex);
			}
			catch (IndexOutOfBoundsException e) 
			{
				LogProvider.instance.logErr("Missing coefficient in \"" + s + "\"!", e);
			}
			return cofs.get(0);
		}
		return CONTINUE;
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
	 * @return null -> false or if number > 0
	 * 
	 * @since 1.3.0
	 */
	public static Object toBool(Object obj)
	{
		if (obj == null)
			return false;
		else if (obj instanceof Number)
			return ((Number) obj).doubleValue() > 0;
		else if (obj instanceof Character)
			return (char) obj > 0;
		/*else if (obj instanceof Map)
			return !((Map<?, ?>) obj).isEmpty();
		else if (obj instanceof Collection)
			return !((Collection<?>) obj).isEmpty();
		else if (obj instanceof Scope)
			return !((Scope) obj).isEmpty();
		else if (obj.getClass().isArray())
			return Array.getLength(obj) > 0;*/
		return obj;
	}
}
