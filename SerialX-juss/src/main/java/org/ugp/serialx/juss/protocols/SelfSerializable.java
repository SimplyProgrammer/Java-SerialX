package org.ugp.serialx.juss.protocols;

/**
 * This is based on pretty similar concept as regular {@link java.io.Serializable} is! However this interface is meant to create its instance programmatically via constructor!
 * So condition of using this is that array of objects returned by {@link SelfSerializable#serialize()} must be applicable for some public constructor of certain class implementing this!
 * Specific instances of this interface will be created by calling that public constructor! This is done reflectively by {@link SelfSerializableProtocol}!
 * 
 * @author PETO
 *
 * @since 1.2.2
 */
public interface SelfSerializable
{
	/**
	 * @return Array of objects that can be applied to certain public constructor of this class! This constructor will be then used as unserialize method and will be called during unserialization! 
	 * 
	 * @since 1.2.2
	 */
	public Object[] serialize();
}
