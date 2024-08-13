package org.ugp.serialx.converters.operators;

import static org.ugp.serialx.Utils.indexOfNotInObj;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataParser;

/**
 * This parser provides conditional assignment operators more specially ternary and null coalescing! For example <code>true ? "Yes" : "No"</code> will return "Yes" and <code>null ?? "Hello!"</code> will return hello!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class ConditionalAssignmentOperators implements DataParser 
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		int index, ternIfIndex;
		if (str.length() > 2 && (index = indexOfNotInObj(str, '?')) > -1)
		{
			if ((ternIfIndex = indexOfOne(str, index, '?')) > -1) //?:
			{
				try
				{
					String last = str.substring((index = ternIfIndex)+1), first = str.substring(0, index);
					boolean condition = (boolean) LogicalOperators.toBool(myHomeRegistry.parse(first.trim(), args));
					if ((index = indexOfTernaryElse(last, 1, '?', ':')) > -1)
					{
						first = last.substring(0, index);
						return condition ? myHomeRegistry.parse(first.trim(), args) : (last = last.substring(index+1).trim()).isEmpty() ? VOID : myHomeRegistry.parse(last, args);
					}
					return condition ? myHomeRegistry.parse(last.trim(), args) : VOID;
				}
				catch (ClassCastException ex)
				{
					LogProvider.instance.logErr(str.substring(0, index).trim() + " is invalid condition for ternary operator! Condition must be boolean, try to insert \"!= null\" check!", ex);
					return null;
				}
			}
			
			if (str.charAt(index+1) == '?') //??
			{
				Object obj ;
				if ((obj = myHomeRegistry.parse(str.substring(0, index).trim(), args)) != null)
					return obj;
				
				String next = str.substring(index+2);
				if ((next = next.trim()).isEmpty())
					return null;
				return myHomeRegistry.parse(next, args);
			}
		}
		return CONTINUE;
	}
	
	/**
	 * @param str | Source string to search.
	 * @param defaultCountOfConfitions | How many condition operators (tenraryTokens[0]) are expected. Should be 1 in most cases.
	 * @param tenraryTokens | Characters representing parts of ternary operator. Index 0 should be '?' and index 1 should be ':'.
	 * 
	 * @return Return index of else branch in ternary operator expression or -1 if there is no else branch!
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfTernaryElse(CharSequence str, int defaultCountOfConfitions, char... ternaryOperators)
	{
		for (int i = 0, len = str.length(), oldCh = -1, tokenCount = 0; i < len; i++)
		{
			int ch = str.charAt(i);
			if (ch == '\"')
				while (++i < len && str.charAt(i) != '"');
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++i >= len)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + str);
					if ((ch = (str.charAt(i) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++i < len && str.charAt(i) != '"');
				}
			}
			else
			{
				for (char token : ternaryOperators)
					if (ch == token && oldCh != token && (i >= len-1 || str.charAt(i+1) != token))
					{
						defaultCountOfConfitions += token == ternaryOperators[0] ? 1 : -1;
						tokenCount++;
					}
			
				if (defaultCountOfConfitions == 0 && tokenCount > 0)
					return i;
			}
			oldCh = ch;
		}
		return -1;
	}
	
	/**
	 * @param str | String to search.
	 * @param from | Beginning index. Should be 0 in most cases.
	 * @param oneChar | Single character that you want to find.
	 * 
	 * @return Index of oneChar if there is only one in a row!
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfOne(CharSequence str, int from, char oneChar)
	{
		for (int len = str.length(), oldCh = -1; from < len; from++) 
		{
			int ch = str.charAt(from);
			if (ch == '\"')
				while (++from < len && str.charAt(from) != '"');
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++from >= len)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + str);
					if ((ch = (str.charAt(from) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++from < len && str.charAt(from) != '"');
				}
			}
			else if (ch == oneChar && oldCh != oneChar && (from >= len-1 || str.charAt(from+1) != oneChar))
				return from;
			oldCh = ch;
		}
		return -1;
	}
}
