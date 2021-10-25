package examples;

import org.ugp.serialx.protocols.SelfSerializable;

/**
 * Example of self-serializable object!
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
