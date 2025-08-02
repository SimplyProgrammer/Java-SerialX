package org.ugp.serialx.json.converters;

import java.io.IOException;

import org.ugp.serialx.converters.CharacterConverter;

/**
 * {@link CharacterConverter} modified to match JSON more closely. It will serialize char as {@link String} since JSON does not know char...
 * 
 * @author PETO
 * 
 * @since 1.3.8
 */
public class JsonCharacterConverter extends CharacterConverter 
{
	protected boolean formatAsString;
	
	/**
	 * @param formatAsString | If true, character will be serialized in a letter form. Otherwise it will be serialized as ASCII value (int).
	 * 
	 * @since 1.3.8
	 */
	public JsonCharacterConverter(boolean formatAsString) 
	{
		setFormatAsString(formatAsString);
	}

	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj instanceof Character)
			return source.append(isFormatAsString() ? "\""+obj+"\"" : String.valueOf((int) (char) obj));
		return CONTINUE;
	}

	/**
	 * @return True if character is going to be serialized in a letter form. If false, it will be serialized as ASCII value (int).
	 * 
	 * @since 1.3.8
	 */
	public boolean isFormatAsString()
	{
		return formatAsString;
	}

	/**
	 * @param formatAsString | If true, character will be serialized in a letter form. Otherwise it will be serialized as ASCII value (int).
	 * 
	 * @since 1.3.8
	 */
	public void setFormatAsString(boolean formatAsString) 
	{
		this.formatAsString = formatAsString;
	}
}
