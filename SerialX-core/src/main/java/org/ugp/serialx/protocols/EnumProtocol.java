 package org.ugp.serialx.protocols;

import java.util.Collection;

/**
 * EnumProtocol is universal protocol to serialize any enumerator ({@link Collection} instance).
 * 
 * @author PETO
 *
 * @since 1.2.2
 */
public class EnumProtocol extends SerializationProtocol<Enum<?>> 
{
	@Override
	public Object[] serialize(Enum<?> object) 
	{
		return new Object[] {object.name()};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Enum<?> unserialize(Class<? extends Enum<?>> objectClass, Object... args)
	{
		return Enum.valueOf((Class<Enum>) objectClass, args[0].toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Enum<?>> applicableFor()
	{
		return (Class<? extends Enum<?>>) Enum.class;
	}
}
