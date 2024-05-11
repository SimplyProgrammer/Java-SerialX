package org.ugp.serialx.protocols;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * ListProtocol is universal protocol to serialize any {@link Collection} instance. The condition of use is public constructor with one {@link Collection} argument.
 * 
 * @author PETO
 *
 * @since 1.0.0 and applicable for {@link Collection} since 1.2.2
 */
public class ListProtocol extends SerializationProtocol<Collection<?>>
{
	@Override
	public Object[] serialize(Collection<?> obj) 
	{
		return obj.toArray();
	}

	@Override
	public Collection<?> unserialize(Class<? extends Collection<?>> objectClass, Object... args) throws Exception
	{
		if (objectClass.isInterface())
			return new ArrayList<>(Arrays.asList(args));

		try
		{
			return objectClass.getConstructor(Collection.class).newInstance(Arrays.asList(args));
		}
		catch (Exception e) 
		{
			return objectClass.getConstructor(Object[].class).newInstance(args);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Collection<?>> applicableFor() 
	{
		return (Class<? extends Collection<?>>) Collection.class;
	}
}
