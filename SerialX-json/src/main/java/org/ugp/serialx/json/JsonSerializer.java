package org.ugp.serialx.json;

import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.isOneOf;
import static org.ugp.serialx.Utils.multilpy;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.JussSerializer;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.BooleanConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.NullConverter;
import org.ugp.serialx.converters.StringConverter;
import org.ugp.serialx.converters.VariableConverter;
import org.ugp.serialx.json.converters.JsonCharacterConverter;
import org.ugp.serialx.json.converters.JsonNumberConverter;
import org.ugp.serialx.json.converters.JsonObjectConverter;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

import javafx.beans.binding.When;

/**
 * This is implementation of {@link JussSerializer} for serializing in <a href = "https://www.json.org/json-en.html">Json</a>!
 * It should generate and work with .json files!
 * <br><br>
 * Note: No Json specific syntax checks are made, only some small conventions and formating changes will occur to ensure Json syntax correctness but it will let you to use some Juss features freely so you can easily end up with some Json-juss hybrid!
 * 
 * @author PETO
 *
 * @since 1.3.2
 */
@SuppressWarnings("serial")
public class JsonSerializer extends JussSerializer 
{
	/**
	 * This is representation of empty Json array. Use this instead of empty scope during serialization when you want to prevent it being serialized as empty Json object!
	 * <br><br>
	 * <code>[]</code>
	 * 
	 * @since 1.3.5
	 */
	public static final String EMPTY_ARRAY = StringConverter.DirectCode("[]");

	/**
	 * This is representation of empty Json array. This is how empty scopes will be serialized by default!
	 * <br><br>
	 * <code>[]</code>
	 * 
	 * @since 1.3.5
	 * 
	 * @see JsonSerializer#EMPTY_ARRAY
	 */
	public static final String EMPTY_OBJECT = StringConverter.DirectCode("{}");
	
	public static final ParserRegistry JSON_PARSERS = new ParserRegistry(new VariableConverter(true), new StringConverter(), new JsonNumberConverter(), new BooleanConverter(false), new JsonCharacterConverter(false), new NullConverter(), new JsonObjectConverter());
	
