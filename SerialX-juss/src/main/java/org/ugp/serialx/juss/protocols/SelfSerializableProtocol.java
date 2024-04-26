package org.ugp.serialx.protocols;

/**
 * SelfSerializableProtocol is universal protocol to serialize any {@link SelfSerializable} instance. The condition of use is implementation of {@link SelfSerializable} interface and public constructor that can be called with content returned by specific {@link SelfSerializable#serialize()}!
 * 
 * @author PETO
 *
 * @since 1.2.2	
 * 
 * @see UniversalObjectInstantiationProtocol
 */
public class SelfSerializableProtocol extends UniversalObjectInstantiationProtocol<SelfSerializable> 
{
	public SelfSerializableProtocol() 
	{
		super(SelfSerializable.class);
	}
	
	@Override
	public Object[] serialize(SelfSerializable object) 
	{
		return object.serialize();
	}
	
	@Override
	public byte getMode() 
	{
		return MODE_ALL;
	}
}
