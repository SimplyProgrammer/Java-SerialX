package org.ugp.serialx.json.converters;

import java.io.IOException;

import org.ugp.serialx.converters.NumberConverter;

/**
 * {@link NumberConverter} modified to match JSON more closely. It will not use Juss number suffixes since JSON does not support them...
 * 
 * @author PETO
 * 
 * @since 1.3.8
 */
public class JsonNumberConverter extends NumberConverter 
{
	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj instanceof Number)
			return source.append(format((Number) obj));
		return CONTINUE;
	}
}
