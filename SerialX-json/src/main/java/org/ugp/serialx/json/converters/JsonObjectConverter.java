package org.ugp.serialx.json.converters;

import java.io.IOException;
import java.util.Map;

import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.json.JsonSerializer;
import org.ugp.serialx.juss.converters.ObjectConverter;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.utils.Utils;

/**
 * Used internally by {@link JsonSerializer} to ensure proper and valid Json format for scopes and protocols.
 * 
 * @author PETO
 * 
 * @since 1.3.5 (separated from JsonSerializer since 1.3.8)
 */
public class JsonObjectConverter extends ObjectConverter
{		
	@SuppressWarnings("unchecked")
	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException 
	{
		if (obj == null)
			return CONTINUE;

		if (obj.getClass().isArray())
			return super.toString(source, myHomeRegistry, new Scope(Utils.fromAmbiguousArray(obj)), args);
		
		if (obj instanceof Map)
		{
			Map<?, ?> map = (Map<?, ?>) obj;
			if (map.isEmpty())
				return source.append("{}");
			if (map.keySet().iterator().next() instanceof CharSequence)
				return super.toString(source, myHomeRegistry, new Scope((Map<String, ?>) map), args);
		}
			
		SerializationProtocol<Object> prot = (SerializationProtocol<Object>) getProtocolFor(obj, SerializationProtocol.MODE_SERIALIZE, args);
		if (prot != null && !(obj instanceof Scope))
			try
			{
				Object[] objArgs = prot.serialize(obj);
				if (objArgs.length == 1 && objArgs[0] instanceof Scope)
					return super.toString(source, myHomeRegistry, objArgs[0], args);
				return super.toString(source, myHomeRegistry, new Scope(objArgs), args);
			}
			catch (Exception e) 
			{}

		return super.toString(source, myHomeRegistry, obj, prot, args);
	}
	
	@Override
	public Serializer getPreferredSerializer() 
	{
		return new JsonSerializer();
	}
}