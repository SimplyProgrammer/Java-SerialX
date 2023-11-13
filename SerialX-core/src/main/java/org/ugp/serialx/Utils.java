package org.ugp.serialx;

import static org.ugp.serialx.converters.DataParser.VOID;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.protocols.SerializationProtocol;

/**
 * Provides general utility used across SerialX library, mostly string analysis/manipulation and reflection.
 * 
 * @author PETO
 * 
 * @since 1.3.7
 */
public final class Utils {
	private Utils() {}
	
	/**
	 * @param f | Source file.
	 * 
	 * @return All lines from source file as string.
	 * @throws IOException 
	 * 
	 * @since 1.1.5
	 */
	public static String LoadFileToString(File f) throws IOException
	{
		return LoadFileToString(f, 1);
	}
	
	/**
	 * @param f | Source file.
	 * @param endlMode | 0 = no line brakes, 1 = always line brakes, 2 = line break only when contains with "//"! <br>
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link Serializer#LoadFileToString(File)} which is doing this automatically!
	 * 
	 * @return Content of file as string.
	 * @throws IOException 
	 * 
	 * @since 1.2.0
	 */
	public static String LoadFileToString(File f, int endlMode) throws IOException
	{
		return StreamToString(new FileReader(f), endlMode);
	}
	
	/**
	 * @param input | Input stream to read to string!
	 * @param endlMode | 0 = no line brakes, 1 = always line brakes, 2 = line break only when contains with "//"! <br>
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link Serializer#LoadFileToString(File)} which is doing this automatically!
	 * 
	 * @return Reader converted to string form!
	 * 
	 * @throws IOException
	 * 
	 * @since 1.3.5
	 */
	public static String StreamToString(InputStream input, int endlMode) throws IOException
	{
		return StreamToString(new InputStreamReader(input), endlMode);
	}
	
	/**
	 * @param input | Input reader!
	 * @param endlMode | 0 = no line brakes, 1 = always line brakes, 2 = line break only when contains with "//"! <br>
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link Serializer#LoadFileToString(File)} which is doing this automatically!
	 * 
	 * @return Reader converted to string form!
	 * 
	 * @throws IOException
	 * 
	 * @since 1.3.2
	 */
	public static String StreamToString(Reader input, int endlMode) throws IOException
	{
		String l;
		StringBuilder sb = new StringBuilder();

		BufferedReader r = new BufferedReader(input);
		while ((l = r.readLine()) != null)
		{
			sb.append(l);
			if (endlMode == 1 || (endlMode > 1 && l.contains("//")))
				sb.append("\n");
		}
		r.close();
		return sb.toString();
	}
	
	/* Reflect */
	
	/**
	 * @param cls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.2.2
	 */
	public static Object InvokeStaticFunc(Class<?> cls, String name, Object... args) throws InvocationTargetException
	{
		return InvokeFunc(null, cls, name, args);
	}
	
	/**
	 * @param obj | The object the underlying method is invoked from!
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.3.5
	 */
	public static Object InvokeFunc(Object obj, String name, Object... args) throws InvocationTargetException
	{
		return InvokeFunc(obj, obj.getClass(), name, args);
	}
	
	/**
	 * @param obj | The object the underlying method is invoked from!
	 * @param cls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.3.5
	 */
	public static Object InvokeFunc(Object obj, Class<?> objCls, String name, Object... args) throws InvocationTargetException
	{
		Object result = InvokeFunc(obj, objCls, name, ToClasses(args), args);
		if (result != null)
			return result;
		result = InvokeFunc(obj, objCls, name, ToClasses(false, args), args);
		if (result == null)
			LogProvider.instance.logErr("Unable to call function \"" + name + "\" because inserted arguments " + Arrays.asList(args) + " cannot be applied or function does not exist in required class!", null);
		return result;
	}
	
