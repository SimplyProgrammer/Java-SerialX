package org.ugp.serialx.juss.protocols;

/**
 * SelfSerializableProtocol is universal protocol to serialize any {@link SelfSerializable} instance. The condition of use is implementation of {@link SelfSerializable} interface and public constructor that can be called with content returned by specific {@link SelfSerializable#serialize()}!
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
	 * Note: Passing {@link SelfSerializable#getClass()} will make this protocol universal and work for any {@link SelfSerializable} instance, this can be considered unsafe in some cases...
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
	public byte getMode() 
	{
		return MODE_ALL;
	}
}
 