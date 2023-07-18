package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.indexOfNotInObj;

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
	public static boolean serializeStringNormally = true; 
	
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
			else if (serializeStringNormally)
			{
				if (contains(str, '\"', '\n', '\r'))
					return CONTINUE;
				else
					return "\""+str+"\"";
			}
		}
		return CONTINUE;
	}
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{	
		if (str.length() > 1 && str.charAt(0) == '\"' && str.charAt(str.length()-1) == '\"' && indexOfNotInObj(str, ' ') == -1)
			return str.substring(1, str.length() - 1);
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
