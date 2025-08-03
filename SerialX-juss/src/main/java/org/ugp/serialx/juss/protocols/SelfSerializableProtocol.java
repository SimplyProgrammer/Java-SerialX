package org.ugp.serialx.juss.protocols;

/**
 * SelfSerializableProtocol is universal protocol to serialize any {@link SelfSerializable} instance. The condition of use is implementation of {@link SelfSerializable} interface and public constructor that can be called with content returned by specific {@link SelfSerializable#serialize()}!
 * 
 * @param <T> Type of SelfSerializable class that this protocol should be applicable for...
 * 
 * @author PETO
 *
 * @since 1.2.2	
 * 
 * @see UniversalObjectInstantiationProtocol
 */
public class SelfSerializableProtocol<T extends SelfSerializable> extends UniversalObjectInstantiationProtocol<T> 
{
	/**
	 * @param applicableFor | Class implementing {@link SelfSerializable} that can be serialized using this protocol.<br>
	 * Note: Passing <code>{@link SelfSerializable}.class</code> will make this protocol universal and work for any {@link SelfSerializable} instance, this can be considered a security risk in some cases...
	 * 
	 * @since 1.3.8
	 */
	public SelfSerializableProtocol(Class<T> applicableFor) 
	{
		super(applicableFor);
	}
	
	@Override
	public Object[] serialize(T object)
	{
		return object.serialize();
	}
	
	@Override
	public long getMode() 
	{
		return MODE_SERIALIZE_DESERIALIZE;
	}
}
 