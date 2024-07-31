package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.Utils.isOneOf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.ugp.serialx.LogProvider;
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
	 * @deprecated (since 1.3.8) DO NOT USE, USE {@link OperationGroups#groupMark} instead! <br>
	 * 
	 * Opening and closing of group mark!
	 * 
	 * @since 1.3.0
	 */
	@Deprecated
	public static final char[] GROUP_MARK_OP = new char[] {127, 128, 129}, GROUP_MARK_CLS = new char[] {129, 128, 127};
	
	/**
	 * Character marking the opening of {@link OperationGroups} mark in processed string, closing should be marked as this character -1.<br>
	 * This character is generated in sami-random fashion but it will never be an ASCII character.
	 * 
	 * @since 1.3.8
	 */
	protected final char groupMark = (char) (System.nanoTime() | 128);
	
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args)
	{
		if (str.length() > 1)
		{
			int opIndex = indexOfOpening(str, 0, '('), clsIndex;
			if (opIndex > -1 && (clsIndex = indexOfClosing(str, opIndex, new char[] {'('}, ')')) > -1)
			{
				Map<Integer, String> runtimeGroups;
				if (args.length > 2 && args[2] instanceof Map)
					runtimeGroups = (Map<Integer, String>) args[2];
				else
				{
					if (args.length < 3)
						args = Arrays.copyOf(args, 3);
					args[2] = runtimeGroups = new HashMap<>();
				}

				int groupId;
				runtimeGroups.put(groupId = runtimeGroups.size(), str.substring(opIndex+1, clsIndex).trim());
				
				StringBuilder newStr = new StringBuilder(str).replace(opIndex, clsIndex+1, new StringBuilder().append(groupMark).append(groupId).append((char) (groupMark-1)).toString());
				return myHomeRegistry.parse(newStr.toString(), args);
			}
			
			int groupId;
			if ((groupId = isGroupMark(str, groupMark)) != -1)
			{
				if (args.length > 2 && args[2] instanceof Map)
				{
					Map<Integer, String> runtimeGroups = (Map<Integer, String>) args[2];

					Object[] newArgs = args.clone();
					newArgs[2] = new HashMap<Integer, String>();
					return myHomeRegistry.parse(runtimeGroups.get(groupId), newArgs);
				}
				LogProvider.instance.logErr("Runtime group stack is trying to be accessed using " + str + " however it was not provided yet!", null);
				return null;
			}
		}
		
		return CONTINUE;
	}
	
	/**
	 * @param s | Char sequence to check!
	 * @param groupMark | Opening character identifying this group mark, closing should be this character -1
	 * 
	 * @return Id of the group mark if inserted CharSequence matches the runtime group mark wrapper format! -1 otherwise.<br>
	 * This is used for internal purposes of {@link OperationGroups}.
	 * 
	 * @since 1.3.0
	 */
	public static int isGroupMark(CharSequence s, char groupMark) 
	{
		int i = s.length()-1;
		if (i < 2 || s.charAt(0) != groupMark-- || s.charAt(i--) != groupMark)
			return -1;

		int groupId;
		if ((groupId = s.charAt(i--) - '0') < 0 || groupId > 9)
			return -1;
		for (int ch, baseCof = 10; i >= 1; i--, baseCof *= 10)
		{
			if ((ch = s.charAt(i)) < '0' || ch > '9')
				return -1;
			groupId += (ch - '0') * baseCof;
		}
		
		return groupId;
	}
	
	/**
	 * @param str | CharSequence to search!
	 * @param from | Beginning index of search!
	 * @param openings | Openings to find!
	 * 
	 * @return Return index of first opening char found if is not in object or -1 if there is no opening found similar to {@link org.ugp.serialx.Utils#indexOfNotInObj(CharSequence, char...)}!
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
	 * @return Return index of first closing char found if is not in object or -1 if no closing is found similar to {@link org.ugp.serialx.Utils#indexOfNotInObj(CharSequence, char...)}! 
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
