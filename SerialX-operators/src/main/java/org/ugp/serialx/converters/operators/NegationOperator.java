package org.ugp.serialx.converters.operators;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataParser;

/**
 * This parser provides ability to negate stuff! For example <code>!true</code> returns false!
 * 
 * @author PETO
 * 
 * @since 1.3.0 
 */
public class NegationOperator implements DataParser 
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		int ch, len = str.length();
		if (len > 0 && ((ch = str.charAt(0)) == '-' || ch == '!'))
		{
			int negCount = 1;
			for (int i = negCount; i < len; i++) 
				if ((ch = str.charAt(i)) == '-' || ch == '!')
					negCount++;
				else
					break;
			
			Object obj = myHomeRegistry.parse(str.substring(negCount), args); 
			if (negCount % 2 == 0)
				return obj;
			Object neg = notOperator(obj);
			if (obj == neg && !(obj instanceof Number && ((Number) obj).intValue() == 0))
				LogProvider.instance.logErr("Unable to nagete \"" + obj + "\" because object of \"" + obj.getClass().getName() + "\" cant be negated!", null);
			return neg;
		}
		return CONTINUE;
	}
	
	
	/**
	 * @param obj | Object to negate!
	 * 
	 * @return Negated object supposed to be returned or same object as argument if object can't be negated! Only numbers and booleans can be negated!
	 * 
	 * @since 1.3.2
	 */
	public Object notOperator(Object obj)
	{
		return negate(obj);
	}
	
	/**
	 * @param obj | Object to negate!
	 * 
	 * @return Negated object or same object as argument if object can't be negated! Only numbers and booleans can be negated!
	 * 
	 * @since 1.3.0
	 */
	public static Object negate(Object obj)
	{
		if (obj == null)
			return 0;
		else if (obj instanceof Boolean)
			obj = !((boolean) obj);
		else if (obj.getClass() == Byte.class)
			obj = (byte) -((Number) obj).byteValue();
		else if (obj.getClass() == Short.class)
			obj = (short) -((Number) obj).shortValue();
		else if (obj.getClass() == Integer.class)
			obj = -((int) obj);
		else if (obj.getClass() == Character.class)
			obj = (char) -((Character) obj);
		else if (obj.getClass() == Long.class)
			obj = -((long) obj);
		else if (obj.getClass() == Float.class)
			obj = -((float) obj);
		else if (obj.getClass() == Double.class)
			obj = -((double) obj);
		return obj;
	}
}
