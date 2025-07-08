package org.ugp.serialx.json.converters;

import org.ugp.serialx.juss.converters.VariableConverter;

/**
 * Json specific {@link VariableConverter}. Wraps keys in <code>""</code> and uses <code>:</code> instead of <code>=</code>
 * 
 * @since 1.3.9
 */
public class JsonVariableConverter extends VariableConverter 
{
	@Override
	protected StringBuilder appendEntry(StringBuilder keyString, CharSequence valueString, Object value, Object... args) 
	{
		if (args.length > 5 && args[5] instanceof Byte && (byte) args[5] != 0)
			return keyString.insert(0, '"').append("\" : ").append(valueString);
		return keyString.insert(0, '"').append("\":").append(valueString);
	}
}
