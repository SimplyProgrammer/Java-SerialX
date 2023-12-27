package examples;

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
	protected String str;
	protected long date;
	
	public Message(String str, int date) 
	{
		this.str = str;
		this.date = date;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		return date == other.date && str.equals(other.str);
	}

	@Override
	public String toString() {
		return "Message["+str+", "+date+"]";
	}
	
	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	@Override
	public Object[] serialize() 
	{
		return new Object[] {str, date};
	}
}
