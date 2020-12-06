package head;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import ugp.org.SerialX.Serializer;
import ugp.org.SerialX.Protocols.SerializationProtocol;

public class Main 
{
	public static void main(String[] args)
	{
		//Protocol registration
		Serializer.PROTOCOL_REGISTRY.addAll(new Bar.BarProtocol(), new Foo.FooProtocol(), new SerializationProtocol<Random>() //Sample custom protocol to serialized Random. 
		{																													  //Random will be serialized also without protocol via classic Java Base64 because it implements java.io.Serializable!
			@Override
			public Object[] serialize(Random object) 
			{
				long seed = 0;
				try
				{
					Field f = Random.class.getDeclaredField("seed");
					f.setAccessible(true);
					seed = ((AtomicLong) f.get(object)).get();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				return new Object[] {seed};
			}

			@Override
			public Random unserialize(Class<? extends Random> objectClass, Object... args) 
			{
				return new Random((long) args[0]);
			}

			@Override
			public Class<? extends Random> applicableFor() 
			{
				return Random.class;
			}
		});
		
		File f = new File("./test.srlx");
		
		//Sample objects
		Random r = new Random();
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < 10; i++)
			list.add(r.nextBoolean() ? r.nextInt(i+1) : r.nextBoolean());
		int[] intArr = {1, 2, 3, 4};

		HashMap<String, Object> vars = new HashMap<>(); //Variables to serialize
		vars.put("yourMom", "is heavier than sun...");
		vars.put("num", 6);
		
		Serializer.generateComments = true; //Enabling comment generation
		
		Serializer.globalVariables.put("parent", "father"); //Setting global variables
		
		Serializer.PROTOCOL_REGISTRY.GetProtocolFor(String.class).setActive(false); //Disabling a string protocol. This will force Serializer to serialize string with regular Java Base64 because String implements java.io.Serializable!
		Serializer.SerializeTo(f, vars, "145asaa4144akhdgj31hahaXDDLol", r, list, Serializer.Comment("Size of array"), Serializer.Var("arrSize", list.size()), new Bar(), 1, 2.2, 3, 'A', true, false, null, intArr, Serializer.Code("$num")); //Saving to file (serializing)
							                                  //This will insert an comment        Another way to add variable except Map<String, Object> 				             $ is used to obtain value from variable
		
		Serializer.PROTOCOL_REGISTRY.setActivityForAll(true); //Enabling all protocols
		System.out.println(Serializer.LoadFrom(f)); //Loading from file
		System.out.println(Serializer.LoadVariablesFrom(f)); //Loading variables from file
 	}

}