	/**
	 * @param obj | The object the underlying method is invoked from!
	 * @param cls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param argClasses | Classes of args.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.3.5
	 */
	public static Object InvokeFunc(Object obj, Class<?> objCls, String name, Class<?>[] argClasses, Object... args) throws InvocationTargetException
	{
		try 
		{
			Method method = objCls.getMethod(name, argClasses);
			Object resualt = method.invoke(obj, args);
			return method.getReturnType().equals(void.class) ? VOID : resualt;
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException find) 
		{
			for (Method method : objCls.getMethods()) 
				if (method.getName().equals(name))
					try
					{
						Object resualt = method.invoke(obj, args);
						return method.getReturnType().equals(void.class) ? VOID : resualt;
					}
					catch (IllegalArgumentException e) 
					{}
					catch (SecurityException | IllegalAccessException ex)
					{
						ex.printStackTrace();
					}
		}
		return null;
	}
	
	
	//Syntactical analyzes and fast string utility:
	
	/**
	 * @param obj | Object to clone.
	 * 
	 * @return Cloned object using {@link DataParser}, {@link DataConverter} and {@link SerializationProtocol} or the same object as inserted one if cloning is not possible, for instance when protocol was not found and object is not instance of {@link Cloneable}.
	 * This clone function will always prioritized the Protocol variation, regular cloning is used only when there is no protocol registered or exception occurs. <br>
	 * Note: If there are protocols to serialize inserted object and all its sub-objects and variables then this clone will be absolute deep copy, meaning that making any changes to this cloned object or to its variables will not affect original one in any way! 
	 * But keep in mind that this clone is absolute hoverer, based on protocols used, it does not need to be an 100% copy!
	 * 
	 * @since 1.2.2
	 */
	public static <T> T Clone(T obj)
	{
		return Clone(obj, DataParser.REGISTRY, new Object[] {}, new Scope());
	}
	
	/**
	 * @param obj | Object to clone.
	 * @param 
	 * @param converterArgs | Argument for {@link DataConverter#objToString(Registry, Object, Object...)}!
	 * @param parserArgs | Arguments for {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)}!
	 * 
	 * @return Cloned object using {@link DataParser}, {@link DataConverter} and {@link SerializationProtocol} or the same object as inserted one if cloning is not possible, for instance when protocol was not found and object is not instance of {@link Cloneable}.
	 * This clone function will always prioritized the Protocol variation, regular cloning is used only when there is no protocol registered or exception occurs. <br>
	 * Note: If there are protocols to serialize inserted object and all its sub-objects and variables then this clone will be absolute deep copy, meaning that making any changes to this cloned object or to its variables will not affect original one in any way! 
	 * But keep in mind that this clone is absolute hoverer, based on protocols used, it does not need to be an 100% copy!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T Clone(T obj, Registry<DataParser> parsersToUse, Object[] converterArgs, Object... parserArgs)
	{
		if (obj == null) 
			return obj;
		else if (obj.getClass() == Byte.class)
			return (T) new Byte((byte) obj);
		else if (obj.getClass() == Short.class)
			return (T) new Short((short) obj);
		else if (obj.getClass() == Integer.class)
			return (T) new Integer((int) obj);
		else if (obj.getClass() == Long.class)
			return (T) new Long((long) obj);
		else if (obj.getClass() == Float.class)
			return (T) new Float((float) obj);
		else if (obj.getClass() == Double.class)
			return (T) new Double((double) obj);
		else if (obj.getClass() == Character.class)
			return (T) new Character((char) obj);
		else if (obj.getClass() == Boolean.class)
			return (T) new Boolean((boolean) obj);
		else if (obj.getClass() == String.class)
			return (T) new String((String) obj);
		else
		{
			ParserRegistry parsers = parsersToUse instanceof ParserRegistry ? (ParserRegistry) parsersToUse : new ParserRegistry(parsersToUse);
			
			Object cln = NULL.toOopNull(parsers.parse(parsers.toString(obj, converterArgs).toString(), parserArgs));
			if (cln != null && cln != VOID)
				return (T) cln;
			
			if (obj instanceof Cloneable)
			{
				try 
				{
					Method method = Object.class.getDeclaredMethod("clone");
					method.setAccessible(true);
					return (T) method.invoke(obj);
				} 
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}
			LogProvider.instance.logErr("Unable to clone " + obj.getClass() + ": " + obj, null);
			return obj;
		}
	}
	
	/**
	 * @param cls | Class to instantiate.
	 * 
	 * @return New blank instance of required class created by calling shortest public constructor with default values!<br>
	 * Note: Do not use this when your class contains final fields!
	 * 
	 * @throws NoSuchMethodException if there is no public constructor!
	 * @throws InvocationTargetException if called constructor throws and exception!
	 * 
	 * @since 1.2.2
	 */
	public static <T> T Instantiate(Class<T> cls) throws NoSuchMethodException, InvocationTargetException
	{
		return Instantiate(cls, true);
	}
	
