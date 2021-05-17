package head;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.javafx.css.parser.CSSParser;

import ugp.org.SerialX.Scope;
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

		HashMap<String, Object> vars = new HashMap<>(); //Variables to serialize
		vars.put("yourMom", "is heavier than sun...");
		vars.put("num", 6);
		
		Serializer.generateComments = true; //Enabling comment generation
		
		Serializer.globalVariables.put("parent", "father"); //Setting global variables
		
		double t0 = System.nanoTime();																										   //Invokation of static members of this class (calling method "println" and obtaining "hello" field as argument! 
		Serializer.SerializeTo(f, vars, "145asaa4144akhdgj31hahaXDDLol", r, list, Serializer.Comment("Size of array"), Serializer.Var("arrSize", list.size()), new Bar(), 1, 2.2, 3, 'A', true, false, null, Serializer.Code("$num"), new Scope(), Serializer.StaticMember(Main.class, "println", Serializer.StaticMember(Main.class, "hello"))); //Saving to file (serializing)
		double t = System.nanoTime();						  //This will insert an comment        Another way to add variable except Map<String, Object> 				     $ is used to obtain value from variable
		System.out.println("Write: " + (t-t0)/1000000);
		
		Serializer.PROTOCOL_REGISTRY.setActivityForAll(true); //Enabling all protocols
		t0 = System.nanoTime();
		Scope scope = Serializer.LoadFrom(f); //Loading scope with variables and values from file!
		t = System.nanoTime();
		System.out.println("Read: " + (t-t0)/1000000);
		
		scope = scope.filter(obj -> obj != null); //This will filter away every null value and variable!
		
		System.out.println(scope.toVarMap()); 
		System.out.println(scope.toValList());
		
		System.out.println((Object)Serializer.LoadFrom(f, 1));
 	}
	
	//We can invoke static things from SerialX!
	
	public static String hello = "Hello world!";
	
	public static void println(String str)
	{
		System.out.println(str);
	}
}
