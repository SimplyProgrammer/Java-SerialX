package org.ugp.serialx.converters;

import static org.ugp.serialx.utils.Utils.equalsLowerCase;

import java.io.IOException;

/**
 * This converter is capable of converting "nothing" otherwise known as null and {@link DataParser#VOID}.
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
		    <td>null</td>
		    <td>null</td>
	  	</tr>
	  	<tr>
		    <td>void</td>
		    <td>DataParser.VOID</td>
	  	</tr>
	</table>
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class NullConverter implements DataConverter 
{
	@Override
	public Object parse(ParserRegistry registry, String str, Object... args)
	{
		if (str.length() == 4)
		{
			if (equalsLowerCase(str, "null", 0, 4))
				return null;
			if (equalsLowerCase(str, "void", 0, 4))
				return VOID;
		}
		return CONTINUE;
	}

	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj == null)
			return source.append("null");
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return "Null, the nothing!";
	}
}
