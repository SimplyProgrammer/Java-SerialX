package org.ugp.serialx.converters.operators;

import static java.util.Arrays.asList;
import static org.ugp.serialx.Utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;
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
	/**
	 * @deprecated DO NOT USE! USE {@link ArithmeticOperators#evalOperator(Object, String, Object)} AND {@link ArithmeticOperators#getOperatorPriority(String)} INSTEAD!
	 */
	@Deprecated
	protected String[] priority1Oprs = {"*", "*-", "/", "/-", "%"}, priority2Oprs = {"**", "**-"};
	
	/**
	 * Operator characters recognized by {@link ArithmeticOperators}, operators can be any combination of provided characters. Exact behavior is handled by {@link ArithmeticOperators#operator(Object, String, Object)}.<br> Intended for override, should not be null or empty!
	 * 
	 * @since 1.3.8
	 */
	protected char[] operators = {'+', '-', '*', '/', '%'};
	
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		int len;
		if ((len = str.length()) > 2 && isExpression(str, len, operators))
		{
			for (int i = 0; i < len; ) // Format duplicates of [+-]{2}
				if (isOneOf(str.charAt(i++), '+', '-') && i < len && isOneOf(str.charAt(i), '+', '-'))
				{
					len = (str = fastReplace(fastReplace(fastReplace(fastReplace(str, "-+", "-"), "+-", "-"), "--", "+"), "++", "+")).length();
					i--;
				}

			List<?>[] terms = getAndParseTerms(str, len, myHomeRegistry, args, getClass(), operators);
			ArrayList<Object> cofs = (ArrayList<Object>) terms[0];
			if (cofs.size() <= 1)
				return cofs.get(0);
			LinkedList<String> oprs = (LinkedList<String>) terms[1];

			String op = null;
			int index = 1;
			try 
			{
				for (int opPrio = 2; opPrio > 0; opPrio--)
					for (ListIterator<String> iter = oprs.listIterator(); iter.hasNext(); )
					{
						if (getOperatorPriority(op = iter.next()) == opPrio)
						{
							iter.remove();
							cofs.set(index = iter.nextIndex(), operator(cofs.get(index), op, cofs.remove(index + 1)));
						}
					}
				
				for (index = 1; (op = oprs.poll()) != null; )
					cofs.set(0, operator(cofs.get(0), op, cofs.get(index++)));
			}
			catch (ClassCastException ex)
			{
				LogProvider.instance.logErr("Arithmetic operator " + op + " is undefined between provided operands because " + ex.getMessage() + "!", ex);
			}
			catch (IndexOutOfBoundsException ex)
			{
				LogProvider.instance.logErr("Missing operand in \"" + str + "\"!", null);
			}
			catch (ArithmeticException ex)
			{
				LogProvider.instance.logErr(ex.getMessage(), ex);
			}
			
			return cofs.get(0);
		}
		return CONTINUE;
	}
	
	/**
	 * @param opr1 | Operand 1
	 * @param op | The operator/operation
	 * @param opr2 | Operand 2
	 * 
	 * @return Result of binary operation described by op between opr1 and opr2! If operator is not known, opr1 will be returned by default!
	 * 
	 * @since 1.3.8
	 */
	public Object operator(Object opr1, String op, Object opr2)
	{
//		System.err.println(opr1 + op + opr2);
		switch (op.charAt(0))
		{
			case '+':
				return addOperator(opr1, opr2);
			case '-':
				return subOperator(opr1, opr2);
			case '*':
				if (op.length() > 1 && op.charAt(1) == '*')
					return powOperator(opr1, opr2, op.endsWith("-") ? -1 : 1);
				return multOperator(opr1, opr2, op.endsWith("-") ? -1 : 1);
			case '/':
				return divOperator(opr1, opr2, op.endsWith("-") ? -1 : 1);
			case '%':
				return modOperator(opr1, opr2);
		}
		return opr1;
	}
	
	/**
	 * @param op | The operator/operation
	 * 
	 * @return Priority of provided operator (higher number = higher priority, 2 = high, 1 = medium, 0 = low)
	 * 
	 * @since 1.3.8
	 */
	public byte getOperatorPriority(String op)
	{
		switch (op.charAt(0))
		{
			case '+':
			case '-':
				return 0; // Low
			case '*':
				if (op.length() > 1 && op.charAt(1) == '*')
					return 2; // High  **
			default:
				return 1; // Medium  * / %
		}
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
			return mergeArrays(cof, cof2);
		
		if (cof instanceof Collection)
		{
			if (cof2 instanceof Collection)
				((Collection) cof).addAll(((Collection) cof2));
			else if (cof2.getClass().isArray())
				((Collection) cof).addAll(asList(fromAmbiguousArray(cof2)));
			else 
				((Collection) cof).add(cof2);
			return cof;
		}
		
		if (cof instanceof GenericScope)
		{
			if (cof2 instanceof GenericScope)
				((GenericScope) cof).addAll(((GenericScope) cof2));
			else if (cof2 instanceof Collection)
				((GenericScope) cof).addAll((Collection) cof2);
			else if (cof2.getClass().isArray())
				((GenericScope) cof).addAll(fromAmbiguousArray(cof2));
			else 
				((GenericScope) cof).add(cof2);
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
				((Collection) cof).removeAll(asList(fromAmbiguousArray(cof2)));
			else 
				((Collection) cof).remove(cof2);
			return cof;
		}
		
		if (cof instanceof GenericScope)
		{
			if (cof2 instanceof Collection)
				((GenericScope) cof).values().removeAll((Collection) cof2);
			else if (cof2.getClass().isArray())
				((GenericScope) cof).values().removeAll(asList(cof2));
			else 
				((GenericScope) cof).values().remove(cof2);
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
		double pow = Math.pow(((Number) cof).doubleValue(), ((Number) cof2).doubleValue() * sign);
		if (pow > Long.MAX_VALUE || pow < Long.MIN_VALUE || cof instanceof Double || cof2 instanceof Double)
			return pow;
		if (pow <= Float.MAX_VALUE && pow >= Float.MIN_VALUE && (cof instanceof Float || cof2 instanceof Float))
			return (float) pow;
		
		if (pow > Integer.MAX_VALUE || pow < Integer.MIN_VALUE || cof instanceof Long || cof2 instanceof Long)
			return (long) pow;
		if (cof instanceof Integer || cof2 instanceof Integer)
			return (int) pow;
		return pow;
	}

	/**
	 * @param str | String to split!
	 * @param to | Ending index of parsing, exclusive. Everything from this index to the end will be ignored (should be str.length())!
	 * @param registryForParsers | Registry to use for parsing operands!
	 * @param argsForParsers | Arguments for the parse method!
	 * @param classToIgnore | Parser to ignore (should be class of the caller if possible)!
	 * @param oprs | Operators to use as a splitters. 1 or more of these in row will be used as delimiter!
	 * 
	 * @return Array with 2 lists. Index 0 is {@link ArrayList} containing parsed operands of the expression, index 1 is {@link LinkedList} containing operators of the expression!
	 *
	 * @since 1.3.8 (originally getTerms since 1.3.0)
	 */
	@SuppressWarnings("unchecked")
	public static List<?>[] getAndParseTerms(String str, int to, ParserRegistry registryForParsers, Object[] argsForParsers, Class<? extends DataParser> classToIgnore, char... oprs)
	{
		List<Object>[] ret = new List[] {new ArrayList<Object>(), new LinkedList<String>()}; //cofs, ops
		
		int i = 0, startIndex = 0, type = 0;
		for (; i < to && isOneOf(str.charAt(i), oprs); i++); //in case of start cof sign
		
		for (int quote = 0, brackets = 0, lastType = type; i < to; i++) 
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
					String s = str.substring(startIndex, i).trim();
					if (!s.isEmpty())
						ret[lastType].add(lastType == 0 ? registryForParsers.parse(s, false, classToIgnore, argsForParsers) : s);
					startIndex = i;
				}
				else
					type = lastType;
			}
			
			lastType = type;
		}

		String s = str.substring(startIndex, to).trim();
		if (!s.isEmpty())
			ret[type].add(type == 0 ? registryForParsers.parse(s, false, classToIgnore, argsForParsers) : s);
