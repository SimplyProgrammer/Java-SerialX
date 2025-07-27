package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.Utils.castArray;
import static org.ugp.serialx.Utils.fromAmbiguousArray;
import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.splitValues;

import java.io.IOException;

import org.ugp.serialx.converters.DataConverter;

/**
 * This converter is capable of converting primitive arrays. 
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
		    <td>1 2 3</td>
		    <td>new int[] {1, 2, 3}</td>
	  	</tr>
	    <tr>
		    <td>4 5 "hello!"</td>
		    <td>new Object[] {4, 5, "hello!"}</td>
		</tr>
	  	<tr>
		    <td>"Lorem" "ipsum"</td>
		    <td>new String[] {"Lorem", "ipsum"}</td>
		</tr>
	</table>
 *	<br>
 *	This parser requires one optional parse method arg that is type of boolean at index 1 and it specifies if resolving datatype of a parsed array is required (default true).
 *
 * @author PETO
 * 
 * @since 1.3.0
 */
public class ArrayConverter implements DataConverter
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		int len, startI = 0;
		if ((len = str.length()) != 0 && str.charAt(0) == '@' || (startI = indexOfNotInObj(str, 1, len, 0, true, ' ')) > 0)
		{
			String[] strObjs = tokenize(startI == 0 ? str.substring(1).trim() : str, startI);
			int arrLen, i = 0;
			Object[] arr = new Object[arrLen = strObjs.length];
			
			if (args.length < 2 || !(args[1] instanceof Boolean) || (boolean) args[1])
			{
				Class<?> arrClass = null;
				for (; i < arrLen; i++) 
				{
					Object obj = arr[i] = myHomeRegistry.parse(strObjs[i], args);
					if (obj != null)
						if (arrClass == null)
							arrClass = obj.getClass();
						else if (arrClass != obj.getClass())
							arrClass = Object.class;	
				}
				
				if (arrClass != null && arrClass != Object.class)
					try
					{
						return castArray(arr, arrClass);
					}
					catch (IllegalArgumentException e) 
					{}
				return arr;
			}
			
			for (; i < arrLen; i++) 
				arr[i] = myHomeRegistry.parse(strObjs[i], args);
			return arr;
		}
		return CONTINUE;
	}

	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj != null && myHomeRegistry != null && obj.getClass().isArray())
		{
			int index = args.length > 2 && args[2] instanceof Integer ? (int) args[2] : 0;
			
			if (index <= 0 || myHomeRegistry.indexOf(OperationGroups.class) > -1)
			{
				int tabs;

				if (args.length > 2)
				{
					tabs = args[1] instanceof Integer ? (int) args[1] : 0;
					
					args = args.clone(); //Necessary for preventing this from affecting other objects and arrays...
					args[2] = index + 1;
				}
				else
					tabs = args.length > 1 && args[1] instanceof Integer ? (int) args[1] : 0;
				
				Object[] elms = fromAmbiguousArray(obj);
				final int len = elms.length;
				if (len == 0)
					return source.append('@');
				source.append(index > 0 ? "(@" : "@");
				
				StringBuilder elmStr = new StringBuilder();
				if (args.length > 5 && args[5] instanceof Byte && (byte) args[5] != 0) // Format
				{
					for (int i = 0; i < len; i++)
					{
						if (i != 0 && i % 1000 == 0)
						{
							source.append('\n');
							for (int j = 0; j < tabs+1; j++) 
								source.append('\t');
						}
						else 
							source.append(' ');
						
						elmStr.setLength(0);
						myHomeRegistry.toString(elmStr, elms[i], args);
						char ch = elmStr.charAt(0);
						if ((ch | ' ') == '{')
							source.append('(').append(elmStr).append(')');
						else
							source.append(elmStr);
					}
				}
				else
				{
					for (int i = 0; i < len; i++) 
					{
						source.append(' ');
						
						elmStr.setLength(0);
						myHomeRegistry.toString(elmStr, elms[i], args);
						char ch = elmStr.charAt(0);
						if ((ch | ' ') == '{')
							source.append('(').append(elmStr).append(')');
						else
							source.append(elmStr);
					}
				}
					
				return index > 0 ? source.append(')') : source;
			}
		}
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object objToDescribe, Object... argsUsedConvert) 
	{
		return "Primitive array " + objToDescribe + " converted by " + getClass().getName();
	}
	
	/**
	 * @param str | String to tokenize!
	 * @param firstSplitterI | Index of first expected splitter char (space).
	 * 
	 * @return String splitted according to defined rules!
	 * 
	 * @since 1.3.2
	 */
	public String[] tokenize(String str, int firstSplitterI)
	{
		return str.isEmpty() ? new String[0] : splitValues(str, firstSplitterI, 0, 2, new char[0], ' ');
	}
}