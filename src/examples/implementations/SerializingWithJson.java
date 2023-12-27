package examples.implementations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.json.JsonSerializer;
import org.ugp.serialx.protocols.AutoProtocol;
import org.ugp.serialx.protocols.SerializationProtocol;

import examples.Message;

/**
 * This example demonstrating SerialX Json capabilities...
 * 
 * @author PETO
 *
 * @since 1.3.7
 */
public class SerializingWithJson {

	public static void main(String[] args) throws Exception 
	{
		/*
		 * Registering AutoProtocol for Mesasge class and setting it to serialize it as Scope!
		 * 
		 * Note: You can see that we are getting "ProtocolRegistry: Protocol applicable for "examples.Message" is already registered!" this is because Message already implements SelfSerializable which means it can be already serialized via SelfSerializableProtocol.
		 * Registering additional Serialization protocol is therefore not necessary, we are doing this only to enforce the Scope format.
		 */
		SerializationProtocol.REGISTRY.add(new AutoProtocol<>(Message.class, true));
		
		File medium = new File("src/examples/implementations/messages.json"); // Json file to use...
		
		// Content to serialzie (list of Messages) 
		List<Message> messages = new ArrayList<>();
		messages.add(new Message("Hi!", 1));
		messages.add(new Message("My name is Json.", 2));
		messages.add(new Message("And I am data format!", 3));
		
		/*
		 * Creating new JsonSerializer object and putting out Messages into it.
		 */
		Serializer serializer = new JsonSerializer(null, messages);
		serializer.SerializeTo(medium); // Serializing to given file.
		
		System.out.println("-------- Serialized sucessfully! --------");
		
		/*
		 * Deserializing our Messages from given file.
		 */
		Serializer deserializer = JsonSerializer.from(medium);
		
		// Mapping deserialized Scopes, or "json objects" in this case, back into our original messages!
		List<Message> deserializedMessages = deserializer.map(jsonObj -> {
			try {
				return ((Scope) jsonObj).toObject(Message.class);
			} 
			catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		});
		
		System.out.println("-------- Deserialized sucessfully! --------");
		
		System.out.println(messages.equals(deserializedMessages)); // True means that original content was successfully serialized as well as deserialized!
	}
}
