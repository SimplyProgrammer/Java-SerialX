package examples;

import org.ugp.serialx.SerializationDebugger;
import org.ugp.serialx.protocols.SelfSerializable;

/**
 * Example of self-serializable object!
 * SelfSerializable objects can be serialized directly without necessity of having any {@link SerializationDebugger}, all you need to do is implement {@link SelfSerializable} interface and override {@link SelfSerializable#serialize()} method accordingly!
 * 
 * @author PETO
 *
 * @see SelfSerializable
 *
 * @since 1.3.2
 */
public class Message implements SelfSerializable 
{
	public String str;
	public int date;
	
	public Message(String str, int date) 
	{
		this.str = str;
		this.date = date;
	}
	
	@Override
	public String toString() {
		return "Message["+str+", "+date+"]";
	}
	
	@Override
	public Object[] serialize() 
	{
		return new Object[] {str, date};
	}
}
