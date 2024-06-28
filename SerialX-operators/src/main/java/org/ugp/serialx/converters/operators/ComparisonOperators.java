package org.ugp.serialx.converters.operators;

import static org.ugp.serialx.Utils.indexOfNotInObj;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.imports.ImportsProvider;

/**
 * This parser provides comparison operators, <code>> < >= <= == === != !===</code> and <code>instanceof</code>, to compare 2 objects! For example <code>6 > 5</code>, <code>1 < 5</code> or <code>5 == 5</code> returns true!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class ComparisonOperators implements DataParser
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		int len;
		if ((len = str.length()) < 3)
			return CONTINUE;

		int index, op;
		if ((index = indexOfNotInObj(str, '<', '>', '=')) > -1)
		{
			if ((op = str.charAt(index)) != '=') // > <
			{
				boolean orEqual = index+1 < len && str.charAt(index+1) == '=';
				return comparisonOperator(myHomeRegistry.parse(str.substring(0, index).trim(), args), myHomeRegistry.parse(str.substring(index + (orEqual ? 2 : 1)).trim(), args), op == '>', orEqual);
			}
			
			if (/*op == '=' &&*/ str.charAt(index+1) == '=') // ==
			{
				boolean isTripple = index+2 < len && str.charAt(index+2) == '=';
				return equalsOperator(myHomeRegistry.parse(str.substring(0, index).trim(), args), myHomeRegistry.parse(str.substring(index + (isTripple ? 3 : 2)).trim(), args), isTripple);
			}
			if (/*op == '=' &&*/ str.charAt(--index) == '!') // !=
			{
				boolean isTripple = index+2 < len && str.charAt(index+2) == '=';
				return !equalsOperator(myHomeRegistry.parse(str.substring(0, index).trim(), args), myHomeRegistry.parse(str.substring(index + (isTripple ? 3 : 2)).trim(), args), isTripple);
			}
			//System.out.println(str);
		}
		
		if ((index = indexOfNotInObj(str, " instanceof ")) > -1)
		{
			try 
			{
				return ImportsProvider.forName(args.length > 0 ? args[0] : null, str.substring(index+12).trim()).isInstance(myHomeRegistry.parse(str.substring(0, index).trim(), args));
			}
			catch (Exception e)
			{
				LogProvider.instance.logErr("Unable to check if object " + str.substring(0, index).trim() + " is instance of class \"" + str.substring(index+12).trim() + "\" because there is no such a class!", e);
				return null;
			}
		}
		//double t = System.nanoTime();
		//System.out.println((t-t0)/1000000);
		return CONTINUE;
	}
	
	/**
	 * @param obj1 | Object 1!
	 * @param obj2 | Object 2!
	 * @param compareInstances | If true, this method will compare objects using <code>==</code> operator! 
	 * 
	 * @return True supposed to be returned if obj1 equals to obj2 otherwise false similar to {@link Objects#deepEquals(Object)} but this one can handle OOP crosstype number compression such as {@link Integer} and {@link Double}!
	 * 
	 * @since 1.3.2
	 */
	public boolean equalsOperator(Object obj1, Object obj2, boolean compareInstances)
	{
		return equals(obj1, obj2, compareInstances);
	}
	
	/**
	 * @param obj1 | Object 1!
	 * @param obj2 | Object 2 to be compared with Object 1!
	 * @param isGreater | If true, Object 1 must be greater than Object 2 in order for true to be returned!
	 * @param orEqual | If true, Object 1 being numerically equal to Object 2 will result in true to be returned!
	 * 
	 * @return True supposed to be returned according to arguments!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public Object comparisonOperator(Object obj1, Object obj2, boolean isGreater, boolean orEqual)
	{
		try
		{
			return numberComparator(((Number) (obj1 = toCompareFriendly(obj1))).doubleValue(), ((Number) (obj2 = toCompareFriendly(obj2))).doubleValue(), isGreater, orEqual);
		}
		catch (ClassCastException ex)
		{
			if (obj1 instanceof Comparable)
				return numberComparator(((Comparable<Object>) obj1).compareTo(obj2), 0, isGreater, orEqual);

			LogProvider.instance.logErr("Comparison operator is undefined between " + obj1.getClass().getName() + " and " + obj2.getClass().getName() + "!", ex);
			return null;
		}
	}
	
	/**
	 * @return {@link Map} -> {@link Map#size()}, {@link Collection} -> {@link Collection#size()}, {@link CharSequence} -> {@link CharSequence#length()}, array -> array.length otherwise {@link ArithmeticOperators#toNum(Object)}
	 *
	 * @since 1.3.0
	 */
	public static Object toCompareFriendly(Object obj)
	{
		if (obj instanceof Map)
			return ((Map<?, ?>) obj).size();
		else if (obj instanceof Collection)
			return ((Collection<?>) obj).size();
		else if (obj instanceof Scope)
			return ((Scope) obj).valuesCount() + ((Scope) obj).variablesCount();
		else if (obj instanceof CharSequence)
			return ((CharSequence) obj).length();
		else if ((obj = ArithmeticOperators.toNum(obj)).getClass().isArray())
			return Array.getLength(obj);
		return obj;
	}
	
	/**
	 * @param d1 | Number 1!
	 * @param d2 | Number 2 to be compared with number 1!
	 * @param isGreater | If true, number 1 must be greater than number 2 in order for true to be returned!
	 * @param orEqual | If true, number 1 being equal to number 2 will result in true to be returned!
	 * 
	 * @return True according to arguments!
	 * 
	 * @since 1.3.2
	 */
	public static boolean numberComparator(double d1, double d2, boolean isGreater, boolean orEqual)
	{
		return isGreater ? (orEqual ? d1 >= d2 : d1 > d2) : (orEqual ? d1 <= d2 : d1 < d2);
	}
	
	/**
	 * @param obj1 | Object 1!
	 * @param obj2 | Object 2!
	 * 
	 * @return True if obj1 equals to obj2 otherwise false similar to {@link Objects#deepEquals(Object)} but this one can handle OOP crosstype number compression such as {@link Integer} and {@link Double}!
	 * 
	 * @since 1.3.0
	 */
	public static boolean equals(Object obj1, Object obj2)
	{
		return equals(obj1, obj2, false);
	}
	
	/**
	 * @param obj1 | Object 1!
	 * @param obj2 | Object 2!
	 * @param compareInstances | If true, this method will compare objects using <code>==</code> operator! 
	 * 
	 * @return True if obj1 equals to obj2 otherwise false similar to {@link Objects#deepEquals(Object)} but this one can handle OOP crosstype number compression such as {@link Integer} and {@link Double}!
	 * 
	 * @since 1.3.2
	 */
	public static boolean equals(Object obj1, Object obj2, boolean compareInstances)
	{
		return compareInstances ? obj1 == obj2 || obj1 != null && obj2 != null && obj1.getClass() == obj2.getClass() && numericallyEquals(obj1, obj2) : Objects.deepEquals(obj1, obj2) || numericallyEquals(obj1, obj2);
	}
	
	/**
	 * @param obj1 | Object 1!
	 * @param obj2 | Object 2!
	 * 
	 * @return True if obj1 numerically equals to obj2! For instance <code>true == 1</code>, <code>null == 0</code>!
	 * 
	 * @since 1.3.2
	 */
	public static boolean numericallyEquals(Object obj1, Object obj2)
	{
		return (obj1 = ArithmeticOperators.toNum(obj1)) instanceof Number && (obj2 = ArithmeticOperators.toNum(obj2)) instanceof Number && ((Number) obj1).doubleValue() == ((Number) obj2).doubleValue();
	}
}
