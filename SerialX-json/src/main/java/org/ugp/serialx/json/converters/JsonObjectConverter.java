package org.ugp.serialx.json.converters;

import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.Utils;
import org.ugp.serialx.json.JsonSerializer;
import org.ugp.serialx.juss.converters.ObjectConverter;
import org.ugp.serialx.protocols.SerializationProtocol;

/**
 * Used internally by {@link JsonSerializer} to ensure proper and valid Json format for scopes and protocols.
 * 
 * @author PETO
 * 
 * @since 1.3.5
 */
public class JsonObjectConverter extends ObjectConverter
{		
	@SuppressWarnings("unchecked")
	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object arg, Object... args) 
	{
		if (arg.getClass().isArray())
			arg = new Scope(Utils.fromAmbiguousArray(arg));
		
		SerializationProtocol<Object> prot = (SerializationProtocol<Object>) getProtocolFor(arg, SerializationProtocol.MODE_SERIALIZE, args);

		if (prot != null && !(arg instanceof Scope))
			try
			{
				Object[] objArgs = prot.serialize(arg);
				if (objArgs.length == 1 && objArgs[0] instanceof Scope)
					return super.toString(myHomeRegistry, objArgs[0], args);
				else
					return super.toString(myHomeRegistry, new Scope(objArgs), args);
			}
			catch (Exception e) 
			{}

		return super.toString(myHomeRegistry, arg, prot, args);
	}
	
	@Override
	public Serializer getPreferredSerializer() 
	{
		return new JsonSerializer();
	}
}