//		System.err.println(ret[0] + "\n" + ret[1] + "\n");
		return ret;
	}
	
	/**
	 * @param str | String that might be an expression!
	 * @param to | Ending index of checking, exclusive (should be str.length())!
	 * @param operators | Operators that str must have!
	 * 
	 * @return True if inserted string is expression with any coefficients splitted by operators!
	 * 
	 * @since 1.3.2
	 */
	public static boolean isExpression(CharSequence str, int to, char... operators)
	{
		int hasOpr = -1;
		for (int i = 0, oldCh = 0, isCof = 0, quote = 0, brackets = 0; i < to; i++)
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
	 * @return null -> 0, bool ? 1 : 0. Otherwise obj.
	 * 
	 * @since 1.3.0
	 */
	public static Object toNum(Object obj)
	{
		if (obj == null)
			return 0;
		if (obj instanceof Boolean)
			return (boolean) obj ? 1 : 0;
		if (obj instanceof Character)
			return (int) (char) obj;
		return obj; 
	}
	
	/**
	 * @deprecated THIS WAS QUIET A MESSY WORKAROUND, DO NOT USE!<br>
	 * 
	 * Used internally by {@link ArithmeticOperators} to wrap result of evaluation!
	 * Mainly used by String results!
	 * 
	 * @author PETO
	 *
	 * @since 1.3.0
	 */
	@Deprecated
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
