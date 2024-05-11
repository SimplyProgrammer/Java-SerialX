package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.fastReplace;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
	    <tr>
		    <td>0112</td>
		    <td>new Integer(74)</td>
		</tr>
	    <tr>
		    <td>0112</td>
		    <td>new Integer(74)</td>
		</tr>
	    <tr>
		    <td>10e2</td>
		    <td>new Integer(10e2)</td>
		</tr>
	    <tr>
		    <td>10e2</td>
		    <td>new Integer(10e2)</td>
		</tr>
	    <tr>
		    <td>.1e2</td>
		    <td>new Double(0.1e2)</td>
		</tr>
	</table>
 *
 * @see NumberConverter#numberOf(CharSequence, char, int, int, int)
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
		int len;
		if ((len = arg.length()) > 0)
		{
			char ch0 = arg.charAt(0);
			if (ch0 == '+' || ch0 == '-' || ch0 == '.' || (ch0 >= '0' && ch0 <= '9'))
			{
				Number num;
				if ((num = numberOf(arg, ch0, --len, 10, 0)) != null)
					return num;
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
	 * @param str | Source char sequence with number to parse.
	 * @param ch0 | Should be <code>str.charAt(0)</code>. This is to ensure that string is not null or empty and also for possible optimizations.
	 * @param end | Index of where to end with parsing. If whole string is meant to be parsed, then <code>str.length()-1</code>, should not be greater than that!
	 * @param base | Base of the parsed number. Theoretically can be anything but usually should be 2, 8, 10 or 16... Note that base will be overridden by suffixes <code>#</code>. for 16, <code>0x</code> for 16, <code>0b</code> for 2 or <code>0</code> for 8 (only if not followed by <code>.</code>).
	 * @param type | Preferred datatype of of the number represented by suffixes 'S' for {@link Short}, 'Y' for {@link Byte}, 'L' for {@link Long}, 'D' for {@link Double}, 'F' for {@link Float}. Other stands for {@link Integer}.<br> 
	 * Note that floating point numberer will be treated as {@link Double} if no suffix is present by default. Also numbers in E-notation format with negative exponents can be converted to {@link Double}. Further more, integers will be auto-converted to {@link Long} if overflow should occur!<br>
	 * Important thing to know is that this argument will be overridden by suffix from str if present!
	 * 
	 * @return {@link Number} parsed from str with rules specified above. This function was designed to act as more optimized merger of {@link Byte#valueOf(String, int)}, {@link Short#valueOf(String, int)}, {@link Integer#valueOf(String, int)}, {@link Long#valueOf(String, int)} and {@link Float#valueOf(String)}, {@link Double#valueOf(String)} all encapsulated in 1 universal function.<br>
	 * Note: This function will not check for incorrect number formats in order to save performance. Only incorrect format is when inserted string contains space, in this case it will return null!
	 * 
	 * @since 1.3.7
	 */
	public static Number numberOf(CharSequence str, char ch0, int end, int base, int type)
	{
		int start = 0;

		if (ch0 == '#') //Determine base
		{
			base = 16;
			start++;
		}
		else if (ch0 == '0' && end > 0)
		{
			int ch1 = str.charAt(1) | ' ';
			if (ch1 == 'b')
			{
				base = 2;
				start++;
			}
			else if (ch1 == 'x')
			{
				base = 16;
				start++;
			}
			else if (ch1 != '.')
				base = 8;

			start++;
		}
		
		double result = 0, baseCof = 1, exponent = 1;
		int chEnd = str.charAt(end--) | ' '; //Determine data type
		if (base == 10 ? chEnd >= 'd' : chEnd >= 'l')
			type = chEnd;
		else if (chEnd == '.')
			type = 'd';
		else
		{
			result = chEnd > '9' ? chEnd - 'a' + 10 : chEnd - '0';
			baseCof = base;
		}

		for (int ch; end >= start; end--) //Parsing
		{
			if ((ch = str.charAt(end)) == '-') // Neg
				result = -result;
			else if (ch == '.') //Decimal
			{
				result /= baseCof;
				baseCof = 1;
				if (type == 0)
					type = 'd';
			}
			else if ((ch |= ' ') == 'e' && base == 10) //Handle E-notation
			{
				if ((exponent = Math.pow(base, result)) < 1 && type == 0)
					type = 'd';
				result = 0;
				baseCof = 1;
			}
			else if (ch == ' ') //Not valid
				return null;
			else if (ch != 127 && ch != '+')
			{
				result += (ch > '9' ? ch - 'a' + 10 : ch - '0') * baseCof;
				baseCof *= base;
			}
		}

		result *= exponent;

		if (type == 'd')
			return result;
		if (type == 'f')
			return (float) result;
		if (type == 'l' || result > 0x7fffffff || result < 0x80000000)
			return (long) result;
		if (type == 's')
			return (short) result;
		if (type == 'y')
			return (byte) result;
		return (int) result;
	}
	
	/**
	 * @deprecated THIS IS NO LONGER NECESSARY BECAUSE BECAUSE {@link NumberConverter#numberOf(CharSequence, char, int, int, int)} CAN DO THE SAME THING MUCH FASTET!
	 * 
	 * @param num | Number string to format!
	 * 
	 * @return Original string with formated sign and deleted '_'!
	 * 
	 * @since 1.3.0
	 */
	@Deprecated
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
