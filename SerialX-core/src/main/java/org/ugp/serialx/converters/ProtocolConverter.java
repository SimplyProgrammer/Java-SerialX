package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.Instantiate;
import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.isOneOf;
import static org.ugp.serialx.Utils.splitValues;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.Utils;
import org.ugp.serialx.converters.imports.ImportsProvider;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

/**
 * This converter is capable of converting any Object using {@link SerializationProtocol} as well as invoking static functions!
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
 * @since 1.3.0 (separated from ObjectConverter since 1.3.7)
 */
public class ProtocolConverter implements DataConverter 
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
	
	protected boolean allowStaticMemberInvocation = false;
	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... compilerArgs)
	{	
		int len;	
		if ((len = str.length()) > 0)
		{
			if ((str.charAt(0) | ' ') == '{' && (str.charAt(--len) | ' ') == '}') // Unwrap if wrapped in {}
				str = str.substring(1, len).trim();
			
			Class<?> objClass;
			if ((objClass = getProtocolExprClass(str, compilerArgs)) != null) // Get class of protocol expr or continue if there is none
				return parse(myHomeRegistry, objClass, str, compilerArgs);
		}
		return CONTINUE;
	}
	
	/**
	 * @param myHomeRegistry | Same as {@link DataParser#parse(ParserRegistry, String, Object...)}.
	 * @param objClass | Class of object to parse using {@link SerializationProtocol}.
	 * @param str | String that starts with {@link Class} of object to deserialized (it will be used with {@link SerializationProtocol#unserializeObj(ProtocolRegistry, Class, Object...)}).
	 * @param compilerArgs | Same as {@link DataParser#parse(ParserRegistry, String, Object...)}.
	 * 
	 * @return Object od objClass parsed from str in accordance with compilerArgs!
	 * 
	 * @since 1.3.7
	 */
	protected Object parse(ParserRegistry myHomeRegistry, Class<?> objClass, String str, Object... compilerArgs)
	{
		if (objClass == IsSelectorScope.class) //Handle css-like selector scope to variable assignment
		{
			StringBuilder sb = new StringBuilder(str);
			sb.setCharAt(str.indexOf(' '), '=');
			return myHomeRegistry.parse(sb.toString(), compilerArgs); //Should work only when ObjectConverter and VariableConverter are present...
		}

		if (compilerArgs.length < 5)
			compilerArgs = Arrays.copyOf(compilerArgs, 5);
		Class<?> oldObjectClass = (Class<?>) compilerArgs[4];
		compilerArgs[4] = objClass;
		
		String[] args = splitValues(str, ' ');
		int nameIndex;
		if (!isOneOf(args[0].charAt(0), '{', '[') && (nameIndex = args[0].indexOf("::")) > -1) //Is static member invocation
		{
			String memberName = args[0].substring(nameIndex + 2);
			if (!isAllowStaticMemberInvocation())
			{
				LogProvider.instance.logErr("Invocation of static member \"" + memberName + "\" from class \"" + objClass.getName() + "\" was denied because this feature is disabled by default for security reasons!", null);
				return null;
			}
				
			if (args.length > 1)
				return InvokeStaticFunc(objClass, oldObjectClass, memberName, parseAll(myHomeRegistry, args, 1, true, compilerArgs), compilerArgs);
			
			try
			{
				compilerArgs[4] = oldObjectClass;
				return memberName.equals("class") ? objClass : memberName.equals("new") ? Instantiate(objClass) : objClass.getField(memberName).get(null);
			}
			catch (NoSuchFieldException e)
			{
				return InvokeStaticFunc(objClass, oldObjectClass, memberName, parseAll(myHomeRegistry, args, 1, true, compilerArgs), compilerArgs);
			}
			catch (Exception e) 
			{
				LogProvider.instance.logErr("Unable to obtain value of field \"" + memberName + "\" in class \"" + objClass.getSimpleName() + "\" because:", e);
				e.printStackTrace();
				return null;
			}
		}

		try
		{
//			if ((ch = str.charAt(str.length()-1)) == ';' || ch == ',')
//				throw new ClassNotFoundException();
			
			Object[] objArgs = parseAll(myHomeRegistry, args, 1, true, compilerArgs);
			if (objArgs.length == 1 && objArgs[0] instanceof Scope && Scope.class.isAssignableFrom(objClass))
				return objArgs[0];
			compilerArgs[4] = oldObjectClass;
			return SerializationProtocol.unserializeObj(compilerArgs.length > 3 && compilerArgs[3] instanceof ProtocolRegistry ? (ProtocolRegistry) compilerArgs[3] : SerializationProtocol.REGISTRY, objClass, objArgs);
		}
		catch (Exception e) 
		{
			LogProvider.instance.logErr("Exception while unserializing instance of \"" + objClass.getName() + "\":", e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object arg, Object... args)
	{
		return toString(myHomeRegistry, arg, null, args);
	}
	
	/**
	 * @param myHomeRegistry | Registry where this parser is registered provided by {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)} otherwise it demands on implementation (it should not be null)!
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
		if (arg == null)
			return CONTINUE;
		
		if (useBase64IfCan && arg instanceof Serializable)
			return CONTINUE;
		
		if (preferedProtocol != null || (preferedProtocol = (SerializationProtocol<Object>) getProtocolFor(arg, SerializationProtocol.MODE_SERIALIZE, args)) != null)
		{
			Object[] objArgs;
			Class<?> oldObjectClass = null;
			try
			{
				int tabs = 0, index = 0;
				if (args.length > 1 && args[1] instanceof Integer)
					tabs = (int) args[1];
				
				if (args.length > 2 && args[2] instanceof Integer)
					index = (int) args[2];
				
				if (args.length < 5)
					args = Arrays.copyOf(args, 5);
				oldObjectClass = (Class<?>) args[4];
				args[4] = arg.getClass();;

				objArgs = preferedProtocol.serialize(arg);
				StringBuilder sb = new StringBuilder(ImportsProvider.getAliasFor(args.length > 0 ? args[0] : null, arg.getClass()) + (objArgs.length <= 0 ? "" : " "));
				
				args = args.clone();
				for (int i = 0, sizeEndl = 10000; i < objArgs.length; i++) 
				{
					if (i > 0)
						if (sb.length() > sizeEndl)
						{
							sb.append('\n'); 
							for (int j = 0; j < tabs+1; j++) 
								sb.append('\t');
							sizeEndl += 10000;
						}
						else 
							sb.append(' ');

					if (args.length > 2)
						args[2] = index + 1;
					sb.append(myHomeRegistry.toString(objArgs[i], args));
				}
				
				args[4] = oldObjectClass;
				return index > 0 && objArgs.length > 0 ? sb.insert(0, '{').append('}') : sb;
			}
			catch (Exception e) 
			{
				LogProvider.instance.logErr("Exception while serializing instance of \"" + arg.getClass().getName() + "\":", e);
				e.printStackTrace();
			}
			args[4] = oldObjectClass;
		}
		return CONTINUE;
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
	 * @return True if program is forced to use {@link Base64} serialization on {@link Serializable} objects.
	 * This might result into some form of encryption but its less flexible and tends to be slower than SerialX {@link SerializationProtocol} system!
	 * In some cases, java Serialization can be more effective than protocols sometimes not! You should try which gives you the best result, then you can also deactivate certain protocols that are less effective than Java serialization.
	 * For example for long strings, classic Java serialization is better than protocol, it will take less memory storage space, but performance is almost always far slower!<br>
	 * Note: Whole concept of SerialX API is about avoiding classic Java serialization from many reasons so you most likely want this on true! Also protocol will be almost certainly faster classic serialization!<br>
	 * Note: This will only work when this converter is registered in {@link ParserRegistry} together with {@link SerializableBase64Converter}!
	 *
	 * @since 1.3.5
	 */
	public boolean isUseBase64IfCan() 
	{
		return useBase64IfCan;
	}

	/**
	 * @param useBase64IfCan | Set this on true to force program to use {@link Base64} serialization on {@link Serializable} objects.
	 * Doing this might result into some form of encryption but its less flexible and tends to be slower than SerialX {@link SerializationProtocol} system!
	 * In some cases, java Serialization can be more effective than protocols sometimes not! You should try which gives you the best result, then you can also deactivate certain protocols that are less effective than Java serialization.
	 * For example for long strings, classic Java serialization is better than protocol, it will take less memory storage space, but performance is almost always far slower!<br>
	 * Note: Whole concept of SerialX API is about avoiding classic Java serialization from many reasons so you most likely want this on true! Also protocol will be almost certainly faster classic serialization!<br>
	 * Note: This will only work when this converter is registered in {@link ParserRegistry} together with {@link SerializableBase64Converter}!
	 * 
	 * @since 1.3.5
	 */
	public void setUseBase64IfCan(boolean useBase64IfCan) 
	{
		this.useBase64IfCan = useBase64IfCan;
	}

	/**
	 * @return True if invocation of static members (:: operator) is allowed (false by default)!
	 * 
	 * @since 1.3.7
	 */
	public boolean isAllowStaticMemberInvocation()
	{
		return allowStaticMemberInvocation;
	}

	/**
	 * @param allowStaticMemberInvocation | Enable/disable the invocation of static members (:: operator) (false by default)!
	 * 
	 * @since 1.3.7
	 */
	public void setAllowStaticMemberInvocation(boolean allowStaticMemberInvocation) 
	{
		this.allowStaticMemberInvocation = allowStaticMemberInvocation;
	}

	/**
	 * @param obj | Object to get protocol for!
	 * @param mode | Protocol mode!
	 * @param args | Parser args to get protocol from!
	 * 
	 * @return Protocol obtained from args or from {@link SerializationProtocol#REGISTRY} if there is no protocol or {@link ProtocolRegistry} in args (index 3).<br>
	 * Note: This is mainly used by {@link ObjectConverter}!
	 *
	 * @since 1.3.5
	 */
	public static SerializationProtocol<?> getProtocolFor(Object obj, byte mode, Object[] args)
	{
		if (args.length > 3) 
		{
			if (args[3] instanceof ProtocolRegistry)
				return ((ProtocolRegistry) args[3]).GetProtocolFor(obj, mode);
			else if (args[3] instanceof SerializationProtocol)
				return (SerializationProtocol<?>) args[3];
		}

		return SerializationProtocol.REGISTRY.GetProtocolFor(obj, mode);
	}
	
	/**
	 * @param str | String to check protocol class for!
	 * 
	 * @return Class of the protocol or null if inserted statement is not protocol expression!
	 * For example: <code>getProtocolExprClass("java.util.ArrayList 1 2 3 4 5")</code> will return {@link ArrayList} but <code>"Hello world!"</code> will return null!
	 *
	 * @since 1.3.0
	 */
	public static Class<?> getProtocolExprClass(String str, Object... compilerArgs)
	{
		int i = 0, len = str.length();
		for (char ch; i < len; i++)
			if ((ch = str.charAt(i)) == ' ' || ch == ':')
				break;

		try 
		{
			Class<?> cls = ImportsProvider.forName(compilerArgs.length > 0 ? compilerArgs[0] : null, str.substring(0, i), false, ProtocolConverter.class.getClassLoader());
			if (cls != null)
				return cls;
			for (char ch; i < len; i++)
			{
				if ((ch = str.charAt(i)) > 32)
					if ((ch | ' ') == '{')
						return IsSelectorScope.class;
					else
						return null;
			}
		} 
		catch (ClassNotFoundException e) 
		{}
		return null;
	}
	
	/**
	 * @param registry | Registry to use!
	 * @param strs | Source strings to parse using suitable parser from registry.
	 * @param from | Start index to begin from!
	 * @param trim | If true, all strings will be trimed before parsing!
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Array of parsed objects, each parsed using {@link DataParser#parseObj(String, Object...)}
	 * 
	 * @since 1.3.0
	 */
	public static Object[] parseAll(ParserRegistry registry, String strs[], int from, boolean trim, Object... args)
	{
		Object[] objs = new Object[strs.length-from];
		for (int i = 0; from < strs.length; from++, i++) 
			objs[i] = registry.parse(trim ? strs[from].trim() : strs[from], args);
		return objs;
	}
	
	/**
	 * @return Array with 2 preferred sub-scope wrapping chars that are used during serialization by default. <code>{ and }</code>
	 * 
	 * @since 1.3.5
	 */
	public static char[] primarySubscopeWrappers()
	{
		return new char[] {'{', '}'};
	}
	
	/**
	 * @return Array with 2 secondary sub-scope wrapping char. <code>[ and ]</code>
	 * 
	 * @since 1.3.5
	 */
	public static char[] secondarySubscopeWrappers()
	{
		return new char[] {'[', ']'};
	}
	
	/**
	 * @param cls | Class to invoke method from.
	 * @param oldCls | Old class to set (obtained from compilerArgs[4]).
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * @param compilerArgs | Arguments provided by parser.
	 * 
	 * @return {@link Utils#InvokeStaticFunc(Class, String, Object...)} or null if {@link InvocationTargetException} occurred. <br>
	 * Note: If you are not sure what this does, preferably use {@link Utils#InvokeStaticFunc(Class, String, Object...)}!
	 * 
	 * @since 1.3.7
	 */
	public static Object InvokeStaticFunc(Class<?> cls, Class<?> oldCls, String name, Object[] args, Object... compilerArgs) {
		try
		{
			compilerArgs[4] = oldCls;
			return Utils.InvokeStaticFunc(cls, name, args);
		}
		catch (InvocationTargetException e) 
		{
			LogProvider.instance.logErr("Exception while calling method \"" + name + "\":", e);
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Used internally by {@link ObjectConverter}!<br>
	 * Dummy class with no purpose except to be mark (.class) for shortened scope expression such as:
	 * <code>
	 * shortenedSelectorLikeScope {<br><br>
	 * 		//stuff...
	 * };
	 * </code>
	 * 
	 * @author PETO
	 *
	 * @since 1.3.0
	 */
	protected static class IsSelectorScope {};
}