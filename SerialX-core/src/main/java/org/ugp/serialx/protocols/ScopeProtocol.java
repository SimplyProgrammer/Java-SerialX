package org.ugp.serialx.protocols;

import java.util.HashMap;
import java.util.Map;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.Scope;
import org.ugp.serialx.converters.DataConverter;


/**
 * ScopeProtocol is universal protocol to serialize any {@link GenericScope} instance. The condition of use is public constructor with arguments {@link Map} and Object[]! <br>
 * Note: This protocol is unique in some way because it works for read only because scopes are normally serialized via {@link DataConverter} meaning calling serialize method will throw exception right away! But under normal circumstances protocols like this should not exist!
 * 
 * @author PETO
 *
 * @since 1.2.2
 */
public class ScopeProtocol extends SerializationProtocol<GenericScope<?, ?>> 
{
	@Override
	public Object[] serialize(GenericScope<?, ?> object) throws Exception 
	{
		if (object.getClass() != Scope.class)
			return new Object[] {object.castTo(Scope.class)};
		throw new UnsupportedOperationException("You are trying to serialize GenericScope or Scope via protocol! This is not good and should not even be possible! Scopes are meant to be serialized via converters!");
	}

	@Override
	public GenericScope<?, ?> unserialize(Class<? extends GenericScope<?, ?>> objectClass, Object... args) throws Exception 
	{
		if (args.length == 1 && args[0] instanceof GenericScope)
		{
			if (objectClass == args[0].getClass())
				return (GenericScope<?, ?>) args[0];
			return objectClass.getConstructor(Map.class, Object[].class).newInstance(((GenericScope<?, ?>) args[0]).toVarMap(), ((GenericScope<?, ?>) args[0]).toArray());
		}
		return objectClass.getConstructor(Map.class, Object[].class).newInstance(new HashMap<>(), args);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends GenericScope<?, ?>> applicableFor() 
	{
		return (Class<? extends GenericScope<?, ?>>) GenericScope.class;
	}
}
