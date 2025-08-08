package org.ugp.serialx;

import static org.ugp.serialx.utils.Utils.Instantiate;
import static org.ugp.serialx.utils.Utils.indexOfNotInObj;
import static org.ugp.serialx.utils.Utils.multilpy;
import static org.ugp.serialx.utils.Utils.post;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.StringConverter;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;
import org.ugp.serialx.utils.LogProvider;
import org.ugp.serialx.utils.Registry;

/**
 * {@link org.ugp.serialx.Serializer} is powerful abstract class that allows you to serialize any object in Java using custom data format compiled by recursive descent parser consisting of {@link DataParser}s.
 * This class itself is responsible for formating and managing input-output (IO) of content obtained from parsers and protocols as well as their management of their usage!
 * It is instance of {@link Scope} so we can say that this is scope that can serialize itself using system of already mentioned {@link DataParser} and {@link SerializationProtocol}!
 * 
 * @author PETO
 *
 * @since 1.0.0 
 */
@SuppressWarnings("serial")
public abstract class Serializer extends Scope implements MultimediaSerializer<Scope>
{	
	/**
	 * Common parsers that can parse primitive datatypes!
	 * 
	 * @since 1.3.2
	 */
	public static final ParserRegistry COMMON_PARSERS = DataParser.REGISTRY.clone();
	
	protected ParserRegistry parsers;
	protected ProtocolRegistry protocols;
	
	protected byte format = 0;
	
