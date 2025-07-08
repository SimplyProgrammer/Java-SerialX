package org.ugp.serialx.converters;

import static java.lang.Boolean.*;
import static org.ugp.serialx.Utils.equalsLowerCase;

/**
 * This converter is capable of converting {@link String}. 
 * Its case insensitive!
 * <br>
 * <br>
 * Table of all string <--> object conversions:
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
		    <td>true</td>
		    <td>Boolean.TRUE</td>
	  	</tr>
	    <tr>
		    <td>t</td>
		    <td>Boolean.TRUE</td>
		</tr>
	  	<tr>
		    <td>false</td>
		    <td>Boolean.FALSE</td>
		</tr>
		<tr>
		    <td>f</td>
		    <td>Boolean.FALSE</td>
		</tr>
	</table>
	
 * @author PETO
 * 
 * @since 1.3.0
 */
public class BooleanConverter implements DataConverter
{
	protected boolean shorten;
	
	public BooleanConverter() 
	{
		this(true);
	}
	
	/**
	 * @param shorten | If true, shortened format (T/F) will be serialized. If false, true/false will be serialized...
	 * 
	 * @since 1.3.5
	 */
	public BooleanConverter(boolean shorten)
	{
		setShorten(shorten);
	}
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args)
	{
		int len, ch0;
		if ((len = str.length()) > 0)
		{
			if ((ch0 = str.charAt(0) | ' ') == 't' && (len == 1 || len == 4 && equalsLowerCase(str, "true", 1, 4)))
				return TRUE;
			if (ch0 == 'f' && (len == 1 || len == 5 && equalsLowerCase(str, "false", 1, 5)))
				return FALSE;
		}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Boolean)
			return isShorten() ? (boolean) obj ? "T" : "F" : (boolean) obj ? "true" : "false";
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return new StringBuilder().append("Primitive data type: \"").append(obj).append("\" the ").append(obj.getClass().getSimpleName().toLowerCase()).append(" value!");
	}

	/**
	 * @return If true, shortened format (T/F) will be serialized. If false, true/false will be serialized...
	 * 
	 * @since 1.3.5
	 */
	public boolean isShorten() 
	{
		return shorten;
	}

	/**
	 * @param shorten | If true, shortened format (T/F) will be serialized. If false, true/false will be serialized...
	 * 
	 * @since 1.3.5
	 */
	public void setShorten(boolean shorten) 
	{
		this.shorten = shorten;
	}
}
