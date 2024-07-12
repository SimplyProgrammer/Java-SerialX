package org.ugp.serialx.converters.operators;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataParser;

/**
 * This parser provides ability to negate stuff! For example <code>!true</code> returns false!<br>
 * This include classic numeric negation <code>-</code> and logical negation <code>!</code> operators.
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
		int len, type;
		if ((len = str.length()) < 2)
			return CONTINUE;
		if ((type = str.charAt(0)) == '!' || type == '-')
		{
			int negCount = 1;
			for (int ch; negCount < len && ((ch = str.charAt(negCount)) == '!' || ch == '-'); negCount++);
			
			Object obj = myHomeRegistry.parse(str.substring(negCount).trim(), false, getClass(), args); 
			if (negCount % 2 == 0)
				return obj;

			Object neg = type == '!' ? logicalNotOperator(obj) : notOperator(obj);
			if (obj == neg && !(obj instanceof Number && ((Number) obj).intValue() == 0))
				LogProvider.instance.logErr("Unable to nagete \"" + obj + "\" because object of \"" + obj.getClass().getName() + "\" cant be negated!", null);
			return neg;
		}
		return CONTINUE;
	}
	
	
	/**
	 * @param obj | Object to negate! Numeric in nature or an object!
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
	 * @param obj | Object to negate! Should be boolean!
	 * 
	 * @return Negated object supposed to be returned or same object as argument if object can't be negated! Should, but do not strictly has to be boolean!<br>
	 * Node: By default it has same behavior as {@link NegationOperator#notOperator(Object)} but it can be overridden!
	 * 
	 * @since 1.3.7
	 */
	public Object logicalNotOperator(Object obj)
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
