package org.ugp.serialx.converters;

import java.util.Collection;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Scope;

/**
 * This interface is supposed to be used to parse strings back to java objects using {@link DataParser#parse(String, Object...)}!
 * Instance of DataParser should be registered into {@link DataParser#REGISTRY} or other external registry in order to work, also only one instance of each DataParser should be used and accessed via this registry! <br>
 * Static method {@link DataParser#parseObj} is used to walk this registry and parse inserted string in process, in other words we can say that this interface contains <a href = "https://en.wikipedia.org/wiki/Recursive_descent_parser">recursive descent parse</a> that uses its own implementations!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public interface DataParser
{
	/**
	 * This is the way how {@link DataParser} represents void. You can return this in {@link DataParser#parse(String, Object...)} as a void.
	 * This can be useful when you are adding something to "storage" while parsing and you do not want it to be returned.
	 * 
	 * @since 1.2.2 (moved to {@link DataParser} since 1.3.0)
	 * 
	 * @see Void#TYPE
	 */
	public static final Object VOID = Void.TYPE;
	
	/**
	 * This is connected with {@link DataParser#parse(String, Object...)} and {@link DataParser#parseObj(String, Object...)}! And its a way to tell that this parser is not suitable for parsing obtained string and search for optimal one should continue.
	 * 
	 * @since 1.3.0
	 */
	public static final String CONTINUE = new String();
	
	/**
	 * This is DataParser registry. Here your parser implementations should be registered in order to work properly!
	 * Only one parser should be usable for specific input string, otherwise order of registration is crucial!
	 * Defaultly there are parsers from ugp.org.SerialX.converters.
	 * 
	 * @since 1.3.0
	 */
	public static final ParserRegistry REGISTRY = new ParserRegistry(new VariableParser(), new StringConverter(), new ProtocolConverter(), new NumberConverter(), new BooleanConverter(), new CharacterConverter(), new NullConverter(), new SerializableBase64Converter());
	
	/**
	 * @param myHomeRegistry | Registry where this parser is registered provided by {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)} otherwise it demands on implementation (it should not be null)!
	 * @param str | Source string!
	 * @param args | Some additional args. This can be anything and it demands on implementation of DataParser. Default SerialX API implementation will provide one optional argument with {@link Scope} that value was loaded from!
	 * 
	 * @return Object that was parsed from obtained string. Special return types are {@link DataParser#VOID} and {@link DataParser#CONTINUE}. Continue will ignore this parser and jump to another one in registry.
	 * 
	 * @since 1.3.0
	 */
	Object parse(ParserRegistry myHomeRegistry, String str, Object... args);
	
	/**
	 * @param str | Source string to parse using suitable parser from registry.
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Object that was parsed from obtained string using suitable parser. This method will iterate {@link DataParser#REGISTRY} and try to parse string using each registered parser until suitable return is obtained by parse method of parser, first suitable result will be returned! You can return {@link DataParser#CONTINUE} to mark parser as not suitable for parsing obtained string.
	 * If no suitable result was found, null will be returned and you will be notified in console (null does not necessary means invalid output since null can be proper result of parsing)!
	 * 
	 * @since 1.3.0
	 */
	public static Object parseObj(String str, Object... args)
	{
		return REGISTRY.parse(str, args);
	}
	
	/**
	 * @deprecated Use {@link ParserRegistry#parse(String, Object...)}!
	 * 
	 * @param registry | Registry to use!
	 * @param str | Source string to parse using suitable parser from registry.
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Object that was parsed from obtained string using suitable parser. This method will iterate registry and try to parse string using each registered parser until suitable return is obtained by parse method of parser, first suitable result will be returned! You can return {@link DataParser#CONTINUE} to mark parser as not suitable for parsing obtained string.
	 * If no suitable result was found, null will be returned and you will be notified in console (null does not necessary means invalid output since null can be proper result of parsing)!
	 * 
	 * @since 1.3.0
	 */
	@Deprecated
	public static Object parseObj(Registry<DataParser> registry, String str, Object... args)
	{
		return parseObj(registry, str, false, null, args);
	}
	
	/**
	 * @deprecated Use {@link ParserRegistry#parse(String, boolean, Class[], Object...)}!
	 * 
	 * @param registry | Registry to use!
	 * @param str | Source string to parse using suitable parser from registry.
	 * @param returnAsStringIfNotFound | If true, inserted string will be returned instead of null and error message!
	 * @param ignore | {@link DataParser} class to ignore!
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Object that was parsed from obtained string using suitable parser. This method will iterate registry and try to parse string using each registered parser until suitable return is obtained by parse method of parser, first suitable result will be returned! You can return {@link DataParser#CONTINUE} to mark parser as not suitable for parsing obtained string.
	 * If no suitable result was found, null or inserted string will be returned based on returnAsStringIfNotFound!
	 * 
	 * @since 1.3.0
	 */
	@Deprecated
	public static Object parseObj(Registry<DataParser> registry, String str, boolean returnAsStringIfNotFound, Class<?>[] ignore, Object... args)
	{
		if (registry instanceof ParserRegistry)
			return ((ParserRegistry) registry).parse(str, returnAsStringIfNotFound, ignore, args);
		return parseObj(new ParserRegistry(registry), str, returnAsStringIfNotFound, ignore, args);
	}
	
	/**
	 * @deprecated Use {@link DataParser.REGISTRY#getParserFor(String, Object...)}!
	 * 
	 * @param registry | Registry to search!
	 * @param str | String to find parser for!
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Parser suitable for parsing required string, selected from {@link DataParser#REGISTRY}!
	 * 
	 * @since 1.3.0
	 */
	@Deprecated
	public static DataParser getParserFor(String str, Object... args)
	{
		return getParserFor(REGISTRY, str, args);
	}
	
	/**
	 * @deprecated Use {@link DataParser#getParserFor(String, Object...)}!
	 * 
	 * @param registry | Registry to search!
	 * @param str | String to find parser for!
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Parser suitable for parsing required string, selected from inserted registry!
	 * 
	 * @since 1.3.0
	 */
	@Deprecated
	public static DataParser getParserFor(Registry<DataParser> registry, String str, Object... args)
	{
		if (registry instanceof ParserRegistry)
			return ((ParserRegistry) registry).getParserFor(str, args);
		return getParserFor(new ParserRegistry(registry), str, args);
	}
	
	/**
	 * Registry to store {@link DataParser} and {@link DataConverter} and performing parsing (String -> Object) and converting (Object -> String) operations with them!
	 * 
	 * @author PETO
	 *
	 * @since 1.3.5
	 */
	public static class ParserRegistry extends Registry<DataParser>
	{
		private static final long serialVersionUID = -2598324826689380752L;
		
		protected DataParser[] parsingCache;
		protected DataParser[] convertingCache;
		
		/**
		 * Constructs an {@link ParserRegistry} with the specified initial capacity.
		 * 
		 * @param initialSize | Initial capacity.
		 * 
		 * @since 1.3.5
		 */
		public ParserRegistry(int initialSize) 
		{
			super(initialSize);
		}
		
		/**
		 * Constructs an {@link ParserRegistry} with content of c.
		 * 
		 * @param c | Initial content of registry.
		 * 
		 * @since 1.3.5
		 */
		public ParserRegistry(Collection<? extends DataParser> c) 
		{
			super(c);
		}
		
		/**
		 * Constructs an {@link ParserRegistry} with parsers.
		 * 
		 * @param parsers | Initial content of registry.
		 * 
		 * @since 1.3.5
		 */
		public ParserRegistry(DataParser... parsers) 
		{
			super(parsers);
		}
		
		@Override
		public ParserRegistry clone() 
		{
			return new ParserRegistry(this);
		}
		
		/**
		 * @param str | String to find parser for!
		 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
		 * 
		 * @return Parser suitable for parsing required string, selected from inserted registry!
		 * 
		 * @since 1.3.5
		 */
		public DataParser getParserFor(String str, Object... args)
		{
			for (DataParser parser : this) 
				if (parser.parse(this, str, args) != CONTINUE)
					return parser;
			return null;
		}
		
		/**
		 * @param obj | Object to find converter for!
		 * @param args | Additional arguments that will be obtained in {@link DataParser#toString(String, Object...)}!
		 * 
		 * @return Converter suitable for converting required obj to string, selected from registry!
		 * 
		 * @since 1.3.5
		 */
		public DataConverter getConverterFor(Object obj, Object... args)
		{
			for (DataParser parser : this) 
				if (parser instanceof DataConverter && ((DataConverter)parser).toString(this, obj, args) != CONTINUE)
					return (DataConverter) parser;
			return null;
		}
		
		/**
		 * @param obj | Object to convert into string!
		 * @param args | Additional arguments that will be obtained in {@link DataParser#toString(String, Object...)}!
		 * 
		 * @return Object converted to string using {@link DataConverter} suitable converter picked from registry!
		 * {@link DataConverter#toString(Object, Object...)} of all registered converters will be called however only suitable ones should return the result, others should return {@link DataParser#CONTINUE}!
		 * 
		 * @since 1.3.5
		 */
		public CharSequence toString(Object obj, Object... args)
		{
			CharSequence str = null;
			if (convertingCache != null)
				for (DataParser parser : convertingCache)
					if (parser != null && (str = ((DataConverter) parser).toString(this, obj, args)) != CONTINUE)
						return str; 
			
			for (int i = 0, size = size(); i < size; i++) 
			{
				DataParser parser = get(i);
				if (parser instanceof DataConverter && (str = ((DataConverter) parser).toString(this, obj, args)) != CONTINUE)
				{
					if (convertingCache != null && i < convertingCache.length)
						convertingCache[i] = parser; 
					return str;
				}
			}

			LogProvider.instance.logErr("Unable to convert \"" + obj == null ? "null" : obj.getClass().getName() + "\" to string because none of registered converters were aplicable for this object!", null);
			return null;
		}
		
		/**
		 * @param str | Source string to parse using suitable parser from registry.
		 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
		 * 
		 * @return Object that was parsed from obtained string using suitable parser. This method will iterate registry and try to parse string using each registered parser until suitable return is obtained by parse method of parser, first suitable result will be returned! You can return {@link DataParser#CONTINUE} to mark parser as not suitable for parsing obtained string.
		 * If no suitable result was found, null will be returned and you will be notified in console (null does not necessary means invalid output since null can be proper result of parsing)!
		 * 
		 * @since 1.3.5
		 */
		public Object parse(String str, Object... args)
		{
			return parse(str, false, null, args);
		}
		
		/**
		 * @param str | Source string to parse using suitable parser from registry.
		 * @param returnAsStringIfNotFound | If true, inserted string will be returned instead of null and error message!
		 * @param ignore | {@link DataParser} class to ignore!
		 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
		 * 
		 * @return Object that was parsed from obtained string using suitable parser. This method will iterate registry and try to parse string using each registered parser until suitable return is obtained by parse method of parser, first suitable result will be returned! You can return {@link DataParser#CONTINUE} to mark parser as not suitable for parsing obtained string.
		 * If no suitable result was found, null or inserted string will be returned based on returnAsStringIfNotFound!
		 * 
		 * @since 1.3.5
		 */
		public Object parse(String str, boolean returnAsStringIfNotFound, Class<?>[] ignore, Object... args)
		{
			Object obj = null; 
			if (parsingCache != null)
				for (DataParser parser : parsingCache)
					if (parser != null && (obj = parser.parse(this, str, args)) != CONTINUE)
						return obj; 
			
			registryLoop: for (int i = 0, size = size(); i < size; i++)
			{
				DataParser parser = get(i);
				if (ignore != null)
					for (Class<?> cls : ignore) 
						if (cls == parser.getClass())
							continue registryLoop;

				if ((obj = parser.parse(this, str, args)) != CONTINUE)
				{
					if (parsingCache != null && i < parsingCache.length)
						parsingCache[i] = parser; 
					return obj;
				}
			}

			if (returnAsStringIfNotFound)
				return str;

			LogProvider.instance.logErr("Unable to parse \"" + str + "\" because none of registred parsers were suitable!", null);
			return null;
		}
		
		/**
		 * @param classOfParserToPrecache | Class of parser to precache!
		 * 
		 * @return Int array of 2 signifying the index of where the parser was inserted in parsing cache and converting cache (index 0 = parsing cache index, index 1 = converting cache index)
		 */
		public int[] preCache(Class<? extends DataParser> classOfParserToPrecache)
		{
			int[] ret = {-1, -1};
			if (parsingCache == null && convertingCache == null)
				return ret;
			
			DataParser parser = null;
			int i = 0;
			for (int size = size(); i < size; i++) 
			{
				DataParser elm = get(i);
				Class<?> objCls = elm.getClass();
				if (objCls == classOfParserToPrecache)
				{
					parser = elm;
					break;
				}
			}
			
			if (parser == null)
				return ret;
			
			if (i < parsingCache.length)
			{
				parsingCache[i] = parser;
				ret[0] = i;
			}
			
			if (i < convertingCache.length)
			{
				convertingCache[i] = parser;
				ret[1] = i;
			}
			
			return ret;
		}
		
		/**
		 * Recreates and enables both parsing cache and converting cache! Doing this might give you a solid performance boost when parsing or converting large amount of objects with this registry! But sometimes, this might cause some unexpected behavior especially when you have multiple parsers that are dependent on each other!<br>
		 * Note: Doing this will destroy any existing cache (this is usually not a big problem)!
		 * 
		 * @see ParserRegistry#destroyCache()
		 * 
		 * @since 1.3.5
		 */
		public void resetCache()
		{
			int size = size();
			resetCache(new DataParser[size], new DataParser[size]);
		}
		
		/**
		 * You can use this to manually set caching arrays. Doing this might give you a solid performance boost when parsing or converting large amount of objects with this registry! But sometimes, this might cause some unexpected behavior especially when you have multiple parsers that are dependent on each other!
		 * 
		 * @param parsingCache | Array of specific parsing cache to use (it can contains some preached parsers to use preferably). This array is supposed to be as long as this registry!
		 * @param convertingCache | Array of specific converter cache to use (it can contains some preached converters to use preferably). This array is supposed to be as long as this registry!
		 * 
		 * @since 1.3.5
		 */
		public void resetCache(DataParser[] parsingCache, DataParser[] convertingCache)
		{
			if (parsingCache != null)
				this.parsingCache = parsingCache;
			if (convertingCache != null)
				this.convertingCache = convertingCache;
		}
		
		/**
		 * Destroys any existing cache and stops any further caching! Use this if you are experiencing some strange behaviors!
		 * 
		 * @since 1.3.5
		 */
		public void destroyCache()
		{
			this.parsingCache = this.convertingCache = null;
		}
		
		/**
		 * @return Cache array for parsing (null if caching is disabled)!
		 * 
		 * @since 1.3.5
		 */
		public DataParser[] getParsingCache()
		{
			return parsingCache;
		}
		
		/**
		 * @return Cache array for converting (null if caching is disabled)!
		 * 
		 * @since 1.3.5
		 */
		public DataParser[] getConverterCache()
		{
			return convertingCache;
		}
	}
}
