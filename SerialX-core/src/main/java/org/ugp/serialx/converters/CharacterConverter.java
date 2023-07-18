package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.fastReplace;

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
		if (str.length() > 1 && str.charAt(0) == '\'' && str.charAt(str.length()-1) == '\'')
			try
			{
				if (str.equals("''")) // TODO: str.length() == 2 + mby cache len
					return new Character(' ');
				return new Character((char) Integer.parseInt(str = fastReplace(str, "'", "")));
			}
			catch (Exception e) 
			{
				return new Character(str.charAt(0));
			}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Character)
			return "'"+(int) (char) obj+"'";
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return new StringBuilder().append("Primitive data type: \"").append(obj).append("\" the ").append(obj.getClass().getSimpleName().toLowerCase()).append(" value!");
	}
}