	/**
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JsonSerializer(Object... values) 
	{
		this(null, values);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JsonSerializer(Map<String, ?> variablesMap, Object... values) 
	{
		this(variablesMap, values == null ? null : new ArrayList<>(Arrays.asList(values)));
	}

	/**
	 * @param sourceScope | Scope with initial content!
	 * 
	 * @since 1.3.5
	 */
	public JsonSerializer(GenericScope<String, ?> sourceScope)
	{
		this(sourceScope == null ? null : sourceScope.variables(), sourceScope == null ? null : sourceScope.values());
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JsonSerializer(Map<String, ?> variablesMap, Collection<?> values) 
	{
		this(JSON_PARSERS.clone(), variablesMap, values);
	}
	
	/**
	 * @param parsers | Registry of parsers to use!
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JsonSerializer(Registry<DataParser> parsers, Map<String, ?> variablesMap, Collection<?> values) 
	{
		this(parsers, SerializationProtocol.REGISTRY.clone(), variablesMap, values, null);
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
	public JsonSerializer(Registry<DataParser> parsers, ProtocolRegistry protocols, Map<String, ?> variablesMap, Collection<?> values, Scope parent) 
	{
		super(parsers, protocols, variablesMap, values, parent);
	}
	
	@Override
	public <T> String Var(String name, T value, boolean isValue) 
	{
		return Code((isValue ? "$\"" : "\"") + name + "\" = " + getParsers().toString(value, 0, 0, this, getProtocols(), isGenerateComments()) + (generateComments ? ", //Object of " + value.getClass().getName() + ": \"" + value + "\" inserted manually! Stored by \"" + name + "\" variable!" : ""));
	}
	
	@Override
	public Object put(String variableName, Object variableValue) 
	{
		if (isOneOf(variableName.charAt(0), '"', '\'') && isOneOf(variableName.charAt(variableName.length()-1), '"', '\''))
			variableName = variableName.substring(1, variableName.length()-1);
		return super.put(variableName, variableValue);
	}
	
	@Override
	public JsonSerializer emptyClone(Scope parent) 
	{
		JsonSerializer srl = emptyClone(new JsonSerializer(), parent);
		srl.setGenerateComments(isGenerateComments());
		return srl;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Scope> S LoadFrom(Reader reader, Object... formatArgs) 
	{
		Scope sc = super.LoadFrom(reader, formatArgs);

		Object jsonScope;
		if (sc.valuesCount() == 1 && (jsonScope = sc.get(0)) instanceof Scope)
			return (S) jsonScope;
		return (S) sc;
	}
	
	@Override
	public <A extends Appendable> A SerializeTo(A source, Object... args) throws IOException 
	{
		int tabs = 0;
		if (args.length > 1 && args[1] instanceof Integer)
			tabs = (int) args[1];
		
		if (tabs == 0 && !(valuesCount() == 1 && variablesCount() <= 0 && get(0) instanceof Scope))
		{
			JussSerializer scope = emptyClone(null);
			scope.add(this);
			return scope.SerializeTo(source, args);
		}
		return super.SerializeTo(source, args);
	}
	
	@Override
	public ParserRegistry getParsers() 
	{
		return parsers != null ? parsers : (parsers = JSON_PARSERS.clone());
	}
	
	/**
	 * This should append serializedVar into source based on arguments, add separator and return source!
	 * 
	 * @since 1.3.2
	 */
	@Override
	protected Appendable appandVar(Appendable source, CharSequence serializedVar, Entry<String, ?> var, int tabs, boolean isLast) throws IOException
	{
		source.append(multilpy('\t', tabs)).append(serializedVar);
		if (isLast)
			return source;
		return source.append(',');
	}
	
	/**
	 * This should append serializedVal into source based on arguments, add separator and return source!
	 * 
	 * @since 1.3.2
	 */
	@Override
	protected Appendable appandVal(Appendable source, CharSequence serializedVal, Object value, int tabs, boolean isLast) throws IOException
	{
		source.append(multilpy('\t', tabs)).append(serializedVal);
		if (isLast || serializedVal != null && indexOfNotInObj(serializedVal, '/') != -1)
			return source;
		return source.append(',');
	}
	
	@Override
	public <A extends Appendable> A SerializeAsSubscope(A source, Object... args) throws IOException 
	{
		return SerializeAsSubscope(source, variablesCount() > 0 ? new char[] {'{', '}'} : new char[] {'[', ']'}, args);
	}
	
	/**
	 * @param jsonSerializer | JsonSerializer to create {@link JussSerializer} from!
	 * 
	 * @return JussSerializer created from JsonSerializer, all values and variables will remain intact!
	 * 
	 * @since 1.3.2
	 */
	public static JsonSerializer fromJussSerializer(JussSerializer jussSerializer)
	{
		if (jussSerializer instanceof JsonSerializer)
			return (JsonSerializer) jussSerializer;
		try 
		{
			return jussSerializer.transformToScope().clone(JsonSerializer.class);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param jsonSerializer | JsonSerializer to create {@link JussSerializer} from!
	 * 
	 * @return JussSerializer created from JsonSerializer, all values and variables will remain intact!
	 * 
	 * @since 1.3.2 (since 1.3.7 moved from {@link JussSerializer} and renamed from "fromJsonSerializer" to "toJussSerializer")
	 */
	public static JussSerializer toJussSerializer(JsonSerializer jsonSerializer)
	{
		try 
		{
			if (jsonSerializer.valuesCount() == 1 && jsonSerializer.variablesCount() == 0 && jsonSerializer.get(0) instanceof Scope)
			{
				GenericScope<String, Object> sc = (GenericScope<String, Object>) jsonSerializer.getScope(0);
				if (sc instanceof Serializer)
					return ((Serializer) sc).transformToScope().clone(JsonSerializer.class);
				return sc.clone(JsonSerializer.class);
			}
			else
				return jsonSerializer.transformToScope().clone(JsonSerializer.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * @param fromObj | Object to create serializer from!
	 * 
	 * @return {@link JsonSerializer} created from given fromObj by mapping obj's fields into variables of created serializer via given fields (fieldNamesToUse) and conversion rules listed below!!<br><br>
	 * Table of specific Object --> JsonSerializer conversions:
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
			    <td>{@link Serializer#LoadFrom(CharSequence)}</td>
		  	</tr>
			<tr>
			    <td>{@link CharSequence} (as http address)</td>
			    <td>Serializer (newInstance) will open connection with url and get + deserialize the content from it if possible!</td>
		  	</tr>
		    <tr>
			    <td>{@link File}</td>
			    <td>{@link Serializer#LoadFrom(File)}</td>
			</tr>
			<tr>
			    <td>{@link Reader}</td>
			    <td>{@link Serializer#LoadFrom(Reader)}</td>
			</tr>
			<tr>
			    <td>{@link InputStream}</td>
			    <td>{@link Serializer#LoadFrom(InputStream)}</td>
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
	 * @throws When something went wrong during deserialization!
	 * 
	 * @since 1.3.5
	 */
	public static JsonSerializer from(Object fromObj) throws Exception
	{
		try
		{
			return from(fromObj, new String[0]);
		}
		catch (IntrospectionException e)
		{
			return new JsonSerializer(fromObj);
		}
	}
	
	/**
	 * @param fromObj | Object to create serializer from!
	 * @param fieldNamesToUse | Array of obj field names to map into scopes variables using getters (read method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link GenericScope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return {@link JsonSerializer} created from given fromObj by mapping obj's fields into variables of created serializer via given fields (fieldNamesToUse) and conversion rules listed below!!<br><br>
	 * Table of specific Object --> JsonSerializer conversions:
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
			    <td>{@link Serializer#LoadFrom(CharSequence)}</td>
		  	</tr>
			<tr>
			    <td>{@link CharSequence} (as http address)</td>
			    <td>Serializer (newInstance) will open connection with url and get + deserialize the content from it if possible!</td>
		  	</tr>
		    <tr>
			    <td>{@link File}</td>
			    <td>{@link Serializer#LoadFrom(File)}</td>
			</tr>
			<tr>
			    <td>{@link Reader}</td>
			    <td>{@link Serializer#LoadFrom(Reader)}</td>
			</tr>
			<tr>
			    <td>{@link InputStream}</td>
			    <td>{@link Serializer#LoadFrom(InputStream)}</td>
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
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often) or when something went wrong during deserialization!
	 * @throws IntrospectionException when there were no PropertyDescriptor found for obj class!
	 * 
	 * @since 1.3.5
	 */
	public static JsonSerializer from(Object fromObj, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		return (JsonSerializer) Serializer.from(new JsonSerializer(), fromObj, fieldNamesToUse);
	}
}
