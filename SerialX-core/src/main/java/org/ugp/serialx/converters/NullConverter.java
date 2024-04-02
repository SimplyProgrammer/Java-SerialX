package org.ugp.serialx.converters;

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
		    <td>null (object)</td>
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
		if (str.equalsIgnoreCase("null"))
			return null;
		if (str.equalsIgnoreCase("void"))
			return VOID;
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj == null)
			return "null";
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return "Null, the nothing!";
	}
}
