package org.ugp.serialx.json.converters;

import org.ugp.serialx.converters.NumberConverter;

public class JsonNumberConverter extends NumberConverter {
	
	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Number)
			return format((Number) obj);
		return CONTINUE;
	}
}
