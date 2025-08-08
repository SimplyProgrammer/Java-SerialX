package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.utils.Utils.indexOfNotInObj;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.ProtocolConverter;
import org.ugp.serialx.converters.imports.ImportsProvider;
import org.ugp.serialx.juss.JussSerializer;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;
import org.ugp.serialx.utils.LogProvider;

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
				
				if (compilerArgs.length > 0)
				{
					compilerArgs = compilerArgs.clone();
					compilerArgs[0] = false; //No extra formating...
				}
				
				if (str.isEmpty())
					return scope;
				try 
				{
					return scope.loadFrom(new StringReader(str), compilerArgs);
				} 
				catch (IOException e) 
				{
					LogProvider.instance.logErr("Unable to parse the scope because:", e); // Should not occur...
				}
			}
		}
		return CONTINUE;
	}

	@Override
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, Object... args) throws IOException
	{
		return toString(source, myHomeRegistry, obj, null, args);
	}
	
	@SuppressWarnings("unchecked")
	public Appendable toString(Appendable source, ParserRegistry myHomeRegistry, Object obj, SerializationProtocol<Object> preferedProtocol, Object... args) 
	{
		if (obj instanceof Scope)
		{
			Serializer serializer;
			try
			{
				if (obj instanceof Serializer)
					serializer = (Serializer) obj;
				else if (args.length > 0 && args[0] instanceof Serializer)
					(serializer = ((Serializer) args[0]).emptyClone()).addAll((GenericScope<String, ?>) obj);
				else
					serializer = getPreferredSerializer();
			}
			catch (Exception e)
			{
				serializer = getPreferredSerializer();
			}

			if (args.length < 6)
				args = Arrays.copyOf(args, 6);
			else
			{
				if (args[5] instanceof Byte)
					serializer.setFormat((byte) args[5]);

				args = args.clone(); 
			}
			args[2] = 0;
			args[3] = serializer.getProtocols();
			
			try 
			{
				GenericScope<?, ?> parent;
				if ((parent = serializer.getParent()) == null || serializer.getClass() != parent.getClass())
					source.append(ImportsProvider.getAliasFor(serializer, getClass())).append(' ');
				return serializer.serializeAsSubscope(source, args);
			} 
			catch (IOException e) 
			{
				throw new RuntimeException(e);
			}
		}

		return super.toString(source, myHomeRegistry, obj, preferedProtocol, args);
	}
	
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object obj, Object... argsUsedConvert) 
	{
		if (obj instanceof Scope && ((Scope) obj).isEmpty())
			return "Empty scope!";
		return super.getDescription(myHomeRegistry, obj, argsUsedConvert);
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