package org.ugp.serialx.converters.operators;

import static org.ugp.serialx.Utils.fastReplace;
import static org.ugp.serialx.Utils.isOneOf;
import static org.ugp.serialx.Utils.multilpy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Utils;
import org.ugp.serialx.converters.DataParser;

/**
 * This parser provides arithmetics operators to evaluate mathematical expressions such as <code>5 * 2 +-- 2 / 2 ** 2 % 4</code> = 10!
 * 
 * @author PETO
 *
 * @since 1.3.0
 */
public class ArithmeticOperators implements DataParser
{
	protected String[] priority1Oprs = {"*", "*-", "/", "/-", "%"}, priority2Oprs = {"**", "**-"};
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String s, Object... args) 
	{
		if (s.length() > 2 && isExpression(s, '+', '-', '*', '/', '%'))
			return eval(myHomeRegistry, s, args);
		return CONTINUE;
	}
	
	/**
	 * @return Result of evaluated expression that was inserted! For instance 5 + 5, result 10!
	 * 
	 * @since 1.3.0
	 */
	protected Object eval(ParserRegistry registryForParsers, String expr, Object... argsForParsers)
	{
		while (expr.contains("++") || expr.contains("--") || expr.contains("+-") || expr.contains("-+"))
			expr = fastReplace(fastReplace(fastReplace(fastReplace(expr, "-+", "-"), "+-", "-"), "--", "+"), "++", "+");

		List<Object>[] terms = getTerms(expr, '+', '-', '*', '/', '%');
		List<Object> cofs = terms[0];
		if (cofs.size() <= 1)
			return CONTINUE;
		List<Object> oprs = terms[1]; 

		Object cof1 = null, cof2 = null;
		String opr = null;
		try 
		{
			for (int i = 0, index = -1, orderIndex = 0, size = oprs.size(); i < size; index = -1, i++) 
			{
				for (String adept : priority1Oprs)
					if ((orderIndex = oprs.indexOf(adept)) > -1 && (index == -1 || orderIndex < index))
						index = orderIndex;
				for (String adept : priority2Oprs)
					if ((orderIndex = oprs.indexOf(adept)) > -1)
						index = orderIndex;

				cof1 = cofs.get(index = index < 0 ? 0 : index);
				if (cof1 instanceof String)
					cof1 = registryForParsers.parse(cof1.toString().trim(), i > 0, new Class[] {getClass()}, argsForParsers);
				cof1 = cof1 instanceof ResultWrapper ? ((ResultWrapper) cof1).obj : cof1;
	
			    cof2 = cofs.remove(index + 1);
			    if (cof2 instanceof String)
			    	cof2 = registryForParsers.parse(cof2.toString().trim(), i > 0, new Class[] {getClass()}, argsForParsers);
			    cof2 = cof2 instanceof ResultWrapper ? ((ResultWrapper) cof2).obj : cof2;
			    
				opr = oprs.remove(index).toString();
				if (opr.charAt(0) == '+')
					cofs.set(index, new ResultWrapper(addOperator(cof1, cof2)));
				else if (opr.charAt(0) == '-')
					cofs.set(index, new ResultWrapper(subOperator(cof1, cof2)));
				else if (opr.startsWith("**"))
					cofs.set(index, new ResultWrapper(powOperator(cof1, cof2, opr.endsWith("-") ? -1 : 1)));
				else if (opr.charAt(0) == '*')
					cofs.set(index, new ResultWrapper(multOperator(cof1, cof2, opr.endsWith("-") ? -1 : 1)));
				else if (opr.charAt(0) == '/')
					cofs.set(index, new ResultWrapper(divOperator(cof1, cof2, opr.endsWith("-") ? -1 : 1)));
				else if (opr.charAt(0) == '%')
					cofs.set(index, new ResultWrapper(modOperator(cof1, cof2)));
			}
		}
		catch (ClassCastException ex)
		{
			LogProvider.instance.logErr("Arithmetic operator " + opr + " is undefined between " + cof1.getClass().getName() + " and " + cof2.getClass().getName() + "!", ex);
		}
		catch (IndexOutOfBoundsException ex)
		{
			LogProvider.instance.logErr("Missing coefficient in \"" + expr + "\"!", ex);
		}
		catch (ArithmeticException ex)
		{
			LogProvider.instance.logErr(ex.getMessage(), ex);
		}
		
		return (cof1 = cofs.get(0)) instanceof ResultWrapper ? ((ResultWrapper) cof1).obj : cof1;
	}
	
	/** 
	 * @return Addition of cof and cof2 (cof + cof2) supposed to be returned! 
	 * 
	 * @since 1.3.2
	 */
	public Object addOperator(Object cof, Object cof2)
	{
		return add(toNum(cof), cof instanceof String ? cof2 : toNum(cof2));
	}
	
	/** 
	 * @return Subtraction of cof and cof2 (cof - cof2) supposed to be returned! 
	 * 
	 * @since 1.3.2
	 */
	public Object subOperator(Object cof, Object cof2)
	{
		return sub(toNum(cof), toNum(cof2));
	}
	
	/** 
	 * @return Multiplication of cof and cof2 multiplied by sign (cof * cof2 * sign) supposed to be returned! 
	 * 
	 * @since 1.3.2
	 */
	public Object multOperator(Object cof, Object cof2, int sign)
	{
		return mult(toNum(cof), toNum(cof2), sign);
	}
	
	/** 
	 * @return Division of cof and cof2 multiplied by sign (cof / cof2 * sign) supposed to be returned! 
	 * 
	 * @since 1.3.2
	 */
	public Object divOperator(Object cof, Object cof2, int sign)
	{
		return div(toNum(cof), toNum(cof2), sign);
	}
	
	/** 
	 * @return Modulation of cod and cof2 (cof % cof2) supposed to be returned! 
	 * 
	 * @since 1.3.2
	 */
	public Object modOperator(Object cof, Object cof2)
	{
		return mod(toNum(cof), toNum(cof2));
	}
	
	/** 
	 * @return Cof powered by cof2 multiplied by sign (Math.pow(cof, cof2 * sign)) supposed to be returned! 
	 * 
	 * @since 1.3.2
	 */
	public Object powOperator(Object cof, Object cof2, int sign)
	{
		return pow(toNum(cof), toNum(cof2), sign);
	}
	
	/**
	 * @return Addition of cof and cof2 (cof + cof2)! 
	 * 
	 * @since 1.3.0
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object add(Object cof, Object cof2)
	{
		if (cof instanceof Number && cof2 instanceof Number)
		{
			if (cof instanceof Double || cof2 instanceof Double)
				return ((Number) cof).doubleValue() + ((Number) cof2).doubleValue();
			if (cof instanceof Float || cof2 instanceof Float)
				return ((Number) cof).floatValue() + ((Number) cof2).floatValue();
			if (cof instanceof Integer || cof2 instanceof Integer)
				return ((Number) cof).intValue() + ((Number) cof2).intValue();
			if (cof instanceof Long || cof2 instanceof Long)
				return ((Number) cof).longValue() + ((Number) cof2).longValue();
			
			return ((Number) cof).doubleValue() + ((Number) cof2).doubleValue();
		}
		
		if (cof.getClass().isArray())
			return Utils.mergeArrays(cof, cof2);
		
		if (cof instanceof Collection)
		{
			if (cof2 instanceof Collection)
				((Collection) cof).addAll(((Collection) cof2));
			else if (cof2.getClass().isArray())
				((Collection) cof).addAll(Arrays.asList(Utils.fromAmbiguousArray(cof2)));
			else 
				((Collection) cof).add(cof2);
			return cof;
		}
		
		if (cof instanceof Scope)
		{
			if (cof2 instanceof Scope)
				((Scope) cof).addAll(((Scope) cof2));
			else if (cof2 instanceof Collection)
				((Scope) cof).addAll((Collection) cof2);
			else if (cof2.getClass().isArray())
				((Scope) cof).addAll(Utils.fromAmbiguousArray(cof2));
			else 
				((Scope) cof).add(cof2);
			return cof;
		}
		return String.valueOf(cof) + String.valueOf(cof2);
	}
	
	/**
	 * @return Subtraction of cof and cof2 (cof - cof2)!
	 * 
	 * @since 1.3.0
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object sub(Object cof, Object cof2)
	{
		if (cof instanceof Number && cof2 instanceof Number)
		{
			if (cof instanceof Double || cof2 instanceof Double)
				return ((Number) cof).doubleValue() - ((Number) cof2).doubleValue();
			if (cof instanceof Float || cof2 instanceof Float)
				return ((Number) cof).floatValue() - ((Number) cof2).floatValue();
			if (cof instanceof Integer || cof2 instanceof Integer)
				return ((Number) cof).intValue() - ((Number) cof2).intValue();
			if (cof instanceof Long || cof2 instanceof Long)
				return ((Number) cof).longValue() - ((Number) cof2).longValue();

			return ((Number) cof).doubleValue() - ((Number) cof2).doubleValue();
		}
		
		if (cof instanceof Collection)
		{
			if (cof2 instanceof Collection)
				((Collection) cof).removeAll(((Collection) cof2));
			else if (cof2.getClass().isArray())
				((Collection) cof).removeAll(Arrays.asList(Utils.fromAmbiguousArray(cof2)));
			else 
				((Collection) cof).remove(cof2);
			return cof;
		}
		
		return null;
	}
	
	/**
	 * @return Multiplication of cof and cof2 multiplied by sign (cof * cof2 * sign)!
	 * 
	 * @since 1.3.0
	 */
	public static Object mult(Object cof, Object cof2, int sign)
	{
		if (cof2 instanceof Number)
		{
			if (!(cof instanceof Number))
				return multilpy(cof.toString(), ((Number) cof2).intValue() * sign).toString();
			
			if (cof instanceof Double || cof2 instanceof Double)
				return ((Number) cof).doubleValue() * ((Number) cof2).doubleValue() * sign;
			if (cof instanceof Float || cof2 instanceof Float)
				return ((Number) cof).floatValue() * ((Number) cof2).floatValue() * sign;
			if (cof instanceof Integer || cof2 instanceof Integer)
				return ((Number) cof).intValue() * ((Number) cof2).intValue() * sign;
			if (cof instanceof Long || cof2 instanceof Long)
				return ((Number) cof).longValue() * ((Number) cof2).longValue() * sign;
	
			return ((Number) cof).doubleValue() * ((Number) cof2).doubleValue() * sign;
		}
		
		return multilpy(cof2.toString(), ((Number) cof).intValue() * sign).toString();
	}
	
	/**
	 * @return Division of cof and cof2 multiplied by sign (cof / cof2 * sign)!
	 * 
	 * @since 1.3.0
	 */
	public static Object div(Object cof, Object cof2, int sign)
	{
		if (cof instanceof Double || cof2 instanceof Double)
			return ((Number) cof).doubleValue() / ((Number) cof2).doubleValue() * sign;
		if (cof instanceof Float || cof2 instanceof Float)
			return ((Number) cof).floatValue() / ((Number) cof2).floatValue() * sign;
		if (cof instanceof Integer || cof2 instanceof Integer)
			return ((Number) cof).intValue() / ((Number) cof2).intValue() * sign;
		if (cof instanceof Long || cof2 instanceof Long)
			return ((Number) cof).longValue() / ((Number) cof2).longValue() * sign;

		return ((Number) cof).doubleValue() / ((Number) cof2).doubleValue() * sign;
	}
	
	/**
	 * @return Modulation of cod and cof2 (cof % cof2)!
	 * 
	 * @since 1.3.0
	 */
	public static Object mod(Object cof, Object cof2)
	{
		if (cof instanceof Double || cof2 instanceof Double)
			return ((Number) cof).doubleValue() % ((Number) cof2).doubleValue();
		if (cof instanceof Float || cof2 instanceof Float)
			return ((Number) cof).floatValue() % ((Number) cof2).floatValue();
		if (cof instanceof Integer || cof2 instanceof Integer)
			return ((Number) cof).intValue() % ((Number) cof2).intValue();
		if (cof instanceof Long || cof2 instanceof Long)
			return ((Number) cof).longValue() % ((Number) cof2).longValue();
		
		return ((Number) cof).doubleValue() % ((Number) cof2).doubleValue();
	}
	
	/**
	 * @return Cof powered by cof2 multiplied by sign (Math.pow(cof, cof2 * sign))!
	 * 
	 * @since 1.3.0
	 */
	public static Object pow(Object cof, Object cof2, int sign)
	{
		if (cof instanceof Number && cof2 instanceof Number)
		{
			double pow = Math.pow(((Number) cof).doubleValue(), ((Number) cof2).doubleValue() * sign);
			if (pow > Long.MAX_VALUE || pow < Long.MIN_VALUE || cof instanceof Double || cof2 instanceof Double)
				return pow;
			else if (pow <= Float.MAX_VALUE && pow >= Float.MIN_VALUE && (cof instanceof Float || cof2 instanceof Float))
				return (float) pow;
			
			if (pow > Integer.MAX_VALUE || pow < Integer.MIN_VALUE || cof instanceof Long || cof2 instanceof Long)
				return (long) pow;
			else if (cof instanceof Integer || cof2 instanceof Integer)
				return (int) pow;
		}
		return null;	
	}
	
	/**
	 * 
	 * @param str | String to split!
	 * @param operatos | If true, list of spited operators will be returned otherwise terms split after each operator.
	 * @param oprs | Operators to use as a splitters.
	 * 
	 * @return List of terms splitted according to inserted arguments! For example <code>getTerm("5 + 6", true, '+')</code> will return <code>[+]</code>, while <code>getTerm("5 + 6", false, '+')</code> will return <code>[5, 6]</code>! 
	 *
	 * @since 1.3.0
	 */
	@SuppressWarnings("unchecked")
	public static List<Object>[] getTerms(String str, char... oprs)
	{
		List<Object>[] ret = new ArrayList[] {new ArrayList<>(), new ArrayList<>()}; //cofs, ops
		
		StringBuilder[] sbs = {new StringBuilder(), new StringBuilder()}; //cofs, ops
		
		int i = 0, type = 0, len = str.length();
		for (; i < len; i++) //in case of start cof sign
		{
			char ch = str.charAt(i);
			if (isOneOf(ch, oprs))
				sbs[0].append(ch);
			else
				break;
		}
		
		for (int quote = 0, brackets = 0, lastType = type; i < len; i++) 
		{
			char ch = str.charAt(i);
			if (ch == '\"')
				quote++;
			else if ((ch | ' ') == '{')
				brackets++;
			else if ((ch | ' ') == '}')
				brackets--;
			
			if (type == 1 || quote % 2 == 0 && brackets == 0)
			{
				if ((type = isOneOf(ch, oprs) ? 1 : 0) != lastType)
				{
					String s = sbs[lastType].toString().trim();
					if (!s.isEmpty())
						ret[lastType].add(s);
					sbs[lastType] = new StringBuilder();
				}
				else
					type = lastType;
			}

			sbs[lastType = type].append(ch);
		}
		
		if (sbs[type].length() > 0)
		{
			String s = sbs[type].toString().trim();
			if (!s.isEmpty())
				ret[type].add(s);
		}
		return ret;
	}
	
	/**
	 * @param str | String that might be an expression!
	 * @param operators | Operators that str must have!
	 * 
	 * @return True if inserted string is expression with any coefficients splitted by operators!
	 * 
	 * @since 1.3.2
	 */
	public static boolean isExpression(CharSequence str, char... operators)
	{
		int hasOpr = -1;
		for (int i = 0, len = str.length(), oldCh = 0, isCof = 0, quote = 0, brackets = 0; i < len; i++)
		{
			char ch = str.charAt(i);
			if (ch > 32)
			{
				if (ch == '\"')
					quote++;
				else if (quote % 2 == 0)
				{
					if ((ch | ' ') == '{')
						brackets++;
					else if ((ch | ' ') == '}')
						brackets--;
					else if (brackets == 0)
					{
						if (isOneOf(ch, operators))
							hasOpr = isCof = 0;
						else if (oldCh <= 32 && isCof == 1)
							return false;
						else
							isCof = 1;
					}
				}
			}
			oldCh = ch;
		}
		return hasOpr == 0;
	}
	
	/**
	 * @return null -> 0, bool ? 1 : 0
	 * 
	 * @since 1.3.0
	 */
	public static Object toNum(Object obj)
	{
		if (obj == null)
			return 0;
		else if (obj instanceof Boolean)
			return (boolean) obj ? 1 : 0;
		else if (obj instanceof Character)
			return (int) (char) obj;
		return obj; 
	}
	
	/**
	 * Used internally by {@link ArithmeticOperators} to wrap result of evaluation!
	 * Mainly used by String results!
	 * 
	 * @author PETO
	 *
	 * @since 1.3.0
	 */
	protected static class ResultWrapper
	{
		public final Object obj;
		
		public ResultWrapper(Object obj) 
		{
			this.obj = obj;
		}
		
		@Override
		public String toString() 
		{
			return obj.toString();
		}
	}
}
