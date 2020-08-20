package head;

import java.util.List;

public final class Bar extends Foo
{
	byte by0 = (byte) 142; 
	short s0 = 555;
	
	public Bar(Object... args) {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() 
	{
		return "Bar[" + a + " " + b + " " + c + " " + d + " " + f + " " + ch + " " + s + " " + nah + " " + l + " " + by0 + " " + s0 + "]";
	}
	
	public static class BarProtocol extends FooProtocol
	{
		@Override
		public Object[] serialize(Foo object) 
		{
			return new Object[] {object.a, object.b, object.c, object.d, object.f, object.ch, object.s, object.nah, object.l, ((Bar) object).by0, ((Bar) object).s0};
		}

		@SuppressWarnings("unchecked")
		@Override
		public Foo unserialize(Class<? extends Foo> objectClass, Object... args) 
		{
			Bar f = new Bar();
			f.a = (int) args[0];
			f.b = (int) args[1];
			f.c = (int) args[2];
			f.d = (double) args[3];
			f.f = (float) args[4];
			f.ch = (char) args[5];
			f.s = (String) args[6];
			f.nah = (boolean) args[7];
			f.l = (List<Object>) args[8];
			f.by0 = ((Number) args[9]).byteValue();
			f.s0 = ((Number) args[10]).shortValue();
			
			return f;
		}		
		
		@Override
		public Class<? extends Foo> applicableFor()
		{
			return Bar.class;
		}
	}
}
