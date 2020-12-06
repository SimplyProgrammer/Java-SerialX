package head;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import ugp.org.SerialX.Protocols.SerializationProtocol;

public class Foo //Sample object
{
	int a = 8, b = 1, c = 456;
	double d = 5;
	float f = 1453.364564564132454654511324f;
	char ch = 'l';
	String s = "a"; 
	boolean nah = false;
	List<Object> l = new CopyOnWriteArrayList<Object>(Arrays.asList(6, 45,464654, 9.9, 56f));
	
	public Foo() 
	{
		l.add(6);
		l.add(9);
		l.add(13);
		l.add(new Random());
		l.add(new ArrayList<>(new ArrayList<>(new ArrayList<>(Arrays.asList(4, 5,6d)))));
	}
	
	@Override
	public String toString()
	{
		return "Foo[" + a + " " + b + " " + c + " " + d + " " + f + " " + ch + " " + s + " " + nah + " " + l + "]";
	}
	
	public static class FooProtocol extends SerializationProtocol<Foo> //Protocol to serialize Foo
	{
		@Override
		public Object[] serialize(Foo object) 
		{
			return new Object[] {};
		}

		@SuppressWarnings("unchecked")
		@Override
		public Foo unserialize(Class<? extends Foo> objectClass, Object... args) 
		{
			Foo f = new Foo();
			f.a = (int) args[0];
			f.b = (int) args[1];
			f.c = (int) args[2];
			f.d = (double) args[3];
			f.f = (float) args[4];
			f.ch = (char) args[5];
			f.s = (String) args[6];
			f.nah = (boolean) args[7];
			f.l = (List<Object>) args[8];
			
			return f;
		}		
		
		@Override
		public Class<? extends Foo> applicableFor()
		{
			return Foo.class;
		}
	};
}
