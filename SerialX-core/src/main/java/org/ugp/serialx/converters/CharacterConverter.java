package org.ugp.serialx.converters;

import java.io.IOException;

/**
 * This converter is capable of converting {@link Character}.
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
		    <td>'a'</td>
		    <td>new Character('a')</td>
	  	</tr>
	    <tr>
		    <td>'35'</td>
		    <td>new Character('#')</td>
		</tr>
	</table>
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class CharacterConverter implements DataConverter 
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		int len;
		if ((len = str.length()) > 1 && str.charAt(0) == '\'' && str.charAt(--len) == '\'')
			try
			{
				if (len == 1) // str == "''"
					return ' ';
				return (char) Integer.parseInt(str.substring(1, len));
			}
			catch (Exception e) 
			{
				return str.charAt(1);
			}
		return CONTINUE;
	}

	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj instanceof Character)
			return source.append("'"+(int) (char) obj+"'");
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return new StringBuilder().append("Primitive data type: \"").append(obj).append("\" the ").append(obj.getClass().getSimpleName().toLowerCase()).append(" value!");
	}
}