	/**
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public Serializer(Object... values) 
	{
		this(null, values);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public Serializer(Map<String, ?> variablesMap, Object... values) 
	{
		this(variablesMap, values == null ? null : new ArrayList<>(Arrays.asList(values)));
	}

	/**
	 * @param sourceScope | Scope with initial content!
	 * 
	 * @since 1.3.5
	 */
	public Serializer(GenericScope<String, ?> sourceScope)
	{
		this(sourceScope == null ? null : sourceScope.variables(), sourceScope == null ? null : sourceScope.values());
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public Serializer(Map<String, ?> variablesMap, Collection<?> values) 
	{
		this(null, variablesMap, values);
	}
	
	/**
	 * @param parsers | Registry of parsers to use!
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public Serializer(Registry<DataParser> parsers, Map<String, ?> variablesMap, Collection<?> values) 
	{
		this(parsers, null, variablesMap, values, null);
	}
	
	/**
	 * @param parsers | Registry of parsers to use!
	 * @param protocols | Registry of protocols to use!
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * @param parent | Parent of this scope.
	 *
	 * @since 1.3.2
	 */
	public Serializer(Registry<DataParser> parsers, ProtocolRegistry protocols, Map<String, ?> variablesMap, Collection<?> values, Scope parent) 
	{
		super(variablesMap, values, parent);
		if (parsers != null)
			setParsers(parsers);
		if (protocols != null)
			setProtocols(protocols);
	}

	@Override
	public <S extends GenericScope<String, Object>> S clone(Class<S> typeOfClone) throws Exception
	{
		S clone = super.clone(typeOfClone);
		if (clone instanceof Serializer)
		{
			((Serializer) clone).setParsers(getParsers());
			((Serializer) clone).setProtocols(getProtocols());
		}
		return clone;
	}

	@Override
	public <K, V, S extends GenericScope<? super K, ? super V>> S castTo(Class<S> newType) throws Exception 
	{
		S clone = super.castTo(newType);
		if (clone instanceof Serializer)
		{
			((Serializer) clone).setParsers(getParsers());
			((Serializer) clone).setProtocols(getProtocols());
		}
		return clone;
	}

	@Override
	public <T> T toObjectOf(String variableWithscope, Class<T> objClass, T defaultValue) throws Exception
	{
		return toObjectOf(variableWithscope, objClass, defaultValue, getProtocols());
	}
	
	/**
	 * @param objClass | Class of object to create (may or may not support other implementations of {@link Type}).
	 * 
	 * @return Object of type constructed from this scopes independent values using protocol for given class or null if there was no protocol found in this {@link Serializer#getProtocols()}! 
	 * 
	 * @throws Exception | Exception if Exception occurred in {@link SerializationProtocol#unserialize(Class, Object...)}!
	 * 
	 * @since 1.2.5
	 */
	@Override
	public <T> T toObject(Type objClass) throws Exception
	{
		return toObject(objClass, getProtocols());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V> GenericScope<String, V> transform(Function<Object, V> trans, boolean includeSubScopes) 
	{
		Serializer transformed = (Serializer) super.transform(trans, includeSubScopes);
		transformed.parsers = getParsers();
		transformed.protocols = getProtocols();
		return (GenericScope<String, V>) transformed;
	}
	
	/**
	 * @see Serializer#into(Object, Serializer, String...)
	 */
	@Override
	public <T> T into(T obj, String... fieldNamesToUse) throws IntrospectionException, Exception 
	{
		return Serializer.into(obj, this, fieldNamesToUse);
	}
	
	/**
	 * @deprecated (1.3.9) DO NOT USE THIS, USE VARIATION OF {@link AbstractMap.SimpleEntry} OR {@link VariableConverter#NewVariable(String, Object)} FROM <code>serialx-juss module</code> INSTEAD!
	 * 
	 * @param name | Name of variable.
	 * @param value | Value of variable.
	 * 
	 * @return Classic variable expression in general form like [name] = [value].
	 * 
	 * @since 1.1.5
	 */
	@Deprecated
	public <T> String Var(String name, T value)
	{
		return Code(name + " = " + getParsers().toString(value, 0, 0, this, getProtocols(), false) + ";");
	}
	
	/**
	 * @param comment | The comment...
	 * 
	 * @return Comment in general form, such as //comment<br>Each line will be taken as new comment!
	 * 
	 * @since 1.1.5
	 */
	public String Comment(String comment)
	{
		StringBuilder sb = new StringBuilder();
		String[] lines = comment.split("\n");
		for (int i = 0; i < lines.length; i++)
			sb.append("//" + lines[i] + (i < lines.length - 1 ? "\n" : ""));
		return Code(sb);
	}
	
	/**
	 *
	 * @param code | Code to convert into JUSS (StringConverter) code form.
	 * @return "${" + code + "}
	 * <br><br>Note: This works only in combination with {@link StringConverter}!
	 * 
	 * @since 1.1.5
	 * 
	 * @see StringConverter#DirectCode(Object)
	 */
	public String Code(Object code)
	{
		return StringConverter.DirectCode(code);
	}
	
	/**
	 * @param serialize | If true, this scope will be serialized to string!
	 * 
	 * @return This scope as string!
	 * 
	 * @since 1.3.2
	 * 
	 * @see Serializer#stringify(Object...)
	 */
	public String toString(boolean serialize)
	{
		if (serialize)
			return stringify();
		return super.toString();
	}
	
	/**
	 * @return Clone of this {@link Serializer} without variables and values, protocols and parser will remain same!
	 * 
	 * @throws Exception When creating the new instance fails...
	 * 
	 * @since 1.3.2 
	 */
	public Serializer emptyClone() throws Exception
	{
		return emptyClone(this);
	}
	
	/**
	 * @param parent | Parent scope of new Serializer instance!
	 * 
	 * @return Clone of this {@link Serializer} without variables and values, protocols and parser will remain same!
	 * 
	 * @throws Exception When creating the new instance fails...
	 * 
	 * @since 1.3.2 
	 */
	public Serializer emptyClone(Scope parent) throws Exception
	{
		return emptyClone(getClass(), parent);
	}
	
	/**
	 * @param typeOfClone | Specific type of the clone (has to extend Serializer).
	 * @param parent | Parent scope of new Serializer instance!
	 * 
	 * @return Clone of this {@link Serializer} without variables and values, protocols and parser will remain same!
	 * 
	 * @throws Exception When creating the new instance fails...
	 * 
	 * @since 1.3.5
	 */
	public <T extends Serializer> T emptyClone(Class<T> typeOfClone, GenericScope<?, ?> parent) throws Exception
	{
		return emptyClone(Instantiate(typeOfClone), parent);
	}
	
	/**
	 * @param newEmptyInstance | Manually inserted brand new empty instance of this scope!
	 * @param parent | Parent scope of new Serializer instance!
	 * 
	 * @return Clone of this {@link Serializer} without variables and values, protocols and parser will remain same!
	 * 
	 * @since 1.3.5
	 */
	public <T extends Serializer> T emptyClone(T newEmptyInstance, GenericScope<?, ?> parent) 
	{
		newEmptyInstance.setParsers(getParsers());
		newEmptyInstance.setProtocols(getProtocols());
		newEmptyInstance.setFormat(getFormat());
		newEmptyInstance.parent = parent;
		return newEmptyInstance;
	}
	
	@Override
	public Scope clone()
	{
		Scope scope = super.clone();
		if (scope instanceof Serializer)
		{
			((Serializer) scope).setParsers(getParsers());
			((Serializer) scope).setProtocols(getProtocols());
			((Serializer) scope).setFormat(getFormat());
		}
		return scope;
	}
	
	/**
	 * @return {@link Registry} with parsers that this {@link Serializer} uses!
	 * 
	 * @since 1.3.2
	 */
	public ParserRegistry getParsers() 
	{
		if (parsers == null)
			parsers = COMMON_PARSERS.clone();
		return parsers;
	}

	/**
	 * @param parsers | Registry with parsers to set!
	 * 
	 * @since 1.3.2
	 */
	public void setParsers(Registry<DataParser> parsers) 
	{
		setParsers(parsers instanceof ParserRegistry ? (ParserRegistry) parsers : new ParserRegistry(parsers));
	}
	
	/**
	 * @param parsers | Registry with parsers to set!
	 * 
	 * @since 1.3.5
	 */
	public void setParsers(ParserRegistry parsers) 
	{
		this.parsers = (ParserRegistry) parsers;
	}

	/**
	 * @return {@link ProtocolRegistry} with serialization protocols that this {@link Serializer} uses! 
	 * 
	 * @since 1.3.2
	 */
	public ProtocolRegistry getProtocols() 
	{
		if (protocols == null)
			protocols = SerializationProtocol.REGISTRY.clone();
		return protocols;
	}

	/**
	 * @param protocols | ProtocolRegistry with serialization protocols to set!
	 * 
	 * @since 1.3.2
	 */
	public void setProtocols(ProtocolRegistry protocols) 
	{
		this.protocols = protocols;
	}
	
	/**
	 * @param parser | DataParser to add!
	 * 
	 * @return DataParser if was added!
	 * 
	 * @since 1.3.2
	 */
	public DataParser addParser(DataParser parser)
	{
		if (getParsers().add(parser))
			return parser;
		return null;
	}
	
	/**
	 * @param protocol | SerializationProtocol to add!
	 * 
	 * @return SerializationProtocol if was added!
	 * 
	 * @since 1.3.2
	 */
	public SerializationProtocol<?> addProtocol(SerializationProtocol<?> protocol)
	{
		if (getProtocols().add(protocol))
			return protocol;
		return null;
	}
	
	/**
	 * This will transform this Serializer and whole tree of its sub-scopes into regular {@link Scope}s!
	 * Remember that scope is just a data container and analyzer and it can't be serialized in contrast to serializer!
	 * 
	 * @return This serializer transformed in to simple {@link Scope}!
	 * 
	 * @since 1.3.2
	 */
	public GenericScope<String, Object> transformToScope()
	{
		Function<Object, Object> transFunction = new Function<Object, Object>()
		{
			@Override
			public Object apply(Object t) 
			{
				if (t instanceof Serializer)
				{
					GenericScope<String, ?> srl = ((Scope) t).transform(this);
					return new Scope(srl.variables(), srl.values(), srl.getParent());
				}
				return t;
			}
		};
		return (GenericScope<String, Object>) transform(transFunction);
	}
	
	/**
	 * @param source | Source {@link Appendable} to serialize variables and objects into!
	 * @param wrappingBrackets | Array of 2 characters to wrap content inside
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Serialized content of this scope as inner sub-scope of this scope! Wrapped inside of corresponding wrappingBrackets (default { and })!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public <A extends Appendable> A serializeAsSubscope(A source, final char[] wrappingBrackets, Object... args) throws IOException
	{
		source.append(wrappingBrackets[0]);
		if (isEmpty())
			return (A) source.append(wrappingBrackets[1]);

		if (format == 0) // No format...
		{
			if (args.length > 1 && args[1] instanceof Integer)
				args[1] = ((int) args[1]) + 1;
			return (A) serializeTo(source, args).append(wrappingBrackets[1]);
		}

		int tabs;
		if (args.length > 1 && args[1] instanceof Integer)
			args[1] = (tabs = (int) args[1]) + 1;
		else
			tabs = 0;
		
		return (A) serializeTo(source.append('\n'), args).append('\n').append(multilpy('\t', tabs))
					.append(wrappingBrackets[1]);
	}
	
	/**
	 * @param indexWithStringValue | Index of independent value that should be string.
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Object that was parsed from string at given index, using parsers of this {@link Serializer}!
	 * 
	 * @since 1.3.8
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParsed(int indexWithStringValue, Object... args)
	{
		return (T) getParsers().parse(getString(indexWithStringValue), args);
	}
	
	/**
	 * @param variableWithStringValue | Variable name with string value.
	 * @param args | Additional arguments that will be obtained in {@link DataParser#parse(String, Object...)}!
	 * 
	 * @return Object that was parsed from value of given variable, using parsers of this {@link Serializer}!
	 * 
	 * @since 1.3.8
	 */
	@SuppressWarnings("unchecked")
	public <T> T getParsed(String variableWithStringValue, Object... args)
	{
		return (T) getParsers().parse(getString(variableWithStringValue), args);
	}

	/**
	 * @return Non 0 value if proper indentation and newline characters should be used when serializing. Default is 0, no formating...
	 * The exact behavior of other values will depend on the specific implementation of {@link Serializer#serializeTo(Appendable, Object...)}
	 * 
	 * @since 1.3.9
	 */
	public byte getFormat() 
	{
		return format;
	}

	/**
	 * @param format | A non 0 value if proper indentation and newline characters should be used when serializing.
	 * The exact behavior of other values will depend on the specific implementation of {@link Serializer#serializeTo(Appendable, Object...)}
	 * 
	 * @since 1.3.9
	 */
	public void setFormat(byte format) 
	{
		this.format = format;
	}
	
	/**
	 * @param obj | Object to map the serializer variables into!
	 * @param fromSerializer | Source serializer to map obj from!
	 * @param fieldNamesToUse | Array of obj field names to map/populate from scopes variables using setters (write method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link Scope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return Same obj after being populated/mapped by contents of fromSerializer via requested fields (fieldNamesToUse) and conversion rules listed below!
	 * Table of specific Serializer --> Object conversions:
	 * 	<style>
			table, th, td 
			{
			  border: 1px solid gray;
			}
		</style>
		<table>
			<tr>
			    <th>Object (obj) type</th>
			    <th>Action with obj</th> 
			</tr>
		    <tr>
			    <td>{@link File}</td>
			    <td>{@link Serializer#serializeTo(File, Object...)}</td>
			</tr>
		    <tr>
			    <td>{@link Appendable}</td>
			    <td>{@link Serializer#serializeTo(Appendable, Object...)}</td>
			</tr>
			<tr>
			    <td>{@link OutputStream}</td>
			    <td>{@link Serializer#serializeTo(OutputStream, Object...)}</td>
		  	</tr>
		  	<tr>
			    <td>{@link URL}</td>
			    <td>Serializer (fromSerializer) will open connection with {@link URL} and attempt serialize its content to it if possible!</td>
			</tr>
		  	<tr>
			    <td>{@link URLConnection}</td>
			    <td>Serializer (fromSerializer) will attempt serialize its content into given {@link URLConnection} if possible!</td>
			</tr>
		  	<tr>
			    <td>{@link CharSequence} (as http address)</td>
			    <td>Serializer (fromSerializer) will open connection with url and get + serialize the content into it if possible!</td>
			</tr>
		  	<tr>
			    <td>Others (default)</td>
			    <td>{@link Scope#into(Object, GenericScope, String...)} (return description)</td>
			</tr>
		</table>
		
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often) or when something went wrong during serialization!
	 * @throws IntrospectionException when there were no PropertyDescriptor found for obj class!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public static <T> T into(Object obj, Serializer fromSerializer, String... fieldNamesToUse) throws Exception, IntrospectionException
	{
		if (obj instanceof File)
		{
			fromSerializer.serializeTo((File) obj);
			return (T) obj;
		}
		
		if (obj instanceof Appendable)
		{
			fromSerializer.serializeTo((Appendable) obj);
			return (T) obj;
		}
		
		if (obj instanceof OutputStream)
		{
			fromSerializer.serializeTo((OutputStream) obj);
			return (T) obj;
		}
		
		if (obj instanceof URL)
		{
			URLConnection con = ((URL) obj).openConnection();
			con.setDoOutput(true);
			if (con instanceof HttpURLConnection)
				post(fromSerializer, (HttpURLConnection) con);
			else
				fromSerializer.serializeTo(con.getOutputStream());
			return (T) con;
		}
		
		if (obj instanceof URLConnection)
		{
			if (obj instanceof HttpURLConnection)
				post(fromSerializer, (HttpURLConnection) obj);
			else
				fromSerializer.serializeTo(((URLConnection) obj).getOutputStream());
			return (T) obj;
		}
		
		try
		{
			if (obj instanceof CharSequence)
			{
				if (indexOfNotInObj((CharSequence) obj, "http") == 0)
				{
					URLConnection con = new URI(obj.toString()).toURL().openConnection();
					con.setDoOutput(true);
					if (con instanceof HttpURLConnection)
						post(fromSerializer, (HttpURLConnection) con);
					else
						fromSerializer.serializeTo(con.getOutputStream());
					return (T) con;
				}
			
				try
				{
					File file = new File(obj.toString());
					fromSerializer.serializeTo(file);
					return (T) file;
				}
				catch (Exception e)
				{}
			}
		}
		catch (IOException e)
		{}
		
		return (T) Scope.into(obj, fromSerializer, fieldNamesToUse);
	}

	/**
	 * @param newInstance | New instance of specific {@link Serializer}
	 * @param fromObj | Object to create serializer from!
	 * @param fieldNamesToUse | Array of obj field names to map into scopes variables using getters (read method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link Scope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return {@link Serializer} created from given fromObj by mapping obj's fields into variables of created serializer via given fields (fieldNamesToUse) and conversion rules listed below!!<br><br>
	 * Table of specific Object --> Serializer conversions:
	 * 	<style>
			table, th, td 
			{
			  border: 1px solid gray;
			}
		</style>
		<table>
			<tr>
			    <th>Object (fromObj) type</th>
			    <th>Obtained serializer content (return)</th> 
			</tr>
			<tr>
			    <td>{@link CharSequence}</td>
			    <td>{@link Serializer#loadFrom(CharSequence, Object...)}</td>
		  	</tr>
			<tr>
			    <td>{@link CharSequence} (as http address)</td>
			    <td>Serializer (newInstance) will open connection with url and get + deserialize the content from it if possible!</td>
		  	</tr>
		    <tr>
			    <td>{@link File}</td>
			    <td>{@link Serializer#loadFrom(File, Object...)}</td>
			</tr>
			<tr>
			    <td>{@link Reader}</td>
			    <td>{@link Serializer#loadFrom(Reader)}</td>
			</tr>
			<tr>
			    <td>{@link InputStream}</td>
			    <td>{@link Serializer#loadFrom(InputStream, Object...)}</td>
		  	</tr>
		  	<tr>
			    <td>{@link URL}</td>
			    <td>Serializer (newInstance) will open connection with {@link URL} and get + deserialize the content from it if possible!</td>
			</tr>
		  	<tr>
			    <td>{@link URLConnection}</td>
			    <td>Serializer (newInstance) will attempt to get + deserialize the content from given {@link URLConnection} if possible!</td>
			</tr>
		  	<tr>
			    <td>Others (default)</td>
			    <td>{@link Scope#from(Object, String...)} (return description)</td>
			</tr>
		</table>
	 *	
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often) or when something went very wrong during deserialization!
	 * @throws IntrospectionException when there were no PropertyDescriptor found for obj class!
	 * @throws RuntimeException in case of something went wrong during deserialization process and {@link LogProvider#isReThrowException()} is set to true!
	 * 
	 * @since 1.3.5
	 */
	public static Serializer from(Serializer newInstance, Object fromObj, String... fieldNamesToUse) throws Exception, IntrospectionException
	{
		if (fromObj instanceof CharSequence)
		{
			try
			{
				String fromStr;
				if (indexOfNotInObj(fromStr = fromObj.toString(), "http") == 0)
					return newInstance.loadFrom(new URI(fromStr).toURL().openStream());
				return newInstance.loadFrom(new File(fromStr));
			}
			catch (Exception e)
			{
				if (e instanceof RuntimeException)
					throw e;
			}

			return newInstance.loadFrom((CharSequence) fromObj);
		}
		
		if (fromObj instanceof File)
			return newInstance.loadFrom((File) fromObj);
		if (fromObj instanceof Reader)
			return newInstance.loadFrom((Reader) fromObj);
		if (fromObj instanceof InputStream)
			return newInstance.loadFrom((InputStream) fromObj);
		if (fromObj instanceof URL)
			return newInstance.loadFrom(((URL) fromObj).openStream());
		if (fromObj instanceof URLConnection)
			return newInstance.loadFrom(((URLConnection) fromObj).getInputStream());
		
		newInstance.addAll(Scope.from(fromObj, fieldNamesToUse));
		return newInstance;
	}
}