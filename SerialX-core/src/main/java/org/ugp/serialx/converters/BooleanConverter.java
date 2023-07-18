package org.ugp.serialx.converters;

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
		    <td>new Boolean(true)</td>
	  	</tr>
	    <tr>
		    <td>t</td>
		    <td>new Boolean(true)</td>
		</tr>
	  	<tr>
		    <td>false</td>
		    <td>new Boolean(false)</td>
		</tr>
		<tr>
		    <td>f</td>
		    <td>new Boolean(false)</td>
		</tr>
	</table>
	
 * @author PETO
 * 
 * @since 1.3.0
 */
public class BooleanConverter implements DataConverter
{
	public boolean shorten;
	
	public BooleanConverter() 
	{
		this(true);
	}
	
	public BooleanConverter(boolean shorten) 
	{
		setShorten(shorten);
	}
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args)
	{
		if (arg.equalsIgnoreCase("T") || arg.equalsIgnoreCase("true"))
			return new Boolean(true);
		if (arg.equalsIgnoreCase("F") || arg.equalsIgnoreCase("false"))
			return new Boolean(false);
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

	public boolean isShorten() 
	{
		return shorten;
	}

	public void setShorten(boolean shorten) 
	{
		this.shorten = shorten;
	}
}
