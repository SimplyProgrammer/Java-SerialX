package org.ugp.serialx.devtools;

import java.util.Map;
import java.util.TreeMap;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;

/**
 * Special {@link ParserRegistry} that keeps track of its actions! Use only for debugging!
 * 
 * @author PETO
 *
 * @since 1.3.5
 */
public class DebugParserRegistry extends ParserRegistry 
{
	private static final long serialVersionUID = 3445967263611388142L;
	
	protected Map<Integer, Object> iterationStackTrace = new TreeMap<>();
	
	public DebugParserRegistry(ParserRegistry registry) 
	{
		super(registry);
		resetCache(registry.getParsingCache(), registry.getConverterCache());
	}
	
	@Override
	public DebugParserRegistry clone() 
	{
		return clone(true);
	}
	
	/**
	 * @param copyStackTrace | If true, cloned object will share the same iterationStackTrace with original, otherwise it will get new empty one.
	 * 
	 * @return Clone of this {@link DebugParserRegistry}
	 * 
	 * @since 1.3.7
	 */
	public DebugParserRegistry clone(boolean copyStackTrace) 
	{
		DebugParserRegistry reg = new DebugParserRegistry(this);
		if (copyStackTrace)
			reg.iterationStackTrace = this.iterationStackTrace;
		return reg;
	}
	
	@Override
	public CharSequence toString(Object obj, Object... args) {
		int iterationIndex = 0;
		if (args.length > 99 && args[99] instanceof Integer)
		{
			iterationIndex = (int) args[99];
			args[99] = iterationIndex + 1;
		}
		
		CharSequence str = null;
		if (convertingCache != null)
			for (int i = 0; i < convertingCache.length; i++)
			{
				DataParser parser = convertingCache[i];
				if (parser != null)
				{
					double t0 = System.nanoTime();
					str = ((DataConverter) parser).toString(this, obj, args);
					double t = System.nanoTime();
					if (str != SerializationDebugger.CONTINUE)
					{
						iterationStackTrace.put(iterationIndex, "[" + i + "] " + parser + " " + (t-t0)/1000000 + "ms (from cache)\n>>\t" + SerializationDebugger.toStringAndCls(obj) + "\t -->\t\"" + str + "\"");
						return str; 
					}
				}
			}
		
		for (int i = 0, size = size(); i < size; i++) 
		{
			DataParser parser = get(i);
			if (parser instanceof DataConverter)
			{
				double t0 = System.nanoTime();
				str = ((DataConverter) parser).toString(this, obj, args);
				double t = System.nanoTime();
				if(str != SerializationDebugger.CONTINUE)
				{
					if (convertingCache != null && i < convertingCache.length)
						convertingCache[i] = parser; 
					iterationStackTrace.put(iterationIndex, "[" + i + "] " + parser + " " + (t-t0)/1000000 + "ms\n>>\t" + SerializationDebugger.toStringAndCls(obj) + "\t -->\t\"" + str + "\"");
					return str;
				}
			}
		}
		
		LogProvider.instance.logErr("Unable to convert \"" + obj == null ? "null" : obj.getClass().getName() + "\" to string because none of registered converters were aplicable for this object!", null);
		return null;
	}
	
	@Override
	public Object parse(String str, boolean returnAsStringIfNotFound, Class<? extends DataParser> ignore, Object... args) 
	{
		int iterationIndex = 0;
		if (args.length > 99 && args[99] instanceof Integer)
		{
			iterationIndex = (int) args[99];
			args[99] = iterationIndex + 1;
		}
		
		Object obj = null; 
		if (parsingCache != null)
			for (int i = 0; i < parsingCache.length; i++)
			{
				DataParser parser = parsingCache[i];
				if (parser != null && (ignore == null || ignore != parser.getClass()))
				{
//					try 
//					{
						double t0 = System.nanoTime();
						obj = parser.parse(this, str, args);
						double t = System.nanoTime();
						if (obj != SerializationDebugger.CONTINUE)
						{
							iterationStackTrace.put(iterationIndex, "[" + i + "] " + parser + " " + (t-t0)/1000000 + "ms (from cache)\n>>\t\"" + str + "\"\t -->\t" + SerializationDebugger.toStringAndCls(obj));
							return obj; 
						}
//					}
//					catch (Exception ex)
//					{
//						iterationStackTrace.put(iterationIndex, "[" + i + "] " + parser + " (from cache)\n>>\t\"" + str + "\"\tthrew\t" + ex);
//						return null;
//					}
				}
			}
	
		for (int i = 0, size = size(); i < size; i++)
		{
			DataParser parser = get(i);
			if (ignore != null && ignore == parser.getClass())
				continue;

//			try 
//			{
				double t0 = System.nanoTime();
				obj = parser.parse(this, str, args);
				double t = System.nanoTime();
				if (obj != SerializationDebugger.CONTINUE)
				{
					if (parsingCache != null && i < parsingCache.length)
						parsingCache[i] = parser;
					iterationStackTrace.put(iterationIndex, "[" + i + "] " + parser + " " + (t-t0)/1000000 + "ms\n>>\t\"" + str + "\"\t -->\t" + SerializationDebugger.toStringAndCls(obj));
					return obj;
				}
//			}
//			catch (Exception ex)
//			{
//				iterationStackTrace.put(iterationIndex, "[" + i + "] " + parser + "\n>>\t\"" + str + "\"\tthrew\t" + ex);
//				return null;
//			}
		}

		if (returnAsStringIfNotFound)
			return str;

		LogProvider.instance.logErr("Unable to parse \"" + str + "\" because none of registred parsers were suitable!", null);
		return null;
	}
	
	/**
	 * @return Ordered map of registry iterations generated by using it during parsing or converting!
	 * 
	 * @since 1.3.5
	 */
	public Map<Integer, Object> getRegistryIterationStackTrace()
	{
		return iterationStackTrace;
	}
}