package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.Utils.isOneOf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.DataParser;

/**
 * This parser provides ability to use expression groups that can define order of expression evaluation and compilation! 
 * Its usage depends on combination with other parsers especially operators!
 * Its case sensitive!
 * <br>
 * <br>
 * For example: (5 + 5) / 2 = 5 which is different than 5 + 5 / 2 = 7
 * <br>
 * This protocol will insert one additional argument into array of additional parsing args at index 2 that will be of type {@link Map} and it will contains informations about encapsulated groups, do not alter this map in any way unless you know what are you doing!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class OperationGroups implements DataParser 
{
	/**
	 * Opening and closing of group mark!
	 * 
	 * @since 1.3.0
	 */
	public static final String GROUP_MARK_OP = new String(new char[] {127, 128, 129}), GROUP_MARK_CLS = new String(new char[] {129, 128, 127});
	
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		if (str.length() > 1)
		{
			int opIndex = indexOfOpening(str, 0, '('), clsIndex = -1;
			if (opIndex > -1 && (clsIndex = indexOfClosing(str, opIndex, new char[] {'('}, ')')) > -1)
			{
				Map<String, String> runtimeGroupStack = new HashMap<>();
				if (args.length > 2 && args[2] instanceof Map)
					runtimeGroupStack = (Map<String, String>) args[2];
				else
				{
					if (args.length < 3)
						args = Arrays.copyOf(args, 3);
					args[2] = runtimeGroupStack;
				}
				String mark = GROUP_MARK_OP + runtimeGroupStack.size() + GROUP_MARK_CLS;
				runtimeGroupStack.put(mark, str.substring(opIndex+1, clsIndex).trim());

				StringBuilder sb = new StringBuilder(str).replace(opIndex, clsIndex+1, mark);
				return myHomeRegistry.parse(sb.toString(), args);
			}
			
			if (isGroupMark(str))
			{
				if (args.length > 2 && args[2] instanceof Map)
				{
					Map<String, String> runtimeGroupStack = (Map<String, String>) args[2];

					Object[] newArgs = args.clone();
					newArgs[2] = new HashMap<String, String>();
					return myHomeRegistry.parse(runtimeGroupStack.get(str), newArgs);
				}
				LogProvider.instance.logErr("Runtime group stack is trying to be accessed using " + str + " however it was not provided yet!", null);
				return null;
			}
		}
		
		return CONTINUE;
	}
	
	/**
	 * @param s | Char sequence to check!
	 * 
	 * @return Return true if inserted CharSequence match the runtime group mark wrapper!
	 * This is used for internal purposes of {@link OperationGroups}.
	 * 
	 * @since 1.3.0
	 */
	public static boolean isGroupMark(CharSequence s) 
	{
		String op = GROUP_MARK_OP, cls = GROUP_MARK_CLS;
		int lo = op.length(), lc = cls.length(), len = s.length();
		if (len < lo + lc + 1)
			return false;
		for (int i = 0; i < lo; i++)
			if (s.charAt(i) != op.charAt(i))
				return false;

		for (int i = lo, ch; i < len - lc; i++)
			if ((ch = s.charAt(i)) < '0' || ch > '9')
				return false;
		
		for (int i = 0; i < lc; i++)
			if (s.charAt(len-i-1) != cls.charAt(lc-i-1))
				return false;
		return true;
	}
	
	/**
	 * @param str | CharSequence to search!
	 * @param from | Beginning index of search!
	 * @param openings | Openings to find!
	 * 
	 * @return Return index of first opening char found if is not in object or -1 if there is no opening found similar to {@link Serializer#indexOfNotInObj(CharSequence, char...)}!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfOpening(CharSequence str, int from, char... openings)
	{
		for (int len = str.length(), quote = 0, brackets = 0; from < len; from++) 
		{
			char ch = str.charAt(from);
			
			if (ch == '\"')
				quote++;
			else if (quote % 2 == 0)
			{
				if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing opening bracket in: " + str);
				}
				else if (brackets == 0 && isOneOf(ch, openings))
					return from;	
			}
		}
		return -1;
	}
	
	/**
	 * @param str | CharSequence to search!
	 * @param from | Beginning index of search!
	 * @param openings | Openings to count with!
	 * @param closing | Closings to find!
	 * 
	 * @return Return index of first closing char found if is not in object or -1 if no closing is found similar to {@link Serializer#indexOfNotInObj(CharSequence, char...)}! 
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfClosing(CharSequence str, int from, char[] openings, char... closing) 
	{
		for (int len = str.length(), quote = 0, brackets = 0, ops = 0; from < len; from++) 
		{
			char ch = str.charAt(from);
			
			if (ch == '\"')
				quote++;
			else if (quote % 2 == 0)
			{
				if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing opening bracket in: " + str);
				}
				else if (brackets == 0)
				{
					if (isOneOf(ch, openings))
						ops++;
					else if (isOneOf(ch, closing))
					{
						if (ops == 1)
							return from;
						ops--;
					}
				}
			}
		}
		return -1;
	}
}
