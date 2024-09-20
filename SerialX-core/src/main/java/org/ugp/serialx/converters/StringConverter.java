package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.indexOfNotInObj;

import java.util.Map;

import org.ugp.serialx.Registry;

/**
 * This converter is capable of converting {@link String}.
 * Its case sensitive!
 * <br>
 * <br>
 * Table of sample string <--> object conversions:
	<style>
		table, th, td 
		{
		  border: 1px solid gray;
		}
	</style>
	<table>
		<tr>
		    <th>String</th>
		    <th>Object</th> 
		</tr>
		<tr>
		    <td>"Hello world!"</td>
		    <td>new String("Hello world!")</td>
	  	</tr>
	</table>
 * If you enter string in ${yourString}, "yourString" will be returned by {@link StringConverter#toString(Registry, Object, Object...)} according to {@link StringConverter#CodeInsertion(String)}!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class StringConverter implements DataConverter
{
	/**
	 * Set this on true and {@link String} will be serialized normally, for instance <code>"Hello world!"</code> and not using protocols or java Base64! <br>
	 * Setting this on false will also make Strings unreadable for normal people!
	 * 
	 * @since 1.2.0 (moved to {@link StringConverter} since 1.3.0)
	 */
	protected boolean serializeStringNormally = true;
	
	protected Map<String, String> parsingCache;
	
	@Override
	public String parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{	
		String result;
		int len;
		if ((len = str.length()) > 1 && str.charAt(0) == '\"' && str.charAt(--len) == '\"' && indexOfNotInObj(str, ' ') == -1)
		{
			if (parsingCache != null)
			{
				if ((result = parsingCache.get(str)) != null)
					return result;
				parsingCache.put(str, result = str.substring(1, len));
				return result;
			}
			return str.substring(1, len);
		}
		return CONTINUE;
	}
	
	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object arg, Object... args) 
	{
		if (arg instanceof String)
		{
			String str = arg.toString();
			if (str.startsWith("${") && str.endsWith("}"))
			{
				str = str.substring(2, str.length()-1);
				if (str.contains("::") && indexOfNotInObj(str, ' ') > -1)
					str = "{"+str+"}";
				return str;
			}

			if (serializeStringNormally)
			{
				if (contains(str, '\"', '\n', '\r'))
					return CONTINUE;
				return "\""+str+"\"";
			}
		}
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		String str = obj.toString();
		boolean hasComment = false;
		if (str.startsWith("${") && str.endsWith("}") && !(hasComment = str.substring(2).contains("//")))
			return "Manually inserted code!";
		if (!hasComment)
			return new StringBuilder().append("Object of ").append(obj.getClass().getName()).append(": \"").append(obj.toString()).append("\"!");
		return "";
	}
	
	/**
	 * @param cache | Instance of {@link Map}, preferably {@link HashMap}, that will be used as cache for parsed strings where keys will be strings with " and values will be string without them. This cache will then be prioritized over creating a new instance of string during parsing, similarly to Java's string pool. Doing this can save you some memory with minimal performance overhead!<br>
	 * Setting this to null will disable the parsing result caching by this {@link StringConverter} as it is by default.<br>
	 * Recommended: Enable this when parsing a lot of strings that are the same, otherwise this will not have a big impact.<br>
	 * Rule of thumb, is that this cache should be modified only by this converter however adding some pre-cached entries is possible but should be performed with caution!
	 * 
	 * @since 1.3.8
	 */
	public void setParsingCache(Map<String, String> cache)
	{
		parsingCache = cache;
	}
	
	/**
	 * @return Instance of {@link Map}, preferably {@link HashMap}, that will be used as cache for parsed strings where keys will be strings with " and values will be string without them. This cache will then be prioritized over creating a new instance of string during parsing, similarly to Java's string pool.<br>
	 * Null will be returned if caching is disabled, which is by default...<br>
	 * Note: Rule of thumb, is that this cache should be modified only by this converter however adding some pre-cached entries is possible but should be performed with caution!
	 * 
	 * @since 1.3.8
	 */
	public Map<String, String> getParsingCache()
	{
		return parsingCache;
	}
	
	/**
	 * @return Will return value of {@link StringConverter#serializeStringNormally}!
	 * 
	 * @since 1.3.8 (it was static before = not good)
	 */
	public boolean isSerializeStringNormally() 
	{
		return serializeStringNormally;
	}

	/**
	 * @param serializeStringNormally | Set value of {@link StringConverter#serializeStringNormally}!
	 * 
	 * @since 1.3.8 (it was static before = not good)
	 */
	public void setSerializeStringNormally(boolean serializeStringNormally) 
	{
		this.serializeStringNormally = serializeStringNormally;
	}

	/**
	 * @param obj | Object to stringify directly.
	 * 
	 * @return "${" + obj + "}" - if this is be inserted into {@link StringConverter#toString(Registry, Object, Object...))}, it will be returned without ${ and }!
	 *
	 * @since 1.3.5
	 */
	public static String DirectCode(Object obj)
	{
		return "${" + obj + "}";
	}
}