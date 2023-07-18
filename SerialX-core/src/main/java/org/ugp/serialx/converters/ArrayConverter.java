package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.ToClasses;
import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.splitValues;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.ugp.serialx.converters.imports.ImportsProvider;

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
		if (indexOfNotInObj(str, ' ') > 0)
		{
			boolean findArrType = true;
			if (args.length > 1 && args[1] instanceof Boolean)
				findArrType = (boolean) args[1];
			
			String[] strObjs = tokenize(str);
			int len = strObjs.length;
			Object[] objs = new Object[len];
			
			Class<?> arrClass = null;
			for (int i = 0; i < len; i++) 
			{
				Object obj = objs[i] = myHomeRegistry.parse(strObjs[i], args);
				if (obj != null)
					if (arrClass == null)
						arrClass = obj.getClass();
					else if (arrClass != obj.getClass())
						arrClass = Object.class;	
			}
			
			if (findArrType && arrClass != null && !arrClass.equals(Object.class))
				try
				{
					return castArray(objs, arrClass);
				}
				catch (IllegalArgumentException e) 
				{}
			return objs;
		}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj != null && myHomeRegistry != null && obj.getClass().isArray())
		{
			int tabs = 0, index = 0;
			if (args.length > 2 && args[2] instanceof Integer)
				index = (int) args[2];
			
			if (index <= 0 || myHomeRegistry.indexOf(OperationGroups.class) > -1)
			{
				if (args.length > 1 && args[1] instanceof Integer)
					tabs = (int) args[1];

				if (args.length > 2)
					args[2] = index + 1;
				
				Object[] elms = fromAmbiguous(obj);
				StringBuilder sb = new StringBuilder();
				for (int i = 0, length = elms.length, sizeEndl = 10000; i < length; i++) 
				{
					CharSequence str = myHomeRegistry.toString(elms[i], args);
					char ch = str.charAt(0);
					if (ch == '{' || ch == '[')
						sb.append("("+str+")");
					else
						sb.append(str);
						
					if (i < length-1)
						if (sb.length() > sizeEndl)
						{
							sb.append('\n'); 
							for (int j = 0; j < tabs+1; j++) 
								sb.append('\t');
							sizeEndl += 10000;
						}
						else 
							sb.append(' ');
				}
				return index > 0 ? sb.insert(0, '(').append(')') : sb;
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
	 * 
	 * @return String splitted according to defined rules!
	 * 
	 * @since 1.3.2
	 */
	public String[] tokenize(String str)
	{
		return splitValues(str, ' ');
	}
	
	/**
	 * @param sourceArray | Array to cast!
	 * @param toType | Type to cast array in to!
	 * 
	 * @return Array object casted in to required type!
	 * 
	 * @since 1.3.2
	 */
	public static Object castArray(Object[] sourceArray, Class<?> toType)
	{
		int len = sourceArray.length;
		Object arr = Array.newInstance(ToClasses(toType)[0], len);
		for (int i = 0; i < len; i++) 
			Array.set(arr, i, sourceArray[i]);
		return arr;
	}
	
	/**
	 * @param arr1 | Object one that might be array!
	 * @param arr2 | Object two that might be array!
	 * 
	 * @return New array consisting of array 1 and array 2!
	 * 
	 * @throws IllegalArgumentException if object one is not an array!
	 * 
	 * @since 1.3.2
	 */
	public static Object[] mergeArrays(Object arr1, Object arr2) 
	{
		Object[] array1 = fromAmbiguous(arr1), array2 = arr2.getClass().isArray() ? fromAmbiguous(arr2) : new Object[] {arr2};
		Object[] result = Arrays.copyOf(array1, array1.length + array2.length);
	    System.arraycopy(array2, 0, result, array1.length, array2.length);
	    return result;
	}
	
	/**
	 * @param array | Object that might be array!
	 * 
	 * @return Object transformed in to primitive array! If array is already an instance of primitive array then it will be simply returned!
	 * 
	 * @throws IllegalArgumentException if the specified object is not an array!
	 * 
	 * @since 1.3.2
	 */
	public static Object[] fromAmbiguous(Object array)
	{
		if (array instanceof Object[])
			return (Object[]) array;
		
		int len = Array.getLength(array);
		Object[] arr = new Object[len];
		for (int i = 0; i < len; i++) 
			arr[i] = Array.get(array, i);
		return arr;
	}
}