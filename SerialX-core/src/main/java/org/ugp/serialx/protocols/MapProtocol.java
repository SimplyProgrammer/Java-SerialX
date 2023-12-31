package org.ugp.serialx.protocols;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;

/**
 * ListProtocol is universal protocol to serialize any {@link Map} instance. The condition of use is public constructor with one {@link Map} argument.
 * 
 * @author PETO
 *
 * @since 1.2.2
 */
public class MapProtocol extends SerializationProtocol<Map<Object, Object>> 
{
	@Override
	public Object[] serialize(Map<Object, Object> object) 
	{
		Object[] args = new Object[object.size()*2];
		int i = 0;
		for (Entry<?, ?> entry: object.entrySet()) 
		{
			args[i] = entry.getKey();
			args[i+1] = entry.getValue();
			i+=2;
		}
		return args;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<Object, Object> unserialize(Class<? extends Map<Object, Object>> objectClass, Object... args) throws Exception 
	{
		boolean isFromScope = args.length == 1 && args[0] instanceof GenericScope;
		if (args.length % 2 != 0 && !isFromScope)
			LogProvider.instance.logErr("Some variables have no values, this is not good!", null);
		
		if (objectClass.isInterface())
			objectClass = (Class<? extends Map<Object, Object>>) HashMap.class;
		
		if (isFromScope)
			return objectClass.getConstructor(Map.class).newInstance(((GenericScope<?, ?>) args[0]).variables());

		return objectClass.getConstructor(Map.class).newInstance(GenericScope.mapKvArray(new LinkedHashMap<>(args.length/2), args));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Map<Object, Object>> applicableFor() 
	{
		return (Class<? extends Map<Object, Object>>) Map.class;
	}
}