	/**
	 * @param cls | Class to instantiate.
	 * @param publicOnly | If true, only public constructors will be used to create the object!
	 * 
	 * @return New blank instance of required class created by calling shortest constructor with default values!<br>
	 * Note: Do not use this when your class contains final fields!
	 * 
	 * @throws NoSuchMethodException if there is no public constructor!
	 * @throws InvocationTargetException if called constructor throws and exception!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T Instantiate(Class<T> cls, boolean publicOnly) throws NoSuchMethodException, InvocationTargetException
	{
		try
		{
			Constructor<T> cons = publicOnly ? cls.getConstructor() : cls.getDeclaredConstructor();
			if (!publicOnly)
				cons.setAccessible(true);
			return cons.newInstance();
		}
		catch (Exception e) 
		{
			try
			{
				Constructor<?>[] cnstrs = publicOnly ? cls.getConstructors() : cls.getDeclaredConstructors();
				if (cnstrs.length <= 0)
					throw new NoSuchMethodException("No public constructors in class " + cls.getName() + "!");
				
				for (int i = 1; i < cnstrs.length; i++) 
				{
					if (!publicOnly)
						cnstrs[0].setAccessible(true);
					if (cnstrs[i].getParameterCount() < cnstrs[0].getParameterCount())
						cnstrs[0] = cnstrs[i];
				}
				
				Object[] args = new Object[cnstrs[0].getParameterCount()];
				Class<?>[] argTypes = cnstrs[0].getParameterTypes();
				for (int i = 0; i < cnstrs[0].getParameterCount(); i++) 
				{
					if (argTypes[i] == byte.class)
						args[i] = (byte) 0;
					else if (argTypes[i] == short.class)
						args[i] = (short) 0;
					else if (argTypes[i] == int.class)
						args[i] = 0;
					else if (argTypes[i] == long.class)
						args[i] = 0l;
					else if ( argTypes[i] == float.class)
						args[i] = 0.0f;
					else if (argTypes[i] == double.class)
						args[i] = 0.0;
					else if (argTypes[i] == char.class)
						args[i] = (char) 0;
					else if (argTypes[i] == boolean.class)
						args[i] = false;
					else if (argTypes[i] == String.class)
						args[i] = "";
					else
						args[i] = null;
				}
				return (T) cnstrs[0].newInstance(args);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e2) 
			{
				e2.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * @param objs | Array of objects.
	 * 
	 * @return Array of inserted objects class types. Wrapper types of primitive values will be converted to primitive types! For instance: Integer.class -> int.class
	 * 
	 * @since 1.2.2
	 */
	public static Class<?>[] ToClasses(Object... objs)
	{
		return ToClasses(true, objs);
	}
	
