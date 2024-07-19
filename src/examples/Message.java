package examples;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.ugp.serialx.juss.protocols.SelfSerializable;

/**
 * Example of self-serializable object!
 * {@link SelfSerializable} objects can be serialized directly without necessity of having any {@link org.ugp.serialx.devtools.SerializationDebugger}, all you need to do is implement {@link SelfSerializable} interface and override {@link SelfSerializable#serialize()} method accordingly!
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
	
	protected List<Message> replies;
	protected HashMap<String, Message> mappedReplies;

	protected transient Object rand = new Random();
	
	public Message(String str, int date) 
	{
		this(str, date, (List<Message>) null);
	}

	public Message(String str, int date, List<Message> replies) 
	{
		setStr(str);
		setDate(date);
		setReplies(replies);
	}

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		return date == other.date && 
				Objects.equals(str, other.str) && 
				mappedReplies == null ? Objects.deepEquals(replies, other.replies) : 
					Objects.deepEquals(mappedReplies, other.mappedReplies);
	}

	@Override
	public String toString() 
	{
		return "Message["+str+", "+date+", "+(mappedReplies == null ? replies : mappedReplies)+"]";
	}
	
	@Override
	public Object[] serialize() 
	{
		return new Object[] {str, date, replies};
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

	public List<Message> getReplies() {
		return replies;
	}

	public void setReplies(List<Message> replies) {
		this.replies = replies;
	}

	public HashMap<String, Message> getMappedReplies() {
		return mappedReplies;
	}

	public void setMappedReplies(HashMap<String, Message> mappedReplies) {
		this.mappedReplies = mappedReplies;
	}

	public Object getRand() {
		return rand;
	}

	public void setRand(Object rand) {
		this.rand = rand;
	}
}
