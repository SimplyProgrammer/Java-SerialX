package org.ugp.serialx.converters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.protocols.SerializationProtocol;

/**
 * This converter is capable of converting {@link Serializable}.
 * Its case sensitive!
 * <br>
 * <br>
 * Table of sample string <--> object conversions:
	<style>
		table, th, td 
		{
		  border: 1px solid gray;
		}
	</style>
	<table>
		<tr>
		    <th>String</th>
		    <th>Object</th> 
		</tr>
		<tr>
		    <td>#rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAAdwQAAAAAeA#3D#3D</td>
		    <td>new ArrayList()</td>
	  	</tr>
	  	<tr>
		    <td>#rO0ABXVyAAJbSU26YCZ26rKlAgAAeHAAAAADAAAABQAAAAUAAAAF</td>
		    <td>new int[] {5, 5, 5}</td>
	  	</tr>
	</table>
 * Note: In most of the cases {@link ObjectConverter} and {@link SerializationProtocol} should be used as preferable alternative!<br>
 * Note: In order to avoid conflicts with JUSS syntax, this converter serializes and deserializes by using {@link Base64} and {@link URLEncoder} with addition of all % are being replaced by #!
 * 
 * @author PETO
 * 
 * @since 1.3.0
 */
public class SerializableBase64Converter implements DataConverter 
{
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args) 
	{
		if (arg.length() <= 0)
			return CONTINUE;
		
		char ch0 = arg.charAt(0);
		if (ch0 == '#' || ch0 == 'r')
		{
			try
			{
				if (ch0 == '#')
					return UnserializeClassis(arg = URLDecoder.decode(arg.substring(1).replace('#', '%'), "UTF-8"));
				return UnserializeClassis(arg = URLDecoder.decode(arg.replace('#', '%'), "UTF-8"));
			}
			catch (Exception e) 
			{
				LogProvider.instance.logErr("Looks like there appear some problems with unserializing some object, the instance of java.io.Serializable from string \"" + arg + "\"! This string is most likely corrupted! See error below:", e);
				e.printStackTrace();
				return null;
			}
		}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object arg, Object... args) 
	{
		if (arg instanceof Serializable)
			try
			{
				return "#" + URLEncoder.encode(SerializeClassic((Serializable) arg), "UTF-8").replace('%', '#');
			}
			catch (Exception e)
			{
				LogProvider.instance.logErr("Looks like there appear some problems with serializing \"" + arg + "\", the instance of java.io.Serializable. This could happen when certain object contains non-transient unserializable objects. Use custom valid protocol for serializing \"" + arg + "\" might solve the problem!", e);
				e.printStackTrace();
				return null;
			}
		return CONTINUE;
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		return new StringBuilder("Object of ").append(obj.getClass().getName()).append(": \"").append(obj).append("\" serialized using classic Base64 java.io.Serializable!");
	}
	
	/**
	 * @param objStr | String to unserialize by classic Java serialization.
	 * 
	 * @return Unsrialized object.
	 * 
	 * @throws IOException - if an I/O error occurs while reading stream header.
	 * @throws ClassNotFoundException - Class of a serialized object cannot be found. 
	 * 
	 * @see java.util.Base64
	 * 
	 * @since 1.0.0 (moved to {@link SerializableBase64Converter} since 1.3.0)
	 */
	public static Object UnserializeClassis(String objStr) throws Exception
	{
        return new ObjectInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(objStr))).readObject();
	}
	
	/**
	 * @param obj | Object to serialize using classic Java serialization.
	 * 
	 * @return String with serialized object.
	 * 
	 * @throws Exception - if an I/O error occurs while writing stream header
	 * 
	 * @see java.lang.Base64
	 * 
	 * @since 1.0.0 (moved to {@link SerializableBase64Converter} since 1.3.0)
	 */
	public static String SerializeClassic(Serializable obj) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        oos.writeObject(obj);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray()); 
	}
}
