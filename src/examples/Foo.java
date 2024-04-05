package examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ugp.serialx.protocols.SerializationProtocol;

public class Foo //Sample object to be serialized using its protocol!
{
	int a = 8, b = 1, c = 456;
	double d = 5;
	float f = 1453.364564564132454654511324f;
	char ch = 'l';
	String s = "a"; 
	boolean nah = false;
	List<Object> l = new CopyOnWriteArrayList<Object>();
	
	public Foo() 
	{
		l.add(6);
		l.add(9);
		l.add(13);
		l.add(new HashMap<>());
		l.add(new ArrayList<>(Arrays.asList(4, 5, 6d, new ArrayList<>(), "hi")));
	}
	
	@Override
	public String toString()
	{
		return "Foo[" + a + " " + b + " " + c + " " + d + " " + f + " " + ch + " " + s + " " + nah + " " + l + "]";
	}
	

	@Override
	public boolean equals(Object obj) {
		Foo other = (Foo) obj;
		return a == other.a && b == other.b && c == other.c && ch == other.ch
				&& Double.doubleToLongBits(d) == Double.doubleToLongBits(other.d)
				&& Float.floatToIntBits(f) == Float.floatToIntBits(other.f) && l.equals(other.l)
				&& nah == other.nah && s.equals(other.s);
	}

	public static class FooProtocol extends SerializationProtocol<Foo> //Protocol to serialize Foo
	{
		@Override
		public Object[] serialize(Foo object) 
		{
			return new Object[] {object.a, object.b, object.c, object.d, object.f, object.ch, object.s, object.nah, object.l};
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
	}
	
	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	public char getCh() {
		return ch;
	}

	public void setCh(char ch) {
		this.ch = ch;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public boolean isNah() {
		return nah;
	}

	public void setNah(boolean nah) {
		this.nah = nah;
	}

	public List<Object> getL() {
		return l;
	}

	public void setL(List<Object> l) {
		this.l = l;
	};
	
	public static void a() {};
}
