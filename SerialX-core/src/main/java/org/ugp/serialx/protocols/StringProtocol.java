package org.ugp.serialx.protocols;

/**
 * StringProtocol is universal protocol to serialize any {@link CharSequence} instance. The condition of use is public constructor with one {@link String} or byte[] argument.
 * 
 * @author PETO
 *
 * @since 1.0.0 and universal for {@link CharSequence} since 1.2.0
 */
public class StringProtocol extends SerializationProtocol<CharSequence> 
{
	@Override
	public Object[] serialize(CharSequence object) 
	{		
		int len = object.length();
		Object[] chars = new Object[len];
		
		for (int i = 0; i < len; i++) 
			chars[i] = (int) object.charAt(i);
		return chars;
	}

	@Override
	public CharSequence unserialize(Class<? extends CharSequence> objectClass, Object... args) throws Exception 
	{	
		if (args.length == 1 && args[0] instanceof CharSequence)
			return (CharSequence) args[0];
		
		if (objectClass.isInterface())
			objectClass = String.class;
		
		char[] chars = new char[args.length];
		for (int i = 0; i < args.length; i++) 
			if (args[i] != null)
			{
				if (args[i] instanceof Character)
					chars[i] = (char) args[i];
				else
					chars[i] = (char)((Number) args[i]).intValue();
			}
			else
				break;

		String str = new String(chars);
		if (objectClass == String.class)
			return str;
		try
		{
			return objectClass.getConstructor(String.class).newInstance(str);
		}
		catch (Exception e)
		{
			return objectClass.getConstructor(char[].class).newInstance(chars);
		}
	}

	@Override
	public Class<? extends CharSequence> applicableFor() 
	{
		return CharSequence.class;
	}
}
