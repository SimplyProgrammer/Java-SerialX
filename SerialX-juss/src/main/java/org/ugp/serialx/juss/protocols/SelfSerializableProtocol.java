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
public class SelfSerializableProtocol extends UniversalObjectInstantiationProtocol<SelfSerializable> 
{
	/**
	 * @param applicableFor | Class implementing {@link SelfSerializable} that can be serialized using this protocol.<br>
	 * Note: Passing {@link SelfSerializable#getClass()} will make this protocol universal and work for any {@link SelfSerializable} instance, this can be considered unsafe in some cases...
	 * 
	 * @since 1.3.7
	 */
	public SelfSerializableProtocol(Class<? extends SelfSerializable> applicableFor) 
	{
		super(applicableFor);
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
 