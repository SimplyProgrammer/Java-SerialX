package org.ugp.serialx.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides general utility used across SerialX library, provides string analysis/manipulation helpers.
 * 
 * @author PETO
 * 
 * @since 1.3.8 (split from Utils in 4.0.0)
 */
public class StrUtils {
	private StrUtils() {}
	
	/**
	 * {@link System#lineSeparator()}
	 * 
	 * @since 1.3.9
	 */
	public static final char[] ENDL = System.lineSeparator().toCharArray();
	
	/**
	 * @param ch | String to multiply!
	 * @param times | Count of multiplication!
	 * 
	 * @return Multiplied char, for example <code>multilpy('a', 5)</code> will return "aaaaa";
	 * 
	 * @since 1.3.2
	 */
	public static StringBuilder multilpy(char ch, int times)
	{
		StringBuilder sb = new StringBuilder();
		while (times-- > 0)
			sb.append(ch);
		return sb;
	}
	
	/**
	 * @param str | String to multiply!
	 * @param times | Count of multiplication!
	 * 
	 * @return Multiplied char, for example <code>multilpy("a", 5)</code> will return "aaaaa";
	 * 
	 * @since 1.3.0
	 */
	public static StringBuilder multilpy(CharSequence str, int times)
	{
		StringBuilder sb = new StringBuilder();
		while (times-- > 0)
			sb.append(str);
		return sb;
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters. More than one splitter in row will be take as 1. Each resulting token will be {@link String#trim() trim}<code>med</code>!<br>
	 * Note: Splitting will only occur if splitter is not in object meaning it is not in string nor between '{' or '[' and ']' or '}
	 * 
	 * @since 1.0.0
	 */
	public static String[] splitValues(String s, char... splitter)
	{
		return splitValues(s, 0, 2, splitter);
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param splittingStrategy | <b>If 0</b>, splitting will occur after each splitter! 
	 * 	<b>If 1</b>, string will be splitted after only one splitter, more than one splitters in row will be ignored! 
	 * 	<b>If 2</b>, splitting will occur after any number of splitters, n number of splitters in row will be treated as 1!
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. Each resulting token will be {@link String#trim() trim}<code>med</code>!<br>
	 * Note: Splitting will only occur if splitter is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'
	 * 
	 * @since 1.3.0
	 */
	public static String[] splitValues(String s, int limit, int splittingStrategy, char... splitter)
	{
		return splitValues(s, 0, limit, splittingStrategy, new char[0], splitter);
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param splittingStrategy | <b>If 0</b>, splitting will occur after each splitter! 
	 * 	<b>If 1</b>, string will be splitted after only one splitter, more than one splitters in row will be ignored! 
	 * 	<b>If 2</b>, splitting will occur after any number of splitters, n number of splitters in row will be treated as 1!
	 * @param splitBreaks | When some of these characters is encountered, splitting is terminated for the rest of the string! 
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. Each resulting token will be {@link String#trim() trim}<code>med</code>!<br>
	 * Note: Splitting will only occur if splitter is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'
	 * 
	 * @since 1.3.5
	 */
	public static String[] splitValues(String s, int limit, int splittingStrategy, char[] splitBreaks, char... splitter)
	{
		return splitValues(s, 0, limit, splittingStrategy, splitBreaks, splitter);
	}

	/**
	 * @param s | String to split and check some syntax.
	 * @param i | Index of character to start at. Note that everything before this index will be ignored by other options...
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param splittingStrategy | <b>If 0</b>, splitting will occur after each splitter! 
	 * 	<b>If 1</b>, string will be splitted after only one splitter, more than one splitters in row will be ignored! 
	 * 	<b>If 2</b>, splitting will occur after any number of splitters, n number of splitters in row will be treated as 1!
	 * @param splitBreaks | When some of these characters is encountered (not in object), splitting is terminated for the rest of the string! 
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. Each resulting token will be {@link String#trim() trim}<code>med</code>!<br>
	 * Note: Splitting will only occur if splitter is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'
	 * 
	 * @since 1.3.8
	 */
	public static String[] splitValues(String s, int i, int limit, int splittingStrategy, char[] splitBreaks, char... splitter)
	{
		if (splitter.length <= 0 || limit == 1)
			return new String[] {s};
//		
//		if (isOneOf(s.charAt(0), splitter))
//			return splitValues(" "+s, limit, oneOrMore, splitBreaks, splitter);
		
		List<String> result = new ArrayList<>();
		
		int lastIndex = 0, len = s.length();
		for (int count = 1, oldCh = 0; i < len && (limit <= 0 || count < limit); i++)
		{
			int ch = s.charAt(i);
			if (ch == '"')
			{
				do if (++i >= len)
					throw new IllegalArgumentException("Unclosed or missing quotes in: " + s);
				while (s.charAt(i) != '"');
			}
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++i >= len)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + s);
					if ((ch = (s.charAt(i) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++i < len && s.charAt(i) != '"');
				}
			}
			else if (isOneOf(ch, splitBreaks))
				break;
			else if (isOneOf(ch, splitter) &&
				(splittingStrategy != 1 || ch != oldCh && (i >= len-1 || !isOneOf(s.charAt(i+1), splitter))))
			{	
				String tok = s.substring(lastIndex, i).trim();
				if (splittingStrategy < 2 || result.isEmpty() || !tok.isEmpty())
				{
					result.add(tok);
					lastIndex = i + 1;
					
					count++;
				}
			}
			
			oldCh = ch;
		}

		result.add(s.substring(lastIndex, len).trim());
		return result.toArray(new String[0]);
	}

	/**
	 * @param s | CharSequence to search!
	 * @param oneOf | Characters to find!
	 * 
	 * @return Index of first character found that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}', otherwise -1!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfNotInObj(CharSequence s, char... oneOf)
	{
		return indexOfNotInObj(s, 0, s.length(), -1, true, oneOf);
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param from | The beginning index, where to start the search (should be 0 in most cases).
	 * @param to | Ending index of search (exclusive, should be {@code >= to <= s.length()}).
	 * @param defaultReturn | Index to return by default (usually -1).
	 * @param firstIndex | If true, first index will be returned, if false last index will be returned.
	 * @param oneOf | Characters to find!
	 * 
	 * @return Index of first character found that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}', otherwise -1!
	 * 
	 * @since 1.3.5 (expanded in 1.3.8)
	 */
	public static int indexOfNotInObj(CharSequence s, int from, int to, int defaultReturn, boolean firstIndex, char... oneOf)
	{
		for (; from < to; from++)
		{
			int ch = s.charAt(from);
			if (ch == '"')
				while (++from < to && s.charAt(from) != '"');
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++from >= to)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + s);
					if ((ch = (s.charAt(from) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++from < to && s.charAt(from) != '"');
				}
			}
			else if (isOneOf(ch, oneOf))
			{
				if (firstIndex)
					return from;
				defaultReturn = from;
			}
		}
		return defaultReturn;
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param sequencesToFind | Character sequences to find, index of any of these will be returned accordingly, none of these should contain and object structure!
	 * 
	 * @return Index of first found CharSequence that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfNotInObj(CharSequence s, CharSequence... sequencesToFind)
	{
		return indexOfNotInObj(s, 0, s.length(), -1, true, sequencesToFind);
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param from | The beginning index, where to start the search (should be 0 in most cases).
	 * @param to | Ending index of search (exclusive, should be {@code >= to <= s.length()}).
	 * @param defaultReturn | Index to return by default (usually -1).
	 * @param firstIndex | If true, first index will be returned, if false last index will be returned.
	 * @param sequencesToFind | Character sequences to find, index of any of these will be returned accordingly, none of these should contain and object structure!
	 * 
	 * @return Index of first found CharSequence that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.5 (expanded in 1.3.8)
	 */
	public static int indexOfNotInObj(CharSequence s, int from, int to, int defaultReturn, boolean firstIndex, CharSequence... sequencesToFind)
	{
		if (sequencesToFind.length < 1)
			return defaultReturn;

		for (; from < to; from++)
		{
			int ch = s.charAt(from);
			if (ch == '"')
				while (++from < to && s.charAt(from) != '"');
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++from >= to)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + s);
					if ((ch = (s.charAt(from) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++from < to && s.charAt(from) != '"');
				}
			}
			else
			{
				findMatch: for (int cur = 0, seqsLen = sequencesToFind.length; cur < seqsLen; cur++) 
				{
					CharSequence currentMatch;
					if (ch == (currentMatch = sequencesToFind[cur]).charAt(0))
					{
						int match = 1, lenToFind = currentMatch.length();
						for (int i = from+1; i < to && match < lenToFind; i++, match++)
							if (s.charAt(i) != currentMatch.charAt(match))
								continue findMatch;
						
						if (match == lenToFind)
						{
							defaultReturn = from;
							if (firstIndex)
								return defaultReturn;
						}
					}
				}
			}
		}
		return defaultReturn;
	}
	
	/**
	 * @param str | String to do replacements in!
	 * @param target | Target to replace!
	 * @param replacement | Replacement for target!
	 * 
	 * @return Inserted string after replacing all targets with replacements similar to {@link String#replace(CharSequence, CharSequence)} but faster!
	 * 
	 * @since 1.2.0
	 */
	public static String fastReplace(String str, String target, CharSequence replacement) 
	{
		int targetLength = target.length();
		if (targetLength == 0) 
		    return str;
		
		int i1 = 0, i2 = str.indexOf(target);
		if (i2 < 0) 
		    return str;
		
		int len = str.length();
		StringBuilder sb = new StringBuilder(targetLength > replacement.length() ? len : len * 2);
		do 
		{
		    sb.append(str, i1, i2).append(replacement);
		    i1 = i2 + targetLength;
		    i2 = str.indexOf(target, i1);
		} while (i2 > 0);
		
		return sb.append(str, i1, len).toString();
	}
	
	/**
	 * @param ch | Char to compare!
	 * @param chars | Chars to match!
	 * 
	 * @return True if inserted char (ch) is any of inserted chars!
	 * 
	 * @since 1.3.0 
	 */
	public static boolean isOneOf(int ch, char... chars)
	{
		final int charsLen = chars.length;
		for (int i = 0; i < charsLen; i++)
			if (chars[i] == ch)
				return true;
		return false;
	}
	
	/**
	 * @param str | Char sequence to search.
	 * @param oneOf | Chars to search for!
	 * 
	 * @return {@link String#contains(CharSequence)} for char sequence!
	 * 
	 * @since 1.3.0
	 */
	public static boolean contains(CharSequence str, char... oneOf)
	{
		if (oneOf.length == 1)
		{
			for (int i = 0, len = str.length(); i < len; i++) 
				if (str.charAt(i) == oneOf[0])
					return true;
			return false;
		}
			
		for (int i = 0, len = str.length(); i < len; i++)
			if (isOneOf(str.charAt(i), oneOf))
				return true;
		return false;
	}
	
	/**
	 * @param str | Source string to compare.
	 * @param lowerCaseOther | Other lower-case string to compare with. This must be lower-case in order for this to work! 
	 * @param from | The beginning index, where to start with comprising (inclusive, most likely 0).
	 * @param to | The ending marking index, index where to end the comparing (exclusive, most likely {@code >= to <= str.length()})
	 * 
	 * @return True if str is equal to lowerCaseOther given that str case is ignored and lowerCaseOther is lower-case, otherwise false. Similar to {@link String#equalsIgnoreCase(String)} but more optimal!<br>
	 * Note that this function was designed for non-blank ASCII strings and may not work properly for others...<br>
	 * Also sufficient length of both strings is not checked so adjust from and to accordingly.
	 * 
	 * @since 1.3.8
	 */
	public static boolean equalsLowerCase(CharSequence str, CharSequence lowerCaseOther, int from, int to)
	{
		for (; from < to; from++)
			if ((str.charAt(from) | ' ') != lowerCaseOther.charAt(from))
				return false;
		return true;
	}
	
	/**
	 * @param str | String to display!
	 * @param pos | Position to display!
	 * 
	 * @return String with displayed position by using » or ^ under it!
	 * Use for debugging or error printing!
	 * 
	 * @since 1.3.2
	 */
	public static String showPosInString(CharSequence str, int pos)
	{
		if (pos < 0)
			return str.toString();

		try
		{
			if (contains(str, ENDL))
				return str.subSequence(0, pos) + "»" + str.subSequence(pos, str.length());
			return multilpy(' ', pos).append('^').insert(0, '\n').insert(0, str).toString();
		}
		catch (IndexOutOfBoundsException e)
		{
			return str.toString();
		}
	}
}
