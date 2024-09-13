package org.ugp.serialx.juss.protocols;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Utils;
import org.ugp.serialx.protocols.SerializationProtocol;

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

	protected final Class<? extends T> applicableFor;
	
	/**
	 * @param applicableFor | Class that can be serialized using this protocol.
	 * Note: Passing {@link Object#getClass()} will make this protocol universal and work for any {@link Object} instance, this can be considered unsafe in some cases...
	 * 
	 * @since 1.3.8
	 */
	public UniversalObjectInstantiationProtocol(Class<? extends T> applicableFor)
	{
		this.applicableFor = applicableFor;
	}
	
	@Override
	public Object[] serialize(T object) 
	{
		throw new UnsupportedOperationException("This protocol is only for reading! It cant serialize " + object);
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
			int argCount = args.length;
			for (Constructor<?> cons : objectClass.getConstructors())
				if (cons.getParameterCount() == argCount)
					try
					{
						return (T) cons.newInstance(args);
					}
					catch (IllegalArgumentException e) 
					{
						try
						{
							for (int i = 0; i < argCount; i++)
							{
								Object arg;
								if ((arg = args[i]) instanceof GenericScope)
								{
									Type[] paramTypes;
									args = args.clone();
									args[i] = ((GenericScope<String, ?>) arg).toObject((paramTypes = cons.getGenericParameterTypes())[i]);

									for (i++; i < argCount; i++)
										if ((arg = args[i]) instanceof GenericScope)
											args[i] = ((GenericScope<String, ?>) arg).toObject(paramTypes[i]);
									return (T) cons.newInstance(args);
								}
							}
						}
						catch (IllegalArgumentException e2)
						{}
					}
		}
		LogProvider.instance.logErr("Unable to create new instance of \"" + objectClass + "\" because inserted arguments " + Arrays.asList(args) + " cannot be applied on any public constructor in required class!", null);
		return null;
	}

	@Override
	public Class<? extends T> applicableFor() 
	{
		return applicableFor;
	}
	
	@Override
	public long getMode() 
	{
		return MODE_DESERIALIZE;
	}
}
