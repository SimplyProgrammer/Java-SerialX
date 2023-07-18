package org.ugp.serialx.protocols;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Utils;

/**
 * Universal protocol for deserializing any object using its constructor. Args array of {@link UniversalObjectInstantiationProtocol#unserialize(Class, Object...)} must have elements applicable as arguments for some constructor of required objects class!
 * Note: This protocol is for deserialization only!
 * 
 * @author PETO
 *
 * @param <T> Generic type of deserialized object!
 * 
 * @since 1.3.5
 */
public class UniversalObjectInstantiationProtocol<T> extends SerializationProtocol<T> {

	@Override
	public Object[] serialize(T object) 
	{
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": This protocol is only for reading! It cant serialize " + object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T unserialize(Class<? extends T> objectClass, Object... args) throws Exception 
	{
		try 
		{
			return objectClass.getConstructor(Utils.ToClasses(args)).newInstance(args);
		}
		catch (Exception e0) 
		{
			for (Constructor<?> cons : objectClass.getConstructors()) 
				try
				{
					return (T) cons.newInstance(args);
				}
				catch (IllegalArgumentException e) 
				{}
		}
		LogProvider.instance.logErr(getClass().getSimpleName() + ": Unable to call create new instance of \"" + objectClass + "\" because inserted arguments " + Arrays.asList(args) + " cannot be applied on any public constructor in required class!", null);
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends T> applicableFor() 
	{
		return (Class<? extends T>) Object.class;
	}
	
	@Override
	public byte getMode() 
	{
		return MODE_DESERIALIZE;
	}
}
