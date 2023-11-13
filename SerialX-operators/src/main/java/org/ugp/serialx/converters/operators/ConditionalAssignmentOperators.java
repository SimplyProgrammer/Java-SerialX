package org.ugp.serialx.converters.operators;

import static org.ugp.serialx.Utils.indexOfNotInObj;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Utils.NULL;
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
		if (str.length() > 2 && indexOfNotInObj(str, '?') > -1)
		{
			int index = indexOfOne(str, 0, '?');
			if (index > -1)
			{
				try
				{
					String last = str.substring(index+1), first = str.substring(0, index);
					boolean condition = (boolean) LogicalOperators.toBool(myHomeRegistry.parse(first.trim(), args));
					if ((index = indexOfTernaryElse(last, 1, '?', ':')) > -1)
					{
						first = last.substring(0, index);
						return condition ? myHomeRegistry.parse(first.trim(), args) : (last = last.substring(index+1).trim()).isEmpty() ? VOID : myHomeRegistry.parse(last, args);
					}
					else
						return condition ? myHomeRegistry.parse(last.trim(), args) : VOID;
				}
				catch (ClassCastException ex)
				{
					LogProvider.instance.logErr(str.substring(0, index).trim() + " is invalid condition for ternary operator! Condition must be boolean, try to insert \"!= null\" check!", ex);
					return null;
				}
			}
			
			if ((index = str.indexOf("??")) > -1)
			{
				Object obj = myHomeRegistry.parse(str.substring(0, index).trim(), args);
				if (obj != null && !(obj instanceof NULL))
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
	 * @param str
	 * @param defaultCountOfConfitions
	 * @param tenraryTokens
	 * 
	 * @return
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfTernaryElse(CharSequence str, int defaultCountOfConfitions, char... ternaryOperators)
	{
		for (int i = 0, len = str.length(), oldCh = -1, tokenCount = 0, quote = 0, brackets = 0; i < len; i++) 
		{
			char ch = str.charAt(i);
			if (ch == '\"')
				quote++;
			else if ((ch | ' ') == '{')
				brackets++;
			else if ((ch | ' ') == '}')
				brackets--;
			else if (quote % 2 == 0 && brackets == 0)
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
	 * @param str
	 * @param from
	 * @param oneChar
	 * 
	 * @return
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfOne(CharSequence str, int from, char oneChar)
	{
		for (int len = str.length(), oldCh = -1, quote = 0, brackets = 0; from < len; from++) 
		{
			char ch = str.charAt(from);
			if (ch == '\"')
				quote++;
			else if ((ch | ' ') == '{')
				brackets++;
			else if ((ch | ' ') == '}')
				brackets--;
			else if (quote % 2 == 0 && brackets == 0 && ch == oneChar && oldCh != oneChar && (from >= len-1 || str.charAt(from+1) != oneChar))
				return from;
			oldCh = ch;
		}
		return -1;
	}
}
