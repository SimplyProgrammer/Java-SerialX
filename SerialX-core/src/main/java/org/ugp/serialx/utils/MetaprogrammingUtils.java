package org.ugp.serialx.utils;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.protocols.SerializationProtocol;

/**
 * Provides general utility used across SerialX library, provides reflection and IO helpers.
 * 
 * @author PETO
 * 
 * @since 1.3.8 (split from Utils in 4.0.0)
 */
public final class MetaprogrammingUtils {
	private MetaprogrammingUtils() {}
	
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
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link #LoadFileToString(File)} which is doing this automatically!
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
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link #LoadFileToString(File)} which is doing this automatically!
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
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link #LoadFileToString(File)} which is doing this automatically!
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
	 * @return The returned result of called method or {@link #VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
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
	 * @return The returned result of called method or {@link #VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
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
	 * @param objCls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link #VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
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
	 * @param objCls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param argClasses | Classes of args.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link #VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
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
			Object result = method.invoke(obj, args);
			return method.getReturnType().equals(void.class) ? VOID : result;
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException find) 
		{
			for (Method method : objCls.getMethods()) 
				if (method.getName().equals(name))
					try
					{
						Object result = method.invoke(obj, args);
						return method.getReturnType().equals(void.class) ? VOID : result;
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
		return Clone(obj, DataParser.REGISTRY, new Object[0], new Scope());
	}
	
	/**
	 * @param obj | Object to clone.
	 * @param parsersToUse | Parsers that will be used for cloning...
	 * @param converterArgs | Argument for {@link DataConverter#objToString(Object, Object...)}!
	 * @param parserArgs | Arguments for {@link ParserRegistry#parse(String, boolean, Class, Object...)}!
	 * 
	 * @return Cloned object using {@link DataParser}, {@link DataConverter} and {@link SerializationProtocol} or the same object as inserted one if cloning is not possible, for instance when protocol was not found and object is not instance of {@link Cloneable}.
	 * This clone function will always prioritized the Protocol variation, regular cloning is used only when there is no protocol registered or exception occurs. <br>
	 * Note: If there are protocols to serialize inserted object and all its sub-objects and variables then this clone will be absolute deep copy, meaning that making any changes to this cloned object or to its variables will not affect original one in any way! 
	 * But keep in mind that this clone is absolute hoverer, based on protocols used, it does not need to be an 100% copy! Also note that certain objects such as primitive wrappers ({@link Integer}, {@link Float} etc...) will not be necessarily cloned since cloning them is not reasonable...
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T Clone(T obj, Collection<DataParser> parsersToUse, Object[] converterArgs, Object... parserArgs)
	{
		if (obj == null) 
			return null;
		if (obj.getClass() == Byte.class)
			return (T) (Byte) ((byte) obj); // valueOf call...
		if (obj.getClass() == Short.class)
			return (T) (Short) ((short) obj);
		if (obj.getClass() == Integer.class)
			return (T) (Integer) ((int) obj);
		if (obj.getClass() == Long.class)
			return (T) (Long) ((long) obj);
		if (obj.getClass() == Float.class)
			return (T) (Float) ((float) obj);
		if (obj.getClass() == Double.class)
			return (T) (Double) ((double) obj);
		if (obj.getClass() == Character.class)
			return (T) (Character) ((char) obj);
		if (obj.getClass() == Boolean.class)
			return (T) (Boolean) ((boolean) obj);
		if (obj.getClass() == String.class)
			return (T) new String((String) obj);
		
		ParserRegistry parsers = parsersToUse instanceof ParserRegistry ? (ParserRegistry) parsersToUse : new ParserRegistry(parsersToUse);
		
		Object cln = parsers.parse(parsers.toString(obj, converterArgs).toString(), parserArgs);
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
	 * @param unwrap | If the box types should be unwrapped to primitives...
	 * @param objs | Array of objects.
	 * 
	 * @return Array of inserted objects class types. Wrapper types of primitive values will be converted to primitive types! For instance: Integer.class -> int.class
	 * 
	 * @since 1.3.5
	 */
	public static Class<?>[] ToClasses(boolean unwrap, Object... objs)
	{
		Class<?>[] classes = new Class<?>[objs.length];
		if (unwrap)
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
			classes[i] = objs[i] == null ? Object.class : objs[i].getClass();
		return classes;
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
		int len;
		Object arr = Array.newInstance(ToClasses(toType)[0], len = sourceArray.length);
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
	 * @since 1.3.2 (since 1.3.8 moved from ArrayConverter)
	 */
	public static Object[] fromAmbiguousArray(Object array)
	{
		if (array instanceof Object[])
			return (Object[]) array;

		int len = Array.getLength(array); // Arr of primitives cos Java...
		Object[] arr = new Object[len];
		for (int i = 0; i < len; i++) 
			arr[i] = Array.get(array, i);
		return arr;
	}

	/* Others... */
	
	/**
	 * @deprecated DO NOT USE! THIS FUNCTION, ESPECIALY ITS UTILIZATIOn IN Serializer WAS DEEMED TO BE WAY TO PROBLEMATIC AND POTENTIALLY DANAGEROUS IN THIS FORM. PLEASE IMPLEMENT YOUR OWN HTTP post WHEN SERIALIZING INTO {@link HttpURLConnection}!<br>
	 * 
	 * This will serialize serializer into http query post request however this is not the best networking and you should implement your own http client if you want SerialX to serialize and deserialize remote content!
	 * 
	 * @param serializer | Serializer to post.
	 * @param conn | Http connection to use! Assumed to be in output mode.
	 * 
	 * @throws IOException if posting failed!
	 * 
	 * @since 1.3.5
	 */
	@Deprecated
	public static void post(Serializer serializer, HttpURLConnection conn) throws IOException
	{
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : serializer.varEntrySet()) 
		{
		    if (postData.length() != 0) 
		    	postData.append('&');
		    postData.append(param.getKey()).append('=');
		    postData.append(serializer.getParsers().toString(param.getValue()));
		}
		
		for (Object param : serializer) 
		{
		    if (postData.length() != 0)
		    	postData.append('&');
		    postData.append(serializer.getParsers().toString(param));
		}
		
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);
	}
}