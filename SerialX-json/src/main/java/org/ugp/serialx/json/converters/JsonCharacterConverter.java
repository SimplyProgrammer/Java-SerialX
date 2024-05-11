package org.ugp.serialx.json.converters;

import org.ugp.serialx.converters.CharacterConverter;

/**
 * {@link CharacterConverter} modified to match JSON more closely. It will serialize char as {@link String} since JSON does not know char...
 * 
 * @author PETO
 * 
 * @since 1.3.7
 */
public class JsonCharacterConverter extends CharacterConverter 
{
	protected boolean formatAsString;
	
	/**
	 * @param formatAsString | If true, character will be serialized in a letter form. Otherwise it will be serialized as ASCII value (int).
	 * 
	 * @since 1.3.7
	 */
	public JsonCharacterConverter(boolean formatAsString) 
	{
		setFormatAsString(formatAsString);
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Character)
			return isFormatAsString() ? "\""+obj+"\"" : String.valueOf((int) (char) obj);
		return CONTINUE;
	}

	/**
	 * @return True if character is going to be serialized in a letter form. If false, it will be serialized as ASCII value (int).
	 * 
	 * @since 1.3.7
	 */
	public boolean isFormatAsString()
	{
		return formatAsString;
	}

	/**
	 * @param formatAsString | If true, character will be serialized in a letter form. Otherwise it will be serialized as ASCII value (int).
	 * 
	 * @since 1.3.7
	 */
	public void setFormatAsString(boolean formatAsString) 
	{
		this.formatAsString = formatAsString;
	}
}
