package head;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ugp.org.SerialX.Serializer;

public class Main 
{
	public static void main(String[] args)
	{
		Serializer.PROTOCOL_REGISTRY.addAll(new Bar.BarProtocol(), new Foo.FooProtocol());
		
		File f = new File("./test.srlx");
		Random r = new Random();
		
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < 100; i++)
			list.add(r.nextBoolean() ? r.nextInt(i+1) : r.nextBoolean());

		Serializer.PROTOCOL_REGISTRY.GetProtocolFor(String.class).setActive(false);
		Serializer.SerializeTo(f, "145632asaa415644akhdjgxajcgj312345634hahaXDDDLol", r, list, new Bar(), 1, 2.2, 3, 'A', true, false, null, new int[] {1, 2, 3});
		Serializer.PROTOCOL_REGISTRY.setActivityForAll(true);
		System.out.println(Serializer.LoadFrom(f));
 	}

}