	/**
	 * @param objs | Array of objects.
	 * 
	 * @return Array of inserted objects class types. Wrapper types of primitive values will be converted to primitive types! For instance: Integer.class -> int.class
	 * 
	 * @since 1.3.5
	 */
	public static Class<?>[] ToClasses(boolean unwrapp, Object... objs)
	{
		Class<?>[] classes = new Class<?>[objs.length];
		if (unwrapp)
		{
			for (int i = 0; i < classes.length; i++) 
			{
				if (objs[i] == null)
					classes[i] = Object.class;
				else if (objs[i].getClass() == Byte.class || objs[i] == Byte.class)
					classes[i] = byte.class;
				else if (objs[i].getClass() == Short.class || objs[i] == Short.class)
					classes[i] = short.class;
				else if (objs[i].getClass() == Integer.class || objs[i] == Integer.class)
					classes[i] = int.class;
				else if (objs[i].getClass() == Long.class || objs[i] == Long.class)
					classes[i] = long.class;
				else if (objs[i].getClass() == Float.class || objs[i] == Float.class)
					classes[i] = float.class;
				else if (objs[i].getClass() == Double.class || objs[i] == Double.class)
					classes[i] = double.class;
				else if (objs[i].getClass() == Character.class || objs[i] == Character.class)
					classes[i] = char.class;
				else if (objs[i].getClass() == Boolean.class || objs[i] == Boolean.class)
					classes[i] = boolean.class;
				else if (objs[i] instanceof Class)
					classes[i] = (Class<?>) objs[i];
				else
					classes[i] = objs[i].getClass();
			}
			
			return classes;
		}
		
		for (int i = 0; i < classes.length; i++) 
			classes[i] = objs[i].getClass();
		return classes;
	}
	
	/* Characters */
	
	/**
	 * @param ch | String to multiply!
	 * @param times | Count of multiplication!
	 * 
	 * @return Multiplied char, for example <code>multilpy('a', 5)</code> will return "aaaaa";
	 * 
	 * @since 1.3.2
	 */
	public static StringBuilder multilpy(char ch, int times)
	{
		StringBuilder sb = new StringBuilder();
		while (times-- > 0)
			sb.append(ch);
		return sb;
	}
	
