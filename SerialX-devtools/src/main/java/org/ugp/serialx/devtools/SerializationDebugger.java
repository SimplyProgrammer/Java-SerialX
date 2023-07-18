package org.ugp.serialx.devtools;

import static org.ugp.serialx.Utils.multilpy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ugp.serialx.Registry;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.devtools.converters.DebugParserRegistry;

/**	
 * Use this for debugging during parsing and converting by adding new instance of it into your parser {@link Registry} or in case of {@link Serializer} use !<br>
 * During parsing, type <code>__debug</code> or <code>__debug_yourObject</code> into your code!<br>
 * During converting/serializing, serialize your object using {@link DebugWrapper} with your object as argument!
 * 
 * @author PETO
 *
 * @since 1.3.5
 */
public class SerializationDebugger implements DataConverter
{		
	public static final String DEBUG_MARK = new String("__debug");
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args)
	{
		if (str.toLowerCase().startsWith(DEBUG_MARK))
			return printDebugs(myHomeRegistry instanceof DebugParserRegistry ? (DebugParserRegistry) myHomeRegistry : new DebugParserRegistry(myHomeRegistry), null, str, args);
		return CONTINUE;
	}
	
	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof DebugWrapper)
			return printDebugs(myHomeRegistry instanceof DebugParserRegistry ? (DebugParserRegistry) myHomeRegistry : new DebugParserRegistry(myHomeRegistry), ((DebugWrapper) obj).obj, null, args).toString();
		return CONTINUE;
	}
	
	/**
	 * Print and return debug results of parsing or converting!
	 * 
	 * @return Parsed object or object converted to string!
	 * 
	 * @since 1.3.5
	 */
	public Object printDebugs(DebugParserRegistry myHomeRegistry, Object objToSerialize, String strToParse, Object... args)
	{
		String action = objToSerialize == null && strToParse != null ? "Parsing" : "Converting";
		System.err.println("--------------------------------------------- " + action + " Debug ---------------------------------------------");
		if (args == null)
			System.err.println("------- Available args (args): none (args = null)!");
		else
			print("------- Available args (args):", Arrays.asList(args), 0);
		
		if (myHomeRegistry == null)
			System.err.println("\n------- Available parsers (myHomeRegistry: null): none!");
		else
			print("\n------- Available parsers (myHomeRegistry: " + myHomeRegistry.getClass().getName() + "):", myHomeRegistry, 0);

		if (strToParse != null && (strToParse = strToParse.substring(DEBUG_MARK.length())).length() > 0 && (strToParse = strToParse.substring(1)).length() > 0)
		{
			if (args.length < 100)
			{
				myHomeRegistry.getRegistryIterationStackTrace().clear();
				args = Arrays.copyOf(args, 100);
				args[99] = 0;
			}
			else if (args[99] instanceof Integer && (int) args[99] > 0)
			{
				myHomeRegistry = myHomeRegistry.clone(false);
			}
			
			double t0 = System.nanoTime();
			objToSerialize = myHomeRegistry.parse(strToParse, args);
			double t = System.nanoTime();
			
			if (myHomeRegistry.getParsingCache() == null)
				System.err.println("\n------- Paring cache (after process): null (none)");
			else
				print("\n------- Paring cache (after process):", Arrays.asList(myHomeRegistry.getParsingCache()), 0);
			print("\n------- Registry iterations (" + myHomeRegistry.getRegistryIterationStackTrace().size() + " in total, total time " + (t-t0)/1000000 + "ms):", myHomeRegistry.getRegistryIterationStackTrace(), 0);
			System.err.println("\n--------------------------------------------- " + action + " Results ---------------------------------------------");
			System.err.println("String \"" + strToParse + "\" was parsed into:\n\t" + toStringAndCls(objToSerialize));
			System.err.println("\n");
			return objToSerialize;
		}
		
		if (objToSerialize != null)
		{
			if (args.length < 100)
			{
				myHomeRegistry.getRegistryIterationStackTrace().clear();
				args = Arrays.copyOf(args, 100);
				args[99] = 0;
			}
			else if (args[99] instanceof Integer && (int) args[99] > 0)
			{
				myHomeRegistry = myHomeRegistry.clone(false);
			}
			
			double t0 = System.nanoTime();
			strToParse = myHomeRegistry.toString(objToSerialize, args).toString();
			double t = System.nanoTime();
			
			if (myHomeRegistry.getConverterCache() == null)
				System.err.println("\n------- Converting cache (after process): null (none)");
			else
				print("\n------- Converting cache (after process):", Arrays.asList(myHomeRegistry.getConverterCache()), 0);
			print("\n------- Registry iterations (" + myHomeRegistry.getRegistryIterationStackTrace().size() + " in total, total time " + (t-t0)/1000000 + "ms):", myHomeRegistry.getRegistryIterationStackTrace(), 0);
			System.err.println("\n--------------------------------------------- " + action + " Results ---------------------------------------------");
			System.err.println("Object " + toStringAndCls(objToSerialize) + " was converted to string:\n" + (strToParse.contains("\n") ? strToParse : "\t" + strToParse));
			System.err.println("\n");
			return strToParse;
		}
		
		if (myHomeRegistry.getParsingCache() == null)
			System.err.println("\n------- Current parsing cache: null (none)");
		else
			print("\n------- Current parsing cache:", Arrays.asList(myHomeRegistry.getParsingCache()), 0);
		
		if (myHomeRegistry.getConverterCache() == null)
			System.err.println("\n------- Current parsing cache: null (none)");
		else
			print("\n------- Current parsing cache:", Arrays.asList(myHomeRegistry.getConverterCache()), 0);
		System.err.println("\n--------------------------------------------- No Results ---------------------------------------------");
		System.err.println("\n");
		return VOID;
	}
	
	/**
	 * @param serializer | Serializer to debug!
	 * 
	 * @return Serializer capable of debugging its serialization and deserialization!
	 * 
	 * @since 1.3.5
	 */
	public static <T extends Serializer> T debug(T serializer) 
	{
		return debug(serializer, new SerializationDebugger());
	}
	
	/**
	 * @param serializer | Serializer to debug!
	 * @param debuger | Specific debugger to use!
	 * 
	 * @return Serializer capable of debugging its serialization and deserialization!
	 * 
	 * @since 1.3.5
	 */
	public static <T extends Serializer> T debug(T serializer, SerializationDebugger debugger) 
	{
		serializer.getParsers().add(0, debugger);
		serializer.setParsers(new DebugParserRegistry(serializer.getParsers()));
		return serializer;
	}
	
	/**
	 * @param objToSerializeAndDebug | Object you want to serialize and see the debug of serialization!
	 * 
	 * @return Object wrapped for debugger to debug!
	 * 
	 * @since 1.3.5
	 */
	public static DebugWrapper wrapForDebug(Object objToSerializeAndDebug)
	{
		return new DebugWrapper(objToSerializeAndDebug);
	}
	
	/**
	 * @return <code>obj + " (" + obj.getClass().getName() + ")"</code>
	 * Note: Used internally by debugger!
	 * 
	 * @since 1.3.5
	 */
	public static String toStringAndCls(Object obj)
	{
		if (obj == null)
			return "null";
		else
			return obj + " (" + obj.getClass().getName() + ")";
	}
	
	/**
	 * This will print list into console in somewhat more readable form with tabs and introduction text!
	 * 
	 * @since 1.3.5
	 */
	public static void print(String text, List<?> objs, int tabs)
	{
		System.err.println(text);
		for (int i = 0, i2 = 0; i < objs.size(); i++) 
		{
			Object o = objs.get(i);
			String strTbs = multilpy('\t', tabs).toString();
			if (o instanceof List)
				print(strTbs + (i2++) + ":\t" + o.getClass().getName() + ":", (List<?>) o, tabs+1);
			else if (o instanceof Map)
				print(strTbs + (i2++) + ":\t" + o.getClass().getName() + ":", (Map<?, ?>) o, tabs+1);
			else
				System.err.println(multilpy('\t', tabs).toString() + (i2++) + ":\t" + String.valueOf(o));
		}
	}
	
	/**
	 * This will print map into console in somewhat more readable form with tabs and introduction text!
	 * 
	 * @since 1.3.5
	 */
	public static void print(String text, Map<?, ?> map, int tabs)
	{
		System.err.println(text);
		for (Entry<?, ?> entry : map.entrySet()) 
		{
			Object o = entry.getValue();
			String strTbs = multilpy('\t', tabs).toString();
			if (o instanceof List)
				print(strTbs + (entry.getKey()) + ":\t" + o.getClass().getName() + ":", (List<?>) o, tabs+1);
			else if (o instanceof Map)
				print(strTbs + (entry.getKey()) + ":\t" + o.getClass().getName() + ":", (Map<?, ?>) o, tabs+1);
			else 
				System.err.println(strTbs + entry.getKey() + ":\t" + String.valueOf(o));
		}
	}

	/**
	 * Use this during converting/serializing!
	 * 
	 * @author PETO
	 *
	 * @since 1.3.5
	 * 
	 * @see SerializationDebugger
	 */
	protected static class DebugWrapper 
	{
		public final Object obj;
		
		/**
		 * @param yourObject | Your object to serialize in debug mode!
		 * 
		 * @since 1.3.5
		 */
		public DebugWrapper(Object yourObject)
		{
			obj = yourObject;
		}
	}
}