package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.fastReplace;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.ugp.serialx.LogProvider;

/**
 * This converter is capable of converting {@link Number} including all common implementations like {@link Double}, {@link Float}, {@link Integer} and others. They are determine by suffixes like in java!
 * Its case insensitive!
 * <br>
 * <br>
 * Table of sample string <--> object conversions:
 * 	<style>
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
		    <td>1337</td>
		    <td>new Integer(1337)</td>
	  	</tr>
	    <tr>
		    <td>1337L</td>
		    <td>new Long(1337)</td>
		</tr>
		<tr>
		    <td>1337S</td>
		    <td>new Short(1337)</td>
		</tr>
		<tr>
		    <td>137Y</td>
		    <td>new Byte(137)</td>
	  	</tr>
	  	<tr>
		    <td>13.37</td>
		    <td>new Double(13.37)</td>
		</tr>
		<tr>
		    <td>13.37D</td>
		    <td>new Double(13.37)</td>
		</tr>
		<tr>
		    <td>13.37F</td>
		    <td>new Float(13.37)</td>
		</tr>
	</table>
	Table of sample string --> object conversions:
	<table>
		<tr>
		    <td>0xff</td>
		    <td>new Integer(255)</td>
	  	</tr>
	    <tr>
		    <td>0b1111</td>
		    <td>new Integer(15)</td>
		</tr>
	</table>
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class NumberConverter implements DataConverter 
{
	/**
	 * {@link DecimalFormat} to format decimal numbers (double, float) during serialization!<br>
	 * Default {@link DecimalFormat} will round decimal numbers to 3 decimal places (format pattern #.###)!
	 * 
	 * Set this on null and decimal numbers will not be formated! Do this when you need accuracy!
	 * 
	 * @serial 1.1.0 (moved to {@link NumberConverter} since 1.3.0)
	 * 
	 * @deprecated DO NOT USE! Override {@link NumberConverter#format(Object)} and write your format logic there instead!
	 */
	@Deprecated
	public static DecimalFormat decimalFormatter = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.US));
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args) 
	{
		if (arg.length() > 0)
		{
			char ch = arg.charAt(0);
			if (ch == '+' || ch == '-' || ch == '.' || (ch >= '0' && ch <= '9'))
			{
				arg = normFormatNum(arg.toLowerCase());
				ch = arg.charAt(arg.length()-1); //ch = last char
				
				if (ch == '.')
					return CONTINUE;
				if (contains(arg, '.') || (!arg.startsWith("0x") && ch == 'f' || ch == 'd'))
				{
					if (ch == 'f')
						return new Float(fastReplace(arg, "f", ""));
					return new Double(fastReplace(arg, "d", ""));
				}
				 
				try
				{
					// TODO: Use decode method instead of this mess if possible...
					if (ch == 'l')
						return new Long(Long.parseLong(fastReplace(fastReplace(fastReplace(arg, "l", ""), "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
					if (ch == 's')
						return new Short(Short.parseShort(fastReplace(fastReplace(fastReplace(arg, "s", ""), "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
					if (ch == 'y')
						return new Byte(Byte.parseByte(fastReplace(fastReplace(arg, "y", ""), "0b", ""), arg.startsWith("0b") ? 2 : 10));
					return new Integer(Integer.parseInt(fastReplace(fastReplace(arg, "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
				}
				catch (NumberFormatException e)
				{
					if (arg.matches("[0-9.]+"))
						try
						{	
							return new Long(Long.parseLong(fastReplace(fastReplace(fastReplace(arg, "l", ""), "0b", ""), "0x", ""), arg.startsWith("0b") ? 2 : arg.startsWith("0x") ? 16 : 10));
						}
						catch (NumberFormatException e2)
						{
							LogProvider.instance.logErr("Number " + arg + " is too big for its datatype! Try to change its datatype to double (suffix D)!", e2);
							return null;
						}
				}
			}
		}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Number)
		{
			StringBuilder str = new StringBuilder(format((Number) obj));
			if (obj instanceof Double && !contains(str, '.'))
				str.append('D');
			else if (obj instanceof Float)
				str.append('F');
			else if (obj instanceof Long)
				str.append('L');
			else if (obj instanceof Short)
				str.append('S');
			else if (obj instanceof Byte)
				str.append('Y');
			return str;
		}
		return CONTINUE;
	}

	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return new StringBuilder().append("Primitive data type: \"").append(obj).append("\" the ").append(obj.getClass().getSimpleName().toLowerCase()).append(" value!");
	}
	
	/**
	 * @param num | Number to format before converting it to string by this converter!
	 * 
	 * @return Number formated to string!
	 * 
	 * @since 1.3.7
	 */
	public String format(Number num) 
	{
		return num.toString();
	}

	/**
	 * @param num | Number string to format!
	 * 
	 * @return Original string with formated sign and deleted '_'!
	 * 
	 * @since 1.3.0
	 */
	public static String normFormatNum(String num)
	{
		if (num.length() > 2)
			for (boolean minus = num.startsWith("+-") || num.startsWith("-+"); minus || num.startsWith("++") || num.startsWith("--"); minus = num.startsWith("+-") || num.startsWith("-+"))
			{
				num = num.substring(2);
				if (minus)
					num = "-"+num;
			}
		
		return fastReplace(num, "_", "");
	}
}
