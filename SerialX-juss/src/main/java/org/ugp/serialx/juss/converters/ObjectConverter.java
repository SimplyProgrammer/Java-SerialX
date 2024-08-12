package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.Utils.indexOfNotInObj;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Base64;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.ProtocolConverter;
import org.ugp.serialx.converters.SerializableBase64Converter;
import org.ugp.serialx.converters.imports.ImportsProvider;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

/**
 * This converter is capable of converting any Object using {@link SerializationProtocol} as well as invoking static functions!
 * This is also responsible for {@link Scope}!
 * Its case sensitive!
 * <br>
 * <br>
 * Table of sample string <--> object conversions:
 * 	<style>
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
		    <td>ArrayList 2 4 6</td>
		    <td>new ArrayList<>(Arrays.asList(2, 4, 6))</td>
	  	</tr>
		<tr>
		    <td>{ ... }</td>
		    <td>new Scope( ... )</td>
	  	</tr>
		<tr>
		    <td>java.lang.Math::max 10 5</td>
		    <td>10</td>
	  	</tr>
	</table>
	Note: Be aware that invocation of static members such as functions and fields (:: operator) is disabled by default for the sake of security...<br> 
	<br>
	This parser requires additional parser arg at index 0 of type {@link GenericScope} or {@link Serializer} that will be used for further parsing and operating (default new {@link JussSerializer}).<br>
	This parser requires additional parser arg at index 3 of type {@link ProtocolRegistry} or {@link SerializationProtocol} itself that will be used for parsing protocol expressions (default {@link SerializationProtocol#REGISTRY}).<br>
 *  This parser will insert one additional argument into array of additional parser args at index 4, in case of serialization index 5, that will be of type {@link Class} and it will contains information about class of object that is being unserialized or serialized using protocol!</br>
 *  
 * @author PETO
 * 
 * @since 1.3.0
 */
