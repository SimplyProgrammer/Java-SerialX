package org.ugp.serialx.json.converters;

import java.io.IOException;
import java.util.Map.Entry;

import org.ugp.serialx.juss.converters.VariableConverter;

/**
 * Json specific {@link VariableConverter}. Wraps keys in <code>""</code> and uses <code>:</code> instead of <code>=</code>
 * 
 * @since 1.3.9
 */
public class JsonVariableConverter extends VariableConverter 
{
	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj instanceof Entry)
		{
			Entry<?, ?> var = (Entry<?, ?>) obj;
			
			source.append('"').append(String.valueOf(var.getKey()));
			if (args.length > 5 && args[5] instanceof Byte && (byte) args[5] != 0)
				source.append("\" : ");
			else
				source.append("\":");
			return myHomeRegistry.toString(source, var.getValue(), args);
		}
		return CONTINUE;
	}
}
