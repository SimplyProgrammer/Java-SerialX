package head;

import java.util.List;

import ugp.org.SerialX.Serializer;

public final class Bar extends Foo //Sample object that inheres
{
	byte by0 = (byte) 142; 
	short s0 = 555;
	double d2 = 5;
	Object sampleParent;
	
	@Override
	public String toString() 
	{
		return "Bar[" + a + " " + b + " " + c + " " + d + " " + f + " " + ch + " " + s + " " + nah + " " + l + " " + by0 + " " + s0 + "]";
	}
	
	public static class BarProtocol extends FooProtocol //Protocol to serialize Bar
	{
		@Override
		public Object[] serialize(Foo object) 
		{
			return new Object[] {object.a, object.b, object.c, object.d, object.f, object.ch, object.s, object.nah, object.l, ((Bar) object).by0, ((Bar) object).s0, Serializer.Code("$parent")};
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
			f.by0 = (byte) args[9];
			f.s0 = (short) args[10];
			f.sampleParent = args[11];
			
			return f;
		}		
		
		@Override
		public Class<? extends Foo> applicableFor()
		{
			return Bar.class;
		}
	}
	
	public byte getBy0() {
		return by0;
	}

	public void setBy0(byte by0) {
		this.by0 = by0;
	}

	public short getS0() {
		return s0;
	}

	public void setS0(short s0) {
		this.s0 = s0;
	}

	public double getD2() {
		return d2;
	}

	public void setD2(double d2) {
		this.d2 = d2;
	}

	public Object getSampleParent() {
		return sampleParent;
	}

	public void setSampleParent(Object sampleParent) {
		this.sampleParent = sampleParent;
	}
}
