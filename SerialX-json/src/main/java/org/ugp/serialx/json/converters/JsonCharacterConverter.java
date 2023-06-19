package org.ugp.serialx.json.converters;

import org.ugp.serialx.converters.CharacterConverter;

public class JsonCharacterConverter extends CharacterConverter {
	
	protected boolean formatAsString;
	
	public JsonCharacterConverter(boolean formatAsString) {
		setFormatAsString(formatAsString); 
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Character)
			return isFormatAsString() ? obj.toString() : String.valueOf((int) (char) obj);
		return CONTINUE;
	}

	public boolean isFormatAsString() {
		return formatAsString;
	}

	public void setFormatAsString(boolean formatAsString) {
		this.formatAsString = formatAsString;
	}
}
