package examples.implementations;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Utils;
import org.ugp.serialx.converters.StringConverter;
import org.ugp.serialx.devtools.SerializationDebugger;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.juss.converters.ObjectConverter;
import org.ugp.serialx.protocols.SerializationProtocol;

import examples.Bar;
import examples.Foo;

/**
 * This example is overview of general SerialX API functionalities!
 * We will look at how to serialize and deserialize objects using file. We will also create protocols for our objects as well as for already existing ones!
 * This example is also for testing and benchmarking!
 * 
 * @author PETO
 * 
 * @since 1.0.0
 */
public class GeneralExample 
{
	//Test constants...
	public static final String TEST_1 = "father";
	public static final String TEST_2 = "has an event horizon... //lol";
	
	public static final String TEST_3 = "some string";
	public static double TEST_4 = 5;
	public static Scope TEST_5 = new Scope();
	public static final String TEST_6 = "HELLO_WORLD";
	
	@Test
	public void test() throws Exception 
	{
		GeneralExample.main(new String[0]);
	}
	
	public static void main(String[] args) throws Exception
	{
		//------------------------------------------- Custom protocol registration -------------------------------------------
		
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
		
		File f = new File("src/examples/implementations/test.juss"); //File to write and read from!

		//------------------------------------------- Generating mock data -------------------------------------------
		
		Random r = new Random(123);
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < 10; i++)
			list.add(r.nextBoolean() ? r.nextInt(i+1) : r.nextBoolean());
		list.add(new LinkedList<>(list));

		HashMap<String, Object> vars = new HashMap<>(); //Variables to serialize
		vars.put("yourMom", TEST_2);
		vars.put("num", 6);
		
		int[][] ints = {{1, 2, 3}, {4, 5, 4}, {3, 2, 1}};
		
		Scope someScope = new Scope(111, 222, new Scope(new ArrayList<>(Arrays.asList("some", "elements", "...", new Scope('c', TEST_5)))));
		Scope neastedScope1 = new Scope(), neastedScope2 = new Scope();
		neastedScope2.put("tst4", TEST_4);
		neastedScope2.add(StringConverter.DirectCode("$num"));
		neastedScope1.put("neastedTest", neastedScope2);
		someScope.put("test", neastedScope1);

		//------------------------------------------- Serializing -------------------------------------------
		
		LogProvider.instance.setReThrowException(true); // This is for testing purposes, so no error are allowed in this case, comment temporary when necessary... 
		
		JussSerializer.JUSS_PARSERS.get(ObjectConverter.class).setAllowStaticMemberInvocation(true); //This is necessary since 1.3.7
		
		JussSerializer serializer = new JussSerializer(vars); //Creating an instance of Serializer that will serialize objects using Juss! Serializer is instance of scope so it behaves like so!										   
		//Adding independent values																		         																															Invokation of static members of this class (calling method "println" and obtaining "hello" field as argument! 
		serializer.addAll(TEST_3, r, list, ints, someScope, serializer.Comment("Size of array"), serializer.Var("arrSize", list.size()), new Bar(serializer.Code("$parent")), 1, 2.2, 3, 'A', true, false, null, serializer.Code("$num::new"), serializer.StaticMember(GeneralExample.class, "println", serializer.StaticMember(GeneralExample.class, "hello")));
											    			//This will insert an comment          Another way to add variable except put method			     				   									$ is used to obtain value from variable, ::new will attempt to clone the value
		serializer.setGenerateComments(true); //Enabling comment generation
		
		serializer.getParsers().resetCache(); //Enabling cache, this can improve performance when serializing a lot of data (not case of this example)!

		double t0 = System.nanoTime();			
		serializer.SerializeTo(f); //Saving content of serializer to file (serializing)
		double t = System.nanoTime();						  
		System.out.println("Write: " + (t-t0)/1000000 + " ms"); //Write benchmark
		
		//------------------------------------------- Deserializing -------------------------------------------
		SerializationProtocol.REGISTRY.setActivityForAll(true); //Enabling all protocols, just in case...

		JussSerializer deserializer = new JussSerializer(); //Creating instance of Serializer that will deserialize objects serialized in Juss (same class is responsible for serializing and deserializing)!
		deserializer.setParsers(JussSerializer.JUSS_PARSERS_AND_OPERATORS); //Doing this will allow us to use operators from org.ugp.serialx.converters.operators while deserializing!
		deserializer.put("parent", TEST_1); //Setting global variables
		
		deserializer.getParsers().resetCache(); //Enabling cache, this can improve performance when serializing a lot of data (not case of this example)!
		
		deserializer = SerializationDebugger.debug(deserializer); //Enabling debugging for deserialization!
		
		t0 = System.nanoTime();
		deserializer.LoadFrom(f); //Loading content of file in to deserializer!
		t = System.nanoTime();
		System.out.println("Read: " + (t-t0)/1000000 + " ms"); //Read benchmark

		deserializer = (JussSerializer) deserializer.filter(obj -> obj != null); //This will filter away every null value and variable!

		//Printing values and variables of scope!
		System.out.println(deserializer.variables());
		System.out.println(deserializer.values());
		
		//Performing test
		assertEquals(TEST_1, deserializer.getString("parent"));
		assertEquals(TEST_2, deserializer.getString("yourMom"));
		assertEquals(list.size(), deserializer.getInt("arrSize"));

		assertEquals(TEST_4, deserializer.getScope(4).getScope("neastedTest").getDouble("tst4"), 0);
		assertEquals(deserializer.getScope(4).getScope(Utils.splitValues("test  neastedTest", ' ')).getParent(2), deserializer.getScope(4));
		assertEquals(((Scope) deserializer.getScope(4).getSubScope(0).<List<?>>get(0).get(3)).getSubScope(0).toObject(List.class).size(), TEST_5.into(Collection.class).size());
		assertTrue(deserializer.clone() instanceof JussSerializer);
		assertTrue(deserializer.filter(obj -> obj.equals(true)).get(0));
		
		assertEquals(TEST_3, deserializer.get(0));
		assertEquals(list, deserializer.get(2));
		assertEquals(new Bar(TEST_1), deserializer.<Object>get(5));
		assertArrayEquals(ints, Scope.from(deserializer.get(3)).toValArray());
		
		assertEquals(TEST_6, new Scope(deserializer).getString(-1));
 	}
	
	//We can invoke static members in JUSS!
	public static String hello = "Hello world!";
	
	public static String println(String str)
	{
		System.out.println(str);
		return TEST_6;
	}
}