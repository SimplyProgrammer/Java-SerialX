package examples.implementations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.junit.Test;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.json.JsonSerializer;
import org.ugp.serialx.juss.protocols.AutoProtocol;
import org.ugp.serialx.juss.protocols.SelfSerializableProtocol;
import org.ugp.serialx.protocols.SerializationProtocol;

import examples.Message;

/**
 * This example demonstrating SerialX Json capabilities...
 * 
 * @author PETO
 *
 * @since 1.3.8
 */
public class SerializingWithJson 
{
	public static final String[] NO_SCOPE = new String[] {"str", "date", "replies"};
	
	public static final String[] NO_FORMAT = new String[] {"str", "date", "mappedReplies"};
	
	@Test
	public void test() throws Exception // SelfSerializable
	{
		SerializingWithJson.main(null);
	}
	
	@Test
	public void test1() throws Exception // AutoProtocol
	{
		SerializingWithJson.main(NO_SCOPE);
	}
	
	@Test
	public void test2() throws Exception // AutoProtocol
	{
		SerializingWithJson.main(NO_FORMAT);
	}
	
	@Test
	public void test3() throws Exception // AutoProtocol
	{
		SerializingWithJson.main(new String[] {"str", "date"});
	}
	
	@Test
	public void test4() throws Exception // AutoProtocol
	{
		SerializingWithJson.main(new String[] {"str", "date", "replies"});
	}
	
	@Test
	public void test5() throws Exception // AutoProtocol
	{
		SerializingWithJson.main(new String[] {"str", "date", "mappedReplies"});
	}

	public static void main(String[] args) throws Exception 
	{
		LogProvider.instance.setReThrowException(true); // This is for testing purposes, so no error are allowed in this case, comment temporary when necessary... 

		SerializationProtocol.REGISTRY.add(
			SerializationProtocol.REGISTRY.removeIf(prot -> prot.applicableFor() == Message.class) ? // Change this accordingly while experimenting (add negation in front)...

			/*
			 * Registering AutoProtocol for Message class and setting it to serialize it as Scope, but feel free to experiment (except when args are NO_SCOPE in case you are running this as test)!
			 */
			new AutoProtocol<Message>(Message.class, args != NO_SCOPE /*true*/, args) 
			{
				/*
				 * This is not really necessary to do but in this case we are doing it to achieve a bit better efficiency/performance...
				 */
				public Message createBlankInstance(Class<? extends Message> objectClass) throws Exception 
				{
					if (objectClass == Message.class)
						return new Message(null, 0, null); // Faster
					return super.createBlankInstance(objectClass); // Slower...
				}
			} :
				
			/*
			 * Note that Message is also SelfSerializable which makes it eligible to be serialized with SelfSerializableProtocol as well!
			 * You can try this by changing the condition above, notice how data format will change slightly due to different serialization technique that SelfSerializable uses!
			 */
			new SelfSerializableProtocol<>(Message.class)
		);
		
		File medium = new File("src/examples/implementations/messages.json"); // Json file to use...
		
		// Content to serialize (list of Messages) 
		List<Message> messages = new ArrayList<>();
		messages.add(new Message("Hi there!", 1));
		messages.add(new Message("My name is Json.", 2));
		messages.add(new Message("And I am data format!", 3));
		if (Objects.deepEquals(args, new String[] {"str", "date", "mappedReplies"}))
		{
			messages.get(2).setMappedReplies(Scope.mapKvArray(new HashMap<>(), 
				"entry1", new Message("Hello to you as well from this map!", 12), 
				"entry2", new Message("Hello to you as well from the another map!", 13))
			);
		}
		else
		{
			messages.get(0).setReplies(new ArrayList<>());
			messages.get(1).setReplies(new ArrayList<>(Arrays.asList(new Message("Hello to you as well!", 11))));
		}
		
		/*
		 * Creating new JsonSerializer object and putting out Messages into it.
		 */
		Serializer serializer = new JsonSerializer(null, messages);
		if (args != NO_FORMAT)
			serializer.setFormat((byte)1);
		serializer.serializeTo(medium); // Serializing to given file.
		
		System.out.println("-------- Serialization complete! --------");
		
		/*
		 * Deserializing our Messages from given file.
		 */
		Serializer deserializer = JsonSerializer.from(medium);

		// Mapping deserialized Scopes, or "json objects" in this case, back into our original messages!
		List<Message> deserializedMessages = deserializer.map(jsonObj -> {
			try 
			{
				return ((Scope) jsonObj).toObject(Message.class);
			} 
			catch (Exception e) 
			{
				throw new RuntimeException(e);
			}
		});
		
		System.out.println("-------- Deserialization complete! --------");
		System.out.println(deserializedMessages + "\n");
		
		/* Test if original content was successfully serialized as well as deserialized! */
		if (Objects.deepEquals(args, new String[] {"str", "date"}))
		{
			for (int i = 0; i < messages.size(); i++)
			{
				assertTrue(deserializedMessages.get(i).getDate() == messages.get(i).getDate());
				assertTrue(Objects.equals(deserializedMessages.get(i).getStr(), messages.get(i).getStr()));
			}

			return;
		}
		
		assertEquals(messages, deserializedMessages);
	}
}
