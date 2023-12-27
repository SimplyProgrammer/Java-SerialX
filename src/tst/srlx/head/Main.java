package tst.srlx.head;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.ugp.serialx.Scope;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.protocols.SerializationProtocol;

import examples.Bar;
import examples.Foo;

/**
 * This example is overview of general SerialX API functionalities!
 * We will look at how to serialize and deserialize objects using file. We will also create protocols for our objects as well as for already existing ones!
 * This example is also for benchmarking!
 * 
 * @author PETO
 * 
 * @since 1.0.0
 */
public class Main 
{
	public static void main(String[] args) throws Exception
	{
		//Protocol registration
		SerializationProtocol.REGISTRY.addAll(new Bar.BarProtocol(), new Foo.FooProtocol(), new SerializationProtocol<Random>() //Sample custom protocol to serialized Random. 
		{																													    //Random will be serialized also without protocol via classic Java Base64 because it implements java.io.Serializable!
			@Override
			public Object[] serialize(Random object) 
			{
				try
				{
					Field f = Random.class.getDeclaredField("seed");
					f.setAccessible(true);
					return new Object[] {((AtomicLong) f.get(object)).get()};
				}
				catch (Exception e) 
				{
					e.printStackTrace();
					return new Object[] {-1};
				}
			}

			@Override
			public Random unserialize(Class<? extends Random> objectClass, Object... args) 
			{
				return new Random(((Number) args[0]).longValue());
			}

			@Override
			public Class<? extends Random> applicableFor() 
			{
				return Random.class;
			}
		});
		
		File f = new File("test.juss"); //File to write and read from!

		//Sample objects
		Random r = new Random();
		List<Object> list = new ArrayList<>();
		for (int i = 1; i <= 8000000; i++)
			list.add( r.nextInt(i));

		HashMap<String, Object> vars = new HashMap<>(); //Variables to serialize
		vars.put("yourMom", "is heavier than sun...");
		vars.put("num", 6);
		
		int[][] ints = {{1, 2, 3}, {4, 5, 4}, {3, 2, 1}};
		
		//-------------------------------------------Serializing-------------------------------------------
		
		JussSerializer serializer = new JussSerializer(vars); //Creating an instance of Serializer that will serialize objects using Juss! Serializer is instance of scope so it behaves like so!										   
		//Adding independent values																		         																										 Invokation of static members of this class (calling method "println" and obtaining "hello" field as argument! 
		serializer.addAll(list /*serializer.Comment("Size of array"), serializer.Var("arrSize", list.size()), new Bar(), 1, 2.2, 3, 'A', true, false, null, ints, serializer.Code("$num"), new Scope(), serializer.StaticMember(Main.class, "println", serializer.StaticMember(Main.class, "hello"))*/);
											    //This will insert an comment          Another way to add variable except Map<String, Object> 				     				   $ is used to obtain value from variable
//		serializer.setGenerateComments(true); //Enabling comment generation
		
//		serializer.getParsers().resetCache();

		double t0 = System.nanoTime();																										   
//		serializer.SerializeTo(f); //Saving content of serializer to file (serializing)
		double t = System.nanoTime();						  
		System.out.println("Write: " + (t-t0)/1000000 + " ms"); //Write benchmark
		
		//-------------------------------------------Deserializing-------------------------------------------
		
		SerializationProtocol.REGISTRY.setActivityForAll(true); //Enabling all protocols, just in case...
		
		JussSerializer deserializer = new JussSerializer(); //Creating instance of Serializer that will deserialize objects serialized in Juss (same class is responsible for serializing and deserializing)!
		deserializer.setParsers(JussSerializer.JUSS_PARSERS_AND_OPERATORS);
		deserializer.put("parent", "father"); //Setting global variables

//		deserializer.getParsers().resetCache();
		
		t0 = System.nanoTime();
		deserializer.LoadFrom(f); //Loading content of file in to deserializer!
		t = System.nanoTime();
		System.out.println("Read: " + (t-t0)/1000000 + " ms"); //Read benchmark

		//deserializer = (JussSerializer) deserializer.filter(obj -> obj != null); //This will filter away every null value and variable!

		//Printing values and variables of scope!
		System.out.println(deserializer.variables());
		System.out.println(deserializer.values());
 	}
	
	//We can invoke static members in JUSS!
	public static String hello = "Hello world!";
	
	public static void println(String str)
	{
		System.out.println(str);
	}
}