	/**
	 * @param ch | String to multiply!
	 * @param str | Count of multiplication!
	 * 
	 * @return Multiplied char, for example <code>multilpy("a", 5)</code> will return "aaaaa";
	 * 
	 * @since 1.3.0
	 */
	public static StringBuilder multilpy(CharSequence str, int times)
	{
		StringBuilder sb = new StringBuilder(str);
		while (times-- > 1)
			sb.append(str);
		return sb;
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters. If there is more than one splitter in row, it will be taken as one whole!
	 * 
	 * @since 1.0.0
	 */
	public static String[] splitValues(String s, char... splitter)
	{
		return splitValues(s, 0, true, splitter);
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param oneOrMore | If true, string will be splitted after one or more splitters in row, if false splitting will occur only after single splitter (this is similar to "+" in regex)!
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. If there is more than one splitter in row, it will be taken as one whole!
	 * 
	 * @since 1.3.0
	 */
	public static String[] splitValues(String s, int limit, boolean splitAfterSingleCharOnly, char... splitter)
	{
		return splitValues(s, limit, splitAfterSingleCharOnly, new char[0], splitter);
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param oneOrMore | If true, string will be splitted after one or more splitters in row, if false splitting will occur only after single splitter (this is similar to "+" in regex)!
	 * @param splitBreaks | When some of these characters is encountered, splitting is terminated for the rest of the string! 
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. If there is more than one splitter in row, it will be taken as one whole!
	 * 
	 * @since 1.3.5
	 */
	public static String[] splitValues(String s, int limit, boolean oneOrMore, char[] splitBreaks, char... splitter)
	{
		if (splitter.length <= 0 || limit == 1)
			return new String[] {s};
//		
//		if (isOneOf(s.charAt(0), splitter))
//			return splitValues(" "+s, limit, oneOrMore, splitBreaks, splitter);
		
		List<String> result = new ArrayList<>();

		int brackets = 0, quote = 0, lastIndex = 0;
		for (int i = 0, count = 1, len = s.length(), oldCh = splitter[0]; i < len && (limit <= 0 || count < limit); i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
			
			if (quote % 2 == 0)
			{
				if (isOneOf(ch, splitBreaks))
				{
					brackets = quote = 0;
					break;
				}
				
				if (brackets == 0 && oldCh != ch && isOneOf(ch, splitter) && (oneOrMore || (i >= len-1 || !isOneOf(s.charAt(i+1), splitter))))
				{	
					result.add(s.substring(lastIndex == 0 ? 0 : lastIndex + 1, lastIndex = i).trim());
					count++;
				}
				else if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing opening bracket in: " + s);
				}
			}
			oldCh = ch;
		}

		if (brackets > 0)
			throw new IllegalArgumentException("Unclosed brackets in: " + s);
		else if (quote % 2 != 0)
			throw new IllegalArgumentException("Unclosed or missing quotes in: " + s);
		else
		{
			result.add(s.substring(lastIndex == 0 ? 0 : lastIndex + 1, s.length()).trim());
		}
		
		return result.toArray(new String[0]);
	}

	/**
	 * @param s | CharSequence to search!
	 * @param oneOf | Characters to find!
	 * 
	 * @return Index of first found character that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}', otherwise -1!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfNotInObj(CharSequence s, char... oneOf)
	{
		return indexOfNotInObj(s, true, oneOf);
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param firstIndex | If true, first index will be returned, if false last index will be returned.
	 * @param oneOf | Characters to find!
	 * 
	 * @return Index of first found character that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}', otherwise -1!
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfNotInObj(CharSequence s, boolean firstIndex, char... oneOf)
	{
		int found = -1;
		for (int i = 0, brackets = 0, quote = 0, len = s.length(); i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
	
			if (quote % 2 == 0)
			{
				if (brackets == 0 && /*oneOf.length == 0 ? ch == oneOf[0] :*/ isOneOf(ch, oneOf))
				{
					found = i;
					if (firstIndex)
						return found;
				}
				else if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing closing bracket in: " + s);
				}
			}
		}
		return found;
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param sequenceToFind | CharSequence to find!
	 * 
	 * @return Index of first found CharSequence that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfNotInObj(CharSequence s, CharSequence sequenceToFind)
	{
		return indexOfNotInObj(s, sequenceToFind, true);
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param sequenceToFind | CharSequence to find!
	 * @param firstIndex | If true, first index will be returned, if false last index will be returned.
	 * 
	 * @return Index of first found CharSequence that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfNotInObj(CharSequence s, CharSequence sequenceToFind, boolean firstIndex)
	{
		int found = -1;
		for (int i = 0, brackets = 0, quote = 0, match = 0, len = s.length(), lenToFind = sequenceToFind.length(); i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
			
			if (quote % 2 == 0)
			{
				if (brackets == 0 && ch == sequenceToFind.charAt(match++))
				{
					if (match == lenToFind)
					{
						found = i - match + 1;
						if (firstIndex)
							return found;
						match = 0;
					}
				}
				else if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing closing bracket in: " + s);
				}
				else
					match = 0;
			}
		}
		return found;
	}
	
	/**
	 * @param str | String to do replacements in!
	 * @param target | Target to replace!
	 * @param replacement | Replacement for target!
	 * 
	 * @return Inserted string after replacing all targets with replacements similar to {@link String#replace(CharSequence, CharSequence)} but faster!
	 * 
	 * @since 1.2.0
	 */
	public static String fastReplace(String str, String target, CharSequence replacement) 
	{
	    int targetLength = target.length();
	    if (targetLength == 0) 
	        return str;

	    int i1 = 0, i2 = str.indexOf(target);
	    if (i2 < 0) 
	        return str;

	    StringBuilder sb = new StringBuilder(targetLength > replacement.length() ? str.length() : str.length() * 2);
	    do 
	    {
	        sb.append(str, i1, i2).append(replacement);
	        i1 = i2 + targetLength;
	        i2 = str.indexOf(target, i1);
	    } while (i2 > 0);
	    
	    return sb.append(str, i1, str.length()).toString();
	}
	
	/**
	 * @param ch | Char to compare!
	 * @param chars | Chars to match!
	 * 
	 * @return True if inserted char is any of inserted chars!
	 * 
	 * @since 1.3.0 
	 */
	public static boolean isOneOf(int ch, char... chars)
	{
		if (chars.length > 0)
		{
			for (int i = 0, len = chars.length; i < len; i++)
				if (chars[i] == ch)
					return true;
		}
		return false;
	}
	
	/**
	 * @return {@link String#contains(CharSequence)} for char sequence!
	 * 
	 * @since 1.3.0
	 */
	public static boolean contains(CharSequence str, char... oneOf)
	{
		if (oneOf.length == 1)
			for (int i = 0, len = str.length(); i < len; i++) 
				if (str.charAt(i) == oneOf[0])
					return true;
			
		for (int i = 0, len = str.length(); i < len; i++) 
			if (isOneOf(str.charAt(i), oneOf))
				return true;
		return false;
	}
	
	/**
	 * @param str | String to display!
	 * @param pos | Position to display!
	 * 
	 * @return String with displayed position!
	 * Use for debugging or error printing!
	 * 
	 * @since 1.3.2
	 */
	public static String showPosInString(CharSequence str, int pos)
	{
		return str + "\n" + multilpy(' ', pos) + "^";	
	}
	
	/* Arrays */
	
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
		Object[] array1 = fromAmbiguousArray(arr1), array2 = arr2.getClass().isArray() ? fromAmbiguousArray(arr2) : new Object[] { arr2 };
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
	 * @since 1.3.2 (since 1.3.7 moved from ArrayConverter)
	 */
	public static Object[] fromAmbiguousArray(Object array)
	{
		if (array instanceof Object[])
			return (Object[]) array;
		
		int len = Array.getLength(array);
		Object[] arr = new Object[len];
		for (int i = 0; i < len; i++) 
			arr[i] = Array.get(array, i);
		return arr;
	}
	
	/* Others... */
	
	/**
	 * This will serialize serializer into http query post request however this is not the best networking and you should implement your own http client if you want SerialX to serialize and deserialize remote content!
	 * 
	 * @param serializer | Serializer to post.
	 * @param conn | Http connection to use!
	 * 
	 * @throws IOException if posting failed!
	 * 
	 * @since 1.3.5
	 */
	public static void post(Serializer serializer, HttpURLConnection conn) throws IOException
	{
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : serializer.varEntrySet()) 
        {
            if (postData.length() != 0) 
            	postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=');
            postData.append(URLEncoder.encode(serializer.getParsers().toString(param.getValue()).toString(), "UTF-8"));
        }
        
        for (Object param : serializer) 
        {
            if (postData.length() != 0) 
            	postData.append('&');
            postData.append(URLEncoder.encode(serializer.getParsers().toString(param).toString(), "UTF-8"));
        }
        
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
	}
	
	/**
	 * This is a "dummy" class that {@link Serializer} uses internally as an OOP programmatic interpretation of null. In otherwise this is wrapper object for null.
	 * Note: You should not be able to come in contact with this during serialization and loading, if you did then you most likely encountered and bug and you should report it!
	 * 
	 * @author PETO
	 *
	 * @since 1.2.2
	 */
	public static final class NULL
	{
		public static Object toOopNull(Object obj)
		{
			return obj == null ? new NULL() : obj;
		}
		
		@Override
		public boolean equals(Object obj) 
		{
			return obj == null || obj instanceof NULL;
		}
		
		@Override
		public String toString() 
		{
			return "null";
		}
	}
}