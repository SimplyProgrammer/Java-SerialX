package org.ugp.serialx.converters;

import static org.ugp.serialx.Serializer.InvokeStaticFunc;
import static org.ugp.serialx.Serializer.indexOfNotInObj;
import static org.ugp.serialx.Serializer.isOneOf;
import static org.ugp.serialx.Serializer.splitValues;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.JussSerializer;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.imports.ImportsProvider;
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
		    <td>java.lang.Math::max 10 5</td>
		    <td>10</td>
	  	</tr>
	</table>
		<br>
	This parser requires additional parser arg at index 0 of type {@link GenericScope} or {@link Serializer} that will be used for further parsing and operating (default new {@link JussSerializer}).<br>
	This parser requires additional parser arg at index 3 of type {@link ProtocolRegistry} or {@link SerializationProtocol} itself that will be used for parsing protocol expressions (default {@link SerializationProtocol#REGISTRY}).<br>
 *  This parser will insert one additional argument into array of additional parser args at index 4, in case of serialization index 5, that will be of type {@link Class} and it will contains information about class of object that is being unserialized or serialized using protocol!</br>
 *  
 * @author PETO
 * 
 * @since 1.3.0
 */
public class ObjectConverter implements DataConverter 
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
		Class<?> objectClass = null, oldObjectClass;
		boolean hasOp, hasCls = false;
		char ch;

		if (str.length() > 0 && ((hasOp = (ch = str.charAt(0)) == '{' || ch == '[') && (hasCls = (ch = str.charAt(str.length()-1)) == '}' || ch == ']') /*|| containsNotInObj(str, ' ')*/ || (objectClass = getProtocolExprClass(str, compilerArgs)) != null))
		{
			if (objectClass != null)
			{
				if (objectClass == IsSelectorScope.class)
				{
					StringBuilder sb = new StringBuilder(str);
					sb.setCharAt(str.indexOf(' '), '=');
					return myHomeRegistry.parse(sb.toString(), compilerArgs);
				}
				else
				{
					if (compilerArgs.length < 5)
						compilerArgs = Arrays.copyOf(compilerArgs, 5);
					oldObjectClass = (Class<?>) compilerArgs[4];
					compilerArgs[4] = objectClass;
				}
				
				String[] args = splitValues(str = str.trim(), ' ');
				if (!isOneOf(args[0].charAt(0), '{', '[') && args[0].contains("::"))
				{
					String[] clsAttr = args[0].split("::");
					if (clsAttr.length > 1)
					{
						if (args.length > 1)
							try
							{
								Object rtrn = InvokeStaticFunc(objectClass, clsAttr[1], parseAll(myHomeRegistry, args, 1, true, compilerArgs));
								compilerArgs[4] = oldObjectClass;
								return rtrn;
							}
							catch (InvocationTargetException e) 
							{
								LogProvider.instance.logErr("Exception while calling method \"" + clsAttr[1] + "\":", e);
								e.printStackTrace();
							}
						else
							try
							{
								compilerArgs[4] = oldObjectClass;
								return clsAttr[1].equals("class") ? objectClass : clsAttr[1].equals("new") ? Serializer.Instantiate(objectClass) : objectClass.getField(clsAttr[1]).get(null);
							}
							catch (NoSuchFieldException e)
							{
								try
								{
									Object rtrn = InvokeStaticFunc(objectClass, clsAttr[1], parseAll(myHomeRegistry, args, 1, true, compilerArgs));
									compilerArgs[4] = oldObjectClass;
									return rtrn;
								}
								catch (InvocationTargetException e2) 
								{
									LogProvider.instance.logErr("Exception while calling method \"" + clsAttr[1] + "\":", e2);
									e.printStackTrace();
								}
							}
							catch (Exception e) 
							{
								LogProvider.instance.logErr("Unable to obtain value of field \"" + clsAttr[1] + "\" in class \"" + objectClass.getSimpleName() + "\" because:", e);
								e.printStackTrace();
							}
					/*catch (ClassNotFoundException e) 
					{
						LogProvider.instance.logErr("Unable to invoke \"" + args[0].split("::")[1] + "\" because class \"" + args[0].split("::")[0] + "\" was not found!");
					} */
					}
				}
				else
				{
					try
					{
						if ((ch = str.charAt(str.length()-1)) == ';' || ch == ',')
							throw new ClassNotFoundException();
						
						Object[] objArgs = parseAll(myHomeRegistry, args, 1, true, compilerArgs);
						if (objArgs.length == 1 && objArgs[0] instanceof Scope && Scope.class.isAssignableFrom(objectClass))
							return objArgs[0];
						compilerArgs[4] = oldObjectClass;
						return SerializationProtocol.unserializeObj(compilerArgs.length > 3 && compilerArgs[3] instanceof ProtocolRegistry ? (ProtocolRegistry) compilerArgs[3] : SerializationProtocol.REGISTRY, objectClass, objArgs);
						
						/*Object obj;
						if (objArgs.length == 1 && objArgs[0] instanceof Scope)
						{
							Scope scope = (Scope) objArgs[0];
							if ((obj = p.unserialize(objectClass, scope.toValArray())) != null)
								return obj;
							return p.unserialize(objectClass, scope);
						}
						else
						{
							if ((obj = p.unserialize(objectClass, objArgs)) != null)
								return obj;
							return p.unserialize(objectClass, new Scope(objArgs));
						}*/
					}
					catch (Exception e) 
					{
						LogProvider.instance.logErr("Exception while unserializing instance of \"" + objectClass.getName() + "\":", e);
						e.printStackTrace();
					}
				}
				compilerArgs[4] = oldObjectClass;
			}
			else
			{
				Serializer scope;
				try 
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
				
				if (indexOfNotInObj(str, '=', ':', ';', ',') > -1)
				{
					compilerArgs = compilerArgs.clone();
					compilerArgs[0] = false;
					return ((Serializer) scope.inheritParent()).LoadFrom(new StringReader(str), compilerArgs);
				}
				
				if (hasOp && hasCls)
				{
					str = str.substring(1, str.length()-1).trim();
					if (str.isEmpty())
						return scope;
					
					int index;
					if ((index = indexOfNotInObj(str, '=', ';', ',')) > -1 && (index >= str.length()-1 || str.charAt(index+1) != ':') || (objectClass = getProtocolExprClass(str, compilerArgs)) == null || objectClass == IsSelectorScope.class)
					{
						compilerArgs = compilerArgs.clone();
						compilerArgs[0] = false;
						return ((Serializer) scope.inheritParent()).LoadFrom(new StringReader(str), compilerArgs);
					}
					
					if (objectClass != null && indexOfNotInObj(str, "::") > -1)
						return myHomeRegistry.parse(str, compilerArgs);
					return parse(myHomeRegistry, str, compilerArgs);
				}
				else if (str.split(" ").length > 1)
				{
					LogProvider.instance.logErr("Unable to unserialize \"" + str + "\"! Possible reason of this is absence of comma or semicolon, try to insert them into empty spaces!", null);
					return null;
				}
			}
			LogProvider.instance.logErr("Unable to unserialize \"" + str + "\" because there is no such class or source string is corrupted!", null);
			return null;
		}
		return CONTINUE;
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
					sb.append(serializer.getImports().getAliasFor(arg.getClass()) + " ");
				return serializer.SerializeAsSubscope(sb, args);
			} 
			catch (IOException e) 
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
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
						if (args.length > 2)
							args[2] = index + 1;
						sb.append(myHomeRegistry.toString(objArgs[i], args));
						if (i < objArgs.length-1)
							if (sb.length() > sizeEndl)
							{
								sb.append('\n'); 
								for (int j = 0; j < tabs+1; j++) 
									sb.append('\t');
								sizeEndl += 10000;
							}
							else 
								sb.append(' ');
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
	 * @return Serializer that is supposed to be used for serializing sub-scopes if there is no other option.
	 * 
	 * @since 1.3.5
	 */
	public Serializer getPreferredSerializer()
	{
		return new JussSerializer();
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
	public static Class<?> getProtocolExprClass(String str, Object[] compilerArgs)
	{
		int i = 0, len = str.length();
		for (char ch; i < len; i++)
			if ((ch = str.charAt(i)) == ' ' || ch == ':')
				break;

		try 
		{
			Class<?> cls = ImportsProvider.forName(compilerArgs.length > 0 ? compilerArgs[0] : null, str.substring(0, i), false, ObjectConverter.class.getClassLoader());
			if (cls != null)
				return cls;
			for (char ch; i < len; i++)
			{
				if ((ch = str.charAt(i)) > 32)
					if (ch == '{' || ch == '[')
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