public class ObjectConverter extends ProtocolConverter 
{
	/**
	 * Set this on true to force program to use {@link Base64} serialization on {@link Serializable} objects.
	 * Doing this might result into some form of encryption but its less flexible and tends to be slower than SerialX {@link SerializationProtocol} system!
	 * In some cases, java Serialization can be more effective than protocols sometimes not! You should try which gives you the best result, then you can also deactivate certain protocols that are less effective than Java serialization.
	 * For example for long strings, classic Java serialization is better than protocol, it will take less memory storage space, but performance is almost always far slower!<br>
	 * Note: Whole concept of SerialX API is about avoiding classic Java serialization from many reasons so you most likely want this on true! Also protocol will be almost certainly faster classic serialization!<br>
	 * Note: This will only work when this converter is registered in {@link ParserRegistry} together with {@link SerializableBase64Converter}!
	 * 
	 * @since 1.0.0 (moved to {@link SerializableBase64Converter} since 1.3.0 and since 1.3.5 into {@link ObjectConverter})
	 */
	protected boolean useBase64IfCan = false;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... compilerArgs) 
	{
		int len;
		if ((len = str.length()) > 0)
		{
			boolean hasOp, hasCls;
			if ((hasOp = (str.charAt(0) | ' ') == '{') && (hasCls = (str.charAt(len-1) | ' ') == '}')) // Unwrap if wrapped in {}
				len = (str = str.substring(1, --len).trim()).length();
			else
				hasCls = false;
			
			Class<?> objClass;
			int chI;
//			if (((chI = indexOfNotInObj(str, '=', ':', ';', ',')) == -1 || indexOfNotInObj(str, chI, len, -1, true, "==", "::") != -1) && (objClass = getProtocolExprClass(str, compilerArgs)) != null)
			if (((chI = indexOfNotInObj(str, '=', ':', ';', ',')) == -1 || ++chI < len && str.charAt(chI) == ':' && indexOfNotInObj(str, ++chI, len, -1, true, ';', ',', '=') == -1) && (objClass = getProtocolExprClass(str, compilerArgs)) != null) // Is protocol expr in disguise (I know I know but come up with something better before judging...)
				return parse(myHomeRegistry, objClass, str, compilerArgs);
			
			if (hasOp && hasCls) //Is scope
			{
				Serializer scope;
				try //Create desired new empty instance of scope/serializer
				{
					if (compilerArgs.length > 0 && compilerArgs[0] instanceof Serializer)
					{
						if (compilerArgs.length > 4 && compilerArgs[4] instanceof Class && Serializer.class.isAssignableFrom((Class<?>) compilerArgs[4]))
							scope = ((Serializer) compilerArgs[0]).emptyClone((Class<Serializer>) compilerArgs[4], (GenericScope<?, ?>) compilerArgs[0]);
						else
							scope = ((Serializer) compilerArgs[0]).emptyClone();
					} 
					else
						scope = getPreferredSerializer();
				} 
				catch (Exception e) 
				{
					scope = getPreferredSerializer();
				}
				
				compilerArgs = compilerArgs.clone();
				compilerArgs[0] = false; //No extra formating...
				return str.isEmpty() ? scope : scope.LoadFrom(new StringReader(str), compilerArgs);
			}
		}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object arg, Object... args)
	{
		return toString(myHomeRegistry, arg, null, args);
	}
	
	/**
	 * @param myHomeRegistry | Registry where this parser is registered provided by {@link ParserRegistry#parse(String, boolean, Class, Object...)} otherwise it demands on implementation (it should not be null)!
	 * @param obj | Object to convert into string!
	 * @param preferedProtocol | Protocol to use preferably.
	 * @param args | Some additional args. This can be anything and it demands on implementation of DataConverter. Default SerialX API implementation will provide some flags about formating (2 ints)!
	 * 
	 * @return Object converted to string. Easiest way to do this is obj.toString() but you most likely want some more sofisticated formating.
	 * Return {@link DataParser#CONTINUE} to tell that this converter is not suitable for converting this object! You most likely want to do this when obtained obj is not suitable instance!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public CharSequence toString(ParserRegistry myHomeRegistry, Object arg, SerializationProtocol<Object> preferedProtocol, Object... args) 
	{
		if (arg instanceof Scope)
		{
			Serializer serializer;
			try
			{
				if (arg instanceof Serializer)
					serializer = (Serializer) arg;
				else if (args.length > 0 && args[0] instanceof Serializer)
					(serializer = ((Serializer) args[0]).emptyClone()).addAll((GenericScope<String, ?>) arg);
				else
					serializer = getPreferredSerializer();
			}
			catch (Exception e)
			{
				serializer = getPreferredSerializer();
			}
			
			if (serializer instanceof JussSerializer)
				((JussSerializer) serializer).setGenerateComments(args.length > 5 && args[5] instanceof Boolean && (boolean) args[5]);

			try 
			{
				if (args.length < 4)
					args = Arrays.copyOf(args, 4);
				else
					args = args.clone(); 
				args[2] = 0;
				args[3] = serializer.getProtocols();
				
				StringBuilder sb = new StringBuilder();
				GenericScope<?, ?> parent;
				if ((parent = serializer.getParent()) == null || serializer.getClass() != parent.getClass())
					sb.append(ImportsProvider.getAliasFor(serializer, getClass()) + " ");
				return serializer.SerializeAsSubscope(sb, args);
			} 
			catch (IOException e) 
			{
				throw new RuntimeException(e);
			}
		}

		return super.toString(myHomeRegistry, arg, preferedProtocol, args);
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		if (obj instanceof Scope && ((Scope) obj).isEmpty())
			return "Empty scope!";
		else if (obj instanceof CharSequence && indexOfNotInObj((CharSequence) obj, '\n', '\r') != -1)
			return "Multiline char sequence!";
		return new StringBuilder("Object of ").append(obj.getClass().getName()).append(": \"").append(obj.toString()).append("\" serialized using ").append(getProtocolFor(obj, SerializationProtocol.MODE_ALL, argsUsedConvert).toString()).append("!");
	}
	
	/**
	 * @return Serializer that is supposed to be used for serializing sub-scopes if there is no other option. This should never return null!
	 * 
	 * @since 1.3.5
	 */
	public Serializer getPreferredSerializer()
	{
		return new JussSerializer();
	}
}