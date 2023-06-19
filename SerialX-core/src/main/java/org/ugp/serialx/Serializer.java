package org.ugp.serialx;

import static org.ugp.serialx.converters.DataParser.VOID;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.ugp.serialx.converters.BooleanConverter;
import org.ugp.serialx.converters.CharacterConverter;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.NullConverter;
import org.ugp.serialx.converters.NumberConverter;
import org.ugp.serialx.converters.SerializableBase64Converter;
import org.ugp.serialx.converters.StringConverter;
import org.ugp.serialx.converters.imports.ImportConverter;
import org.ugp.serialx.converters.imports.ImportConverter.Imports;
import org.ugp.serialx.converters.imports.ImportsProvider;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

/**
 * {@link org.ugp.serialx.Serializer} is powerful utility class that allows you to serialize any object in Java using custom data format compiled by recursive descent parser consisting of {@link DataParser}s.
 * This class itself is responsible for utility, formating and managing input-output (IO) of content obtained from parsers and protocols as well as their management of their usage!
 * It is instance of {@link Scope} so we can say that this is scope that can serialize itself using system of already mentioned {@link DataParser} and {@link SerializationProtocol}!
 * 
 * @author PETO
 *
 * @since 1.0.0 
 */
@SuppressWarnings("serial")
public abstract class Serializer extends Scope implements ImportsProvider
{	
	/**
	 * Common parsers that can parse primitive datatypes!
	 * 
	 * @since 1.3.2
	 */
	public static final ParserRegistry COMMON_PARSERS = new ParserRegistry(new StringConverter(), new NumberConverter(), new BooleanConverter(), new CharacterConverter(), new NullConverter(), new SerializableBase64Converter());
	
	protected ParserRegistry parsers;
	protected ProtocolRegistry protocols;
	protected Imports imports;
	
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
	public Scope clone() 
	{
		try 
		{
			return clone(getClass());
		}
		catch (Exception e) 
		{
			return new JussSerializer(variables(), values(), getParent());
		}
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
	
	@Override
	public <T> T toObject(Class<T> objClass) throws Exception
	{
		return toObject(objClass, getProtocols());
	}
	
	/**
	 * @see Serializer#into(Object, Serializer, String...)
	 */
	@Override
	public <T> T into(Object obj, String... fieldNamesToUse) throws IntrospectionException, Exception 
	{
		return Serializer.into(obj, this, fieldNamesToUse);
	}
	
	@Override
	public Imports getImports() 
	{
		if (imports == null)
			imports = ImportConverter.IMPORTS.clone();
		return imports;
	}
	
	/**
	 * @param name | Name of variable.
	 * @param value | Value of variable.
	 * 
	 * @return Classic variable expression in general form like [name] = [value].
	 * 
	 * @since 1.1.5
	 */
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
	 * @see Serializer#Stringify(Object...)
	 */
	public String toString(boolean serialize) throws IOException 
	{
		if (serialize)
			return Stringify();
		return super.toString();
	}
	
	/**
	 * @param f | File to write in. This must be a text file.
	 * 
	 * @return String with variables and objects serialized in specific format.
	 * 
	 * @throws IOException if file can't be opened or serialization fails!
	 * 
	 * @since 1.1.5		
	 */
	public void SerializeTo(File f) throws IOException
	{
		SerializeTo(false, f);
	}
	
	/**
	 * @param append | When true, the new objects will be appended to files content (same objects will be also appended if there are some)! 
	 * @param f | File to write in. This must be a text file.
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return String with variables and objects serialized in specific format.
	 * 
	 * @throws IOException if file can't be opened or serialization fails!
	 * 
	 * @since 1.1.5
	 * 
	 * @see Serializer#SerializeTo(Appendable, Object...)
	 */
	public void SerializeTo(boolean append, File f, Object... args) throws IOException
	{
		//double t0 = System.nanoTime();
		BufferedWriter writer = new BufferedWriter(new FileWriter(f, append));

		writer.write(Stringify(args));
		
		writer.close();
	}
	
	/**
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return String with objects serialized in specific format.
	 * 
	 * @since 1.0.0
	 * 
	 * @see Serializer#SerializeTo(Appendable, Object...)
	 */
	public String Stringify(Object... args) throws IOException
	{
		return SerializeTo(new StringBuilder(), args).toString();
	}
	
	/**
	 * @param source | Source {@link OutputStream} to serialize variables and objects into!
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Source {@link OutputStream} with variables and objects serialized in specific format (may or may not be flushed).
	 * 
	 * @since 1.3.2
	 * 
	 * @see Serializer#SerializeTo(Appendable, Object...)
	 */
	public OutputStream SerializeTo(OutputStream outputStream, Object... args) throws IOException
	{
		SerializeTo(new OutputStreamWriter(outputStream), args);
		return outputStream;
	}
	
	/**
	 * @param source | Source {@link Appendable} to serialize variables and objects into!
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Source {@link Appendable} with variables and objects serialized in specific format.
	 * 
	 * @since 1.3.2
	 */
	public abstract <A extends Appendable> A SerializeTo(A source, Object... args) throws IOException;

	/**
	 * @param file | Text file with serialized objects in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized objects and variables in {@link Scope} or empty {@link Scope} if string is empty.
	 * You can use negative indexes to get objects from back of this array since 1.1.0 (-1 = last element)!
	 * 
	 * @throws FileNotFoundException if file does not exist!
	 * 
	 * @since 1.0.0
	 * 
	 * @see Serializer#LoadFrom(Reader, Object...)
	 */
	public <S extends Scope> S LoadFrom(File file, Object... parserArgs) throws FileNotFoundException
	{
		return LoadFrom(new FileReader(file), parserArgs);
	}
	
	/**
	 * @param str | {@link CharSequence} with serialized objects in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized objects and variables in {@link Scope} or empty {@link Scope} if string is empty.
	 * You can use negative indexes to get objects from back of this array since 1.1.0 (-1 = last element)!
	 * 
	 * @since 1.2.5
	 * 
	 * @see Serializer#LoadFrom(Reader, Object...)
	 */
	public <S extends Scope> S LoadFrom(CharSequence str, Object... parserArgs)
	{
		return LoadFrom(new StringReader(str.toString()), parserArgs);
	}
	
	/**
	 * @param stream | Any {@link InputStream} with serialized objects in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized objects and variables in {@link Scope} or empty {@link Scope} if string is empty.
	 * You can use negative indexes to get objects from back of this array since 1.1.0 (-1 = last element)!
	 * 
	 * @since 1.3.2
	 * 
	 * @see Serializer#LoadFrom(Reader, Object...)
	 */
	public <S extends Scope> S LoadFrom(InputStream stream, Object... parserArgs)
	{
		return LoadFrom(new InputStreamReader(stream), parserArgs);
	}
	
	/**
	 * @param reader | Any {@link Reader} with serialized objects in specific format to load.
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return Unserialized objects and variables in {@link Scope} or empty {@link Scope} if string is empty.
	 * You can use negative indexes to get objects from back of this array since 1.1.0 (-1 = last element)!
	 * 
	 * @since 1.2.5
	 */
	public <S extends Scope> S LoadFrom(Reader reader)
	{
		return LoadFrom(reader, true);
	}
	
	/**
	 * @param reader | Reader to read from!
	 * @param parserArgs | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (probably used for formating of incoming information)!
	 * 
	 * @return This scope after loading data from reader (you most likely want to return "this")!
	 * 
	 * @since 1.3.2
	 */
	public abstract <S extends Scope> S LoadFrom(Reader reader, Object... parserArgs);
	
	/**
	 * @return Clone of this {@link Serializer} without variables and values, protocols and parser will remain same!
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
	 * @since 1.3.2 
	 */
	public Serializer emptyClone(Scope parent) throws Exception
	{
		return emptyClone(getClass(), parent);
	}
	
	/**
	 * @param parent | Parent scope of new Serializer instance!
	 * 
	 * @return Clone of this {@link Serializer} without variables and values, protocols and parser will remain same!
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
		newEmptyInstance.parent = parent;
		return newEmptyInstance;
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
					return new GenericScope<>(srl.variables(), srl.values(), srl.getParent());
				}
				return t;
			}
		};
		return (GenericScope<String, Object>) transform(transFunction);
	}
	
	/**
	 * @param source | Source {@link Appendable} to serialize variables and objects into!
	 * @param args | Additional arguments to use, exact usage and behavior of them is based on specific implementation of this function (they should not be serialized)!
	 * 
	 * @return Serialized content of this scope as inner sub-scope of this scope! Wrapped inside of corresponding wrappingBrackets (default { and })!
	 * 
	 * @since 1.3.5
	 */
	public <A extends Appendable> A SerializeAsSubscope(A source, Object... args) throws IOException
	{
		return SerializeAsSubscope(source, new char[] {'{', '}'}, args);
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
	public <A extends Appendable> A SerializeAsSubscope(A source, char[] wrappingBrackets, Object... args) throws IOException
	{
		int tabs = 0;
		if (args.length > 1 && args[1] instanceof Integer)
		{
			tabs = (int) args[1];
			args[1] = tabs + 1;
		}
		source.append(wrappingBrackets[0]);
		if (!isEmpty())
			source = (A) SerializeTo(source.append('\n'), args).append('\n').append(multilpy('\t', tabs));
		return (A) source.append(wrappingBrackets[1]);
	}
		
	/*
	 * @param file | File with specific format content.
	 * @param index | Index of value to get from lowest Scope.
	 * 
	 * @return Value with index in lowest Scope. Similar to Serializer.LoadFrom(file).get(index) however this function is specifically designed to load only that 1 value witch saves alto of performance!
	 * But target value can't be using any variables declared outside and also can't be variable invocation itself! Also there might be some problems with commented code! Also there can't be no variables in file!
	 * 
	 * @since 1.2.5
	 *
	@Deprecated
	public <T> T LoadFrom(File file, int index)
	{
		try 
		{
			return LoadFrom(new FileReader(file), index);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param str | Any {@link CharSequence} with specific format content.
	 * @param index | Index of value to get from lowest Scope.
	 * 
	 * @return Value with index in lowest Scope. Similar to Serializer.LoadFrom(str).get(index) however this function is specifically designed to load only that 1 value witch saves alto of performance!
	 * But target value can't be using any variables declared outside and also can't be variable invocation itself! Also there might be some problems with commented code!
	 * 
	 * @since 1.2.5
	 *
	@Deprecated
	public <T> T LoadFrom(CharSequence str, int index)
	{
		return LoadFrom(new StringReader(str.toString()), index);
	}
	
	/**
	 * @param reader | Any {@link Reader} with specific format content.
	 * @param index | Index of value to get from lowest Scope.
	 * 
	 * @return Value with index in lowest Scope. Similar to Serializer.LoadFrom(reader).get(index) however this function is specifically designed to load only that 1 value witch saves alto of performance!
	 * But target value can't be using any variables declared outside and also can't be variable invocation itself!
	 * 
	 * @since 1.2.5
	 *
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T> T LoadFrom(Reader reader, int index)
	{
		StringBuilder sb = new StringBuilder();
		int semicolon = 0, brackets = 0, quote = 0, vars = 0, multLineCom = -1;
		
		String line;
		try
		{
			BufferedReader lineReader = new BufferedReader(reader);
			while ((line = lineReader.readLine()) != null)
			{
				if (!contains(line = line.trim(), '=', ':') || !(brackets == 0 && quote % 2 == 0))
					for (int i = 0, com = -1, len = line.length(); i < len; i++)
					{
						char ch = line.charAt(i);
						
						if (ch == '/' && i < len-1 && line.charAt(i+1) == '/')
							com++;
						else if (multLineCom <= -1 && ch == '"')
							quote++;
		
						boolean notInObj = brackets == 0 && quote % 2 == 0;
						if (semicolon > index)
						{
							lineReader.close();
							return (T) NULL.toOopNull(DataParser.parseObj(DataParser.REGISTRY, sb.toString(), this));
						}
						
						if (multLineCom > -1 || com > -1) //Is comment
						{
							if (multLineCom > 0 && ch == '*' && i < len-1 && line.charAt(++i) == '/')
								multLineCom = -1;
						}
						else if (ch == '/' && i < len-1)
						{
							char chNext = line.charAt(i+1);
							if (chNext == '*')
								i += multLineCom = 1;
							else
								sb.append(ch);
						}
						/*else if (notInObj && ch == '=')
						{
							vars++;
						}*
						else if (notInObj && isOneOf(ch, ';', ','))
						{
							if (vars > 0)
							{
								vars = 0;
							}
							else 
								semicolon++;
						}
						else
						{
							if (ch == '{' || ch == '[')
								brackets++;
							else if (ch == '}' || ch == ']')
							{
								if (brackets > 0)
									brackets--;
								else
								{
									lineReader.close();
									throw new IllegalArgumentException("Missing closing bracket in: " + line);
								}
							}
							
							if (vars == 0 && semicolon == index)
								sb.append(ch);
						}
					}
				else 
				{
					char lastCh = line.charAt(line.length()-1);
					if (isOneOf(lastCh, '{', '['))
						brackets++;
					if (isOneOf(lastCh, ';', ','))
						vars++;
				}

			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (brackets > 0)
			throw new IllegalArgumentException("Unclosed brackets!");
		else if (quote % 2 != 0)
			throw new IllegalArgumentException("Unclosed or missing quotes!");
		else if (!(line = sb.toString()).isEmpty())
			return (T) NULL.toOopNull(DataParser.parseObj(DataParser.REGISTRY, line, this));
		LogProvider.instance.logErr("Value with index " + index + " is out of bounds!");
		return null;
	}

	/**
	 * @param file | File with specific format content.
	 * @param varName | Name of variable to load!
	 * 
	 * @return Value of variable with varName in lowest Scope. Similar to Serializer.LoadFrom(file).get(varName) however this function is specifically designed to load only that 1 variable witch saves alto of performance!
	 * But target variable can't be using any variables declared outside! Also there might be some problems with commented code!
	 * 
	 * @since 1.2.5
	 *
	@Deprecated
	public <T> T LoadFrom(File file, String varName)
	{
		try 
		{
			return LoadFrom(new FileReader(file), varName);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @param str | Any {@link CharSequence} with specific format content.
	 * @param varName | Name of variable to load!
	 * 
	 * @return Value of variable with varName in lowest Scope. Similar to Serializer.LoadFrom(str).get(varName) however this function is specifically designed to load only that 1 variable witch saves alto of performance!
	 * But target variable can't be using any variables declared outside! Also there might be some problems with commented code!
	 * 
	 * @since 1.2.5
	 *
	@Deprecated
	public <T> T LoadFrom(CharSequence str, String varName)
	{
		return LoadFrom(new StringReader(str.toString()), varName);
	}
	
	/**
	 * @param reader | Any {@link Reader} with specific format content.
	 * @param varName | Name of variable to load!
	 * 
	 * @return Value of variable with varName in lowest Scope. Similar to Serializer.LoadFrom(reader).get(varName) however this function is specifically designed to load only that 1 variable witch saves alto of performance!
	 * But target variable can't be using any variables declared outside! Also there might be some problems with commented code!
	 * 
	 * @since 1.2.5
	 *
	@Deprecated
	@SuppressWarnings("unchecked")
	public <T> T LoadFrom(Reader reader, String varName)
	{
		StringBuilder sb = new StringBuilder();
		int brackets = 0, quote = 0, multLineCom = -1, fromIndex = 0, fromIndexOrig = 0, findIndex = -1;
		
		String line;
		try
		{
			BufferedReader lineReader = new BufferedReader(reader);
			while ((line = lineReader.readLine()) != null)
			{
				if (findIndex <= -1 && multLineCom <= -1 && line.length() > varName.length() && !(line.charAt(0) == '/' && isOneOf(line.charAt(1), '/', '*')) && (findIndex = line.indexOf(varName)) > -1)
				{
					fromIndexOrig = fromIndex = findIndex;
					findIndex+=sb.length();
				}

				for (int com = -1, len = line.length(); fromIndex < len; fromIndex++)
				{
					char ch = line.charAt(fromIndex);

					if (ch == '/' && fromIndex < len-1 && line.charAt(fromIndex+1) == '/')
						com++;
					else if (multLineCom <= -1 && ch == '"')
						quote++;

					if (findIndex > -1 && quote % 2 == 0 && brackets == 0 && isOneOf(ch, ';', ','))
					{
						lineReader.close();
						int start = sb.indexOf("=", findIndex-fromIndexOrig);
						if (start <= -1)
							start = sb.indexOf(":", findIndex-fromIndexOrig);
						return (T) NULL.toOopNull(DataParser.parseObj(DataParser.REGISTRY, sb.substring(start+1), this));
					}
					
					if (multLineCom > -1 || com > -1) //Is comment
					{
						if (multLineCom > 0 && ch == '*' && fromIndex < len-1 && line.charAt(++fromIndex) == '/')
							multLineCom = -1;
					}
					else if (ch == '/' && fromIndex < len-1)
					{
						char chNext = line.charAt(fromIndex+1);
						if (chNext == '*')
							fromIndex = multLineCom = 1;
						else
							sb.append(ch);
					}
					else 
					{
						if (ch == '{' || ch == '[')
							brackets++;
						else if (ch == '}' || ch == ']')
						{
							if (brackets > 0)
								brackets--;
							else
							{
								lineReader.close();
								throw new IllegalArgumentException("Missing closing bracket in: " + line);
							}
						}
	
						sb.append(ch);
					}
				}
				fromIndex = 0;
			}
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}

		LogProvider.instance.logErr("Variable " + varName + " was not found!");
		return null;
	}*/

	/**
	 * @param f | Source file.
	 * 
	 * @return All lines from source file as string.
	 * @throws IOException 
	 * 
	 * @since 1.1.5
	 */
	public static String LoadFileToString(File f) throws IOException
	{
		return LoadFileToString(f, 1);
	}
	
	/**
	 * @param f | Source file.
	 * @param endlMode | 0 = no line brakes, 1 = always line brakes, 2 = line break only when contains with "//"! <br>
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link Serializer#LoadFileToString(File)} which is doing this automatically!
	 * 
	 * @return Content of file as string.
	 * @throws IOException 
	 * 
	 * @since 1.2.0
	 */
	public static String LoadFileToString(File f, int endlMode) throws IOException
	{
		return StreamToString(new FileReader(f), endlMode);
	}
	
	/**
	 * @param input | Input stream to read to string!
	 * @param endlMode | 0 = no line brakes, 1 = always line brakes, 2 = line break only when contains with "//"! <br>
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link Serializer#LoadFileToString(File)} which is doing this automatically!
	 * 
	 * @return Reader converted to string form!
	 * 
	 * @throws IOException
	 * 
	 * @since 1.3.5
	 */
	public static String StreamToString(InputStream input, int endlMode) throws IOException
	{
		return StreamToString(new InputStreamReader(input), endlMode);
	}
	
	/**
	 * @param input | Input reader!
	 * @param endlMode | 0 = no line brakes, 1 = always line brakes, 2 = line break only when contains with "//"! <br>
	 * Note: You almost always want endlMode on 1. So thats why you should use {@link Serializer#LoadFileToString(File)} which is doing this automatically!
	 * 
	 * @return Reader converted to string form!
	 * 
	 * @throws IOException
	 * 
	 * @since 1.3.2
	 */
	public static String StreamToString(Reader input, int endlMode) throws IOException
	{
		String l;
		StringBuilder sb = new StringBuilder();

		BufferedReader r = new BufferedReader(input);
		while ((l = r.readLine()) != null)
		{
			sb.append(l);
			if (endlMode == 1 || (endlMode > 1 && l.contains("//")))
				sb.append("\n");
		}
		r.close();
		return sb.toString();
	}
	
	/**
	 * @param cls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.2.2
	 */
	public static Object InvokeStaticFunc(Class<?> cls, String name, Object... args) throws InvocationTargetException
	{
		return InvokeFunc(null, cls, name, args);
	}
	
	/**
	 * @param obj | The object the underlying method is invoked from!
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.3.5
	 */
	public static Object InvokeFunc(Object obj, String name, Object... args) throws InvocationTargetException
	{
		return InvokeFunc(obj, obj.getClass(), name, args);
	}
	
	/**
	 * @param obj | The object the underlying method is invoked from!
	 * @param cls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.3.5
	 */
	public static Object InvokeFunc(Object obj, Class<?> objCls, String name, Object... args) throws InvocationTargetException
	{
		Object result = InvokeFunc(obj, objCls, name, ToClasses(args), args);
		if (result != null)
			return result;
		result = InvokeFunc(obj, objCls, name, ToClasses(false, args), args);
		if (result == null)
			LogProvider.instance.logErr("Unable to call function \"" + name + "\" because inserted arguments " + Arrays.asList(args) + " cannot be applied or function does not exist in required class!", null);
		return result;
	}
	
	/**
	 * @param obj | The object the underlying method is invoked from!
	 * @param cls | Class to invoke method from.
	 * @param name | Name of public static method to be called.
	 * @param argClasses | Classes of args.
	 * @param args | Arguments of method. Arguments should be certain if method is overloaded!
	 * 
	 * @return The returned result of called method or {@link Serializer#VOID} if return type of method is void. If something when wrong you will be notified and null will be returned.
	 * 
	 * @throws InvocationTargetException if called method throws and exception while calling!
	 * 
	 * @since 1.3.5
	 */
	public static Object InvokeFunc(Object obj, Class<?> objCls, String name, Class<?>[] argClasses, Object... args) throws InvocationTargetException
	{
		try 
		{
			Method method = objCls.getMethod(name, argClasses);
			Object resualt = method.invoke(obj, args);
			return method.getReturnType().equals(void.class) ? VOID : resualt;
		}
		catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException find) 
		{
			for (Method method : objCls.getMethods()) 
				if (method.getName().equals(name))
					try
					{
						Object resualt = method.invoke(obj, args);
						return method.getReturnType().equals(void.class) ? VOID : resualt;
					}
					catch (IllegalArgumentException e) 
					{}
					catch (SecurityException | IllegalAccessException ex)
					{
						ex.printStackTrace();
					}
		}
		return null;
	}
	
	
	//Syntactical analyzes and fast string utility:
	
	/**
	 * @param obj | Object to clone.
	 * 
	 * @return Cloned object using {@link DataParser}, {@link DataConverter} and {@link SerializationProtocol} or the same object as inserted one if cloning is not possible, for instance when protocol was not found and object is not instance of {@link Cloneable}.
	 * This clone function will always prioritized the Protocol variation, regular cloning is used only when there is no protocol registered or exception occurs. <br>
	 * Note: If there are protocols to serialize inserted object and all its sub-objects and variables then this clone will be absolute deep copy, meaning that making any changes to this cloned object or to its variables will not affect original one in any way! 
	 * But keep in mind that this clone is absolute hoverer, based on protocols used, it does not need to be an 100% copy!
	 * 
	 * @since 1.2.2
	 */
	public static <T> T Clone(T obj)
	{
		return Clone(obj, DataParser.REGISTRY, new Object[] {}, new Scope());
	}
	
	/**
	 * @param obj | Object to clone.
	 * @param 
	 * @param converterArgs | Argument for {@link DataConverter#objToString(Registry, Object, Object...)}!
	 * @param parserArgs | Arguments for {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)}!
	 * 
	 * @return Cloned object using {@link DataParser}, {@link DataConverter} and {@link SerializationProtocol} or the same object as inserted one if cloning is not possible, for instance when protocol was not found and object is not instance of {@link Cloneable}.
	 * This clone function will always prioritized the Protocol variation, regular cloning is used only when there is no protocol registered or exception occurs. <br>
	 * Note: If there are protocols to serialize inserted object and all its sub-objects and variables then this clone will be absolute deep copy, meaning that making any changes to this cloned object or to its variables will not affect original one in any way! 
	 * But keep in mind that this clone is absolute hoverer, based on protocols used, it does not need to be an 100% copy!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T Clone(T obj, Registry<DataParser> parsersToUse, Object[] converterArgs, Object... parserArgs)
	{
		if (obj == null) 
			return obj;
		else if (obj.getClass() == Byte.class)
			return (T) new Byte((byte) obj);
		else if (obj.getClass() == Short.class)
			return (T) new Short((short) obj);
		else if (obj.getClass() == Integer.class)
			return (T) new Integer((int) obj);
		else if (obj.getClass() == Long.class)
			return (T) new Long((long) obj);
		else if (obj.getClass() == Float.class)
			return (T) new Float((float) obj);
		else if (obj.getClass() == Double.class)
			return (T) new Double((double) obj);
		else if (obj.getClass() == Character.class)
			return (T) new Character((char) obj);
		else if (obj.getClass() == Boolean.class)
			return (T) new Boolean((boolean) obj);
		else if (obj.getClass() == String.class)
			return (T) new String((String) obj);
		else
		{
			ParserRegistry parsers = parsersToUse instanceof ParserRegistry ? (ParserRegistry) parsersToUse : new ParserRegistry(parsersToUse);
			
			Object cln = NULL.toOopNull(parsers.parse(parsers.toString(obj, converterArgs).toString(), parserArgs));
			if (cln != null && cln != VOID)
				return (T) cln;
			
			if (obj instanceof Cloneable)
			{
				try 
				{
					Method method = Object.class.getDeclaredMethod("clone");
					method.setAccessible(true);
					return (T) method.invoke(obj);
				} 
				catch (Exception e) 
				{
					throw new RuntimeException(e);
				}
			}
			LogProvider.instance.logErr("Unable to clone " + obj.getClass() + ": " + obj, null);
			return obj;
		}
	}
	
	/**
	 * @param cls | Class to instantiate.
	 * 
	 * @return New blank instance of required class created by calling shortest public constructor with default values!<br>
	 * Note: Do not use this when your class contains final fields!
	 * 
	 * @throws NoSuchMethodException if there is no public constructor!
	 * @throws InvocationTargetException if called constructor throws and exception!
	 * 
	 * @since 1.2.2
	 */
	public static <T> T Instantiate(Class<T> cls) throws NoSuchMethodException, InvocationTargetException
	{
		return Instantiate(cls, true);
	}
	
	/**
	 * @param cls | Class to instantiate.
	 * @param publicOnly | If true, only public constructors will be used to create the object!
	 * 
	 * @return New blank instance of required class created by calling shortest constructor with default values!<br>
	 * Note: Do not use this when your class contains final fields!
	 * 
	 * @throws NoSuchMethodException if there is no public constructor!
	 * @throws InvocationTargetException if called constructor throws and exception!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T Instantiate(Class<T> cls, boolean publicOnly) throws NoSuchMethodException, InvocationTargetException
	{
		try
		{
			Constructor<T> cons = publicOnly ? cls.getConstructor() : cls.getDeclaredConstructor();
			if (!publicOnly)
				cons.setAccessible(true);
			return cons.newInstance();
		}
		catch (Exception e) 
		{
			try
			{
				Constructor<?>[] cnstrs = publicOnly ? cls.getConstructors() : cls.getDeclaredConstructors();
				if (cnstrs.length <= 0)
					throw new NoSuchMethodException("No public constructors in class " + cls.getName() + "!");
				
				for (int i = 1; i < cnstrs.length; i++) 
				{
					if (!publicOnly)
						cnstrs[0].setAccessible(true);
					if (cnstrs[i].getParameterCount() < cnstrs[0].getParameterCount())
						cnstrs[0] = cnstrs[i];
				}
				
				Object[] args = new Object[cnstrs[0].getParameterCount()];
				Class<?>[] argTypes = cnstrs[0].getParameterTypes();
				for (int i = 0; i < cnstrs[0].getParameterCount(); i++) 
				{
					if (argTypes[i] == byte.class)
						args[i] = (byte) 0;
					else if (argTypes[i] == short.class)
						args[i] = (short) 0;
					else if (argTypes[i] == int.class)
						args[i] = 0;
					else if (argTypes[i] == long.class)
						args[i] = 0l;
					else if ( argTypes[i] == float.class)
						args[i] = 0.0f;
					else if (argTypes[i] == double.class)
						args[i] = 0.0;
					else if (argTypes[i] == char.class)
						args[i] = (char) 0;
					else if (argTypes[i] == boolean.class)
						args[i] = false;
					else if (argTypes[i] == String.class)
						args[i] = "";
					else
						args[i] = null;
				}
				return (T) cnstrs[0].newInstance(args);
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException e2) 
			{
				e2.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * @param objs | Array of objects.
	 * 
	 * @return Array of inserted objects class types. Wrapper types of primitive values will be converted to primitive types! For instance: Integer.class -> int.class
	 * 
	 * @since 1.2.2
	 */
	public static Class<?>[] ToClasses(Object... objs)
	{
		return ToClasses(true, objs);
	}
	
	/**
	 * @param objs | Array of objects.
	 * 
	 * @return Array of inserted objects class types. Wrapper types of primitive values will be converted to primitive types! For instance: Integer.class -> int.class
	 * 
	 * @since 1.3.5
	 */
	public static Class<?>[] ToClasses(boolean unwrapp, Object... objs)
	{
		Class<?>[] classes = new Class<?>[objs.length];
		if (unwrapp)
		{
			for (int i = 0; i < classes.length; i++) 
			{
				if (objs[i] == null)
					classes[i] = Object.class;
				else if (objs[i].getClass() == Byte.class || objs[i] == Byte.class)
					classes[i] = byte.class;
				else if (objs[i].getClass() == Short.class || objs[i] == Short.class)
					classes[i] = short.class;
				else if (objs[i].getClass() == Integer.class || objs[i] == Integer.class)
					classes[i] = int.class;
				else if (objs[i].getClass() == Long.class || objs[i] == Long.class)
					classes[i] = long.class;
				else if (objs[i].getClass() == Float.class || objs[i] == Float.class)
					classes[i] = float.class;
				else if (objs[i].getClass() == Double.class || objs[i] == Double.class)
					classes[i] = double.class;
				else if (objs[i].getClass() == Character.class || objs[i] == Character.class)
					classes[i] = char.class;
				else if (objs[i].getClass() == Boolean.class || objs[i] == Boolean.class)
					classes[i] = boolean.class;
				else if (objs[i] instanceof Class)
					classes[i] = (Class<?>) objs[i];
				else
					classes[i] = objs[i].getClass();
			}
			
			return classes;
		}
		
		for (int i = 0; i < classes.length; i++) 
			classes[i] = objs[i].getClass();
		return classes;
	}
	
	/**
	 * @param ch | String to multiply!
	 * @param times | Count of multiplication!
	 * 
	 * @return Multiplied char, for example <code>multilpy('a', 5)</code> will return "aaaaa";
	 * 
	 * @since 1.3.2
	 */
	public static StringBuilder multilpy(char ch, int times)
	{
		StringBuilder sb = new StringBuilder();
		while (times-- > 0)
			sb.append(ch);
		return sb;
	}
	
	/**
	 * @param ch | String to multiply!
	 * @param str | Count of multiplication!
	 * 
	 * @return Multiplied char, for example <code>multilpy("a", 5)</code> will return "aaaaa";
	 * 
	 * @since 1.3.0
	 */
	public static StringBuilder multilpy(CharSequence str, int times)
	{
		StringBuilder sb = new StringBuilder(str);
		while (times-- > 1)
			sb.append(str);
		return sb;
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters. If there is more than one splitter in row, it will be taken as one whole!
	 * 
	 * @since 1.0.0
	 */
	public static String[] splitValues(String s, char... splitter)
	{
		return splitValues(s, 0, true, splitter);
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param oneOrMore | If true, string will be splitted after one or more splitters in row, if false splitting will occur only after single splitter (this is similar to "+" in regex)!
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. If there is more than one splitter in row, it will be taken as one whole!
	 * 
	 * @since 1.3.0
	 */
	public static String[] splitValues(String s, int limit, boolean splitAfterSingleCharOnly, char... splitter)
	{
		return splitValues(s, limit, splitAfterSingleCharOnly, new char[0], splitter);
	}
	
	/**
	 * @param s | String to split and check some syntax.
	 * @param limit | If 0 or less = no limit, 1 = no splitting, more than 1 = count of results!
	 * @param oneOrMore | If true, string will be splitted after one or more splitters in row, if false splitting will occur only after single splitter (this is similar to "+" in regex)!
	 * @param splitBreaks | When some of these characters is encountered, splitting is terminated for the rest of the string! 
	 * @param splitter | Chars where string will be split!
	 * 
	 * @return String splitted after splitters according to arguments. If there is more than one splitter in row, it will be taken as one whole!
	 * 
	 * @since 1.3.5
	 */
	public static String[] splitValues(String s, int limit, boolean oneOrMore, char[] splitBreaks, char... splitter)
	{
		if (splitter.length <= 0 || limit == 1)
			return new String[] {s};
//		
//		if (isOneOf(s.charAt(0), splitter))
//			return splitValues(" "+s, limit, oneOrMore, splitBreaks, splitter);
		
		List<String> result = new ArrayList<>();

		int brackets = 0, quote = 0, lastIndex = 0;
		for (int i = 0, count = 1, len = s.length(), oldCh = splitter[0]; i < len && (limit <= 0 || count < limit); i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
			
			if (quote % 2 == 0)
			{
				if (isOneOf(ch, splitBreaks))
				{
					brackets = quote = 0;
					break;
				}
				
				if (brackets == 0 && oldCh != ch && isOneOf(ch, splitter) && (oneOrMore || (i >= len-1 || !isOneOf(s.charAt(i+1), splitter))))
				{	
					result.add(s.substring(lastIndex == 0 ? 0 : lastIndex + 1, lastIndex = i).trim());
					count++;
				}
				else if (ch == '{' || ch == '[')
					brackets++;
				else if (ch == '}' || ch == ']')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing opening bracket in: " + s);
				}
			}
			oldCh = ch;
		}

		if (brackets > 0)
			throw new IllegalArgumentException("Unclosed brackets in: " + s);
		else if (quote % 2 != 0)
			throw new IllegalArgumentException("Unclosed or missing quotes in: " + s);
		else
		{
			result.add(s.substring(lastIndex == 0 ? 0 : lastIndex + 1, s.length()).trim());
		}
		
		return result.toArray(new String[0]);
	}

	/**
	 * @param s | CharSequence to search!
	 * @param oneOf | Characters to find!
	 * 
	 * @return Index of first found character that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfNotInObj(CharSequence s, char... oneOf)
	{
		return indexOfNotInObj(s, true, oneOf);
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param firstIndex | If true, first index will be returned, if false last index will be returned.
	 * @param oneOf | Characters to find!
	 * 
	 * @return Index of first found character that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfNotInObj(CharSequence s, boolean firstIndex, char... oneOf)
	{
		int found = -1;
		for (int i = 0, brackets = 0, quote = 0, len = s.length(); i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
	
			if (quote % 2 == 0)
			{
				if (brackets == 0 && /*oneOf.length == 0 ? ch == oneOf[0] :*/ isOneOf(ch, oneOf))
				{
					found = i;
					if (firstIndex)
						return found;
				}
				else if (ch == '{' || ch == '[')
					brackets++;
				else if (ch == '}' || ch == ']')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing closing bracket in: " + s);
				}
			}
		}
		return found;
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param sequenceToFind | CharSequence to find!
	 * 
	 * @return Index of first found CharSequence that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.0
	 */
	public static int indexOfNotInObj(CharSequence s, CharSequence sequenceToFind)
	{
		return indexOfNotInObj(s, sequenceToFind, true);
	}
	
	/**
	 * @param s | CharSequence to search!
	 * @param sequenceToFind | CharSequence to find!
	 * @param firstIndex | If true, first index will be returned, if false last index will be returned.
	 * 
	 * @return Index of first found CharSequence that is not in object meaning it is not in string nor between '{' or '[' and ']' or '}'!
	 * 
	 * @since 1.3.5
	 */
	public static int indexOfNotInObj(CharSequence s, CharSequence sequenceToFind, boolean firstIndex)
	{
		int found = -1;
		for (int i = 0, brackets = 0, quote = 0, match = 0, len = s.length(), lenToFind = sequenceToFind.length(); i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
			
			if (quote % 2 == 0)
			{
				if (brackets == 0 && ch == sequenceToFind.charAt(match++))
				{
					if (match == lenToFind)
					{
						found = i - match + 1;
						if (firstIndex)
							return found;
						match = 0;
					}
				}
				else if (ch == '{' || ch == '[')
					brackets++;
				else if (ch == '}' || ch == ']')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing closing bracket in: " + s);
				}
				else
					match = 0;
			}
		}
		return found;
	}
	
	/**
	 * @param str | String to do replacements in!
	 * @param target | Target to replace!
	 * @param replacement | Replacement for target!
	 * 
	 * @return Inserted string after replacing all targets with replacements similar to {@link String#replace(CharSequence, CharSequence)} but faster!
	 * 
	 * @since 1.2.0
	 */
	public static String fastReplace(String str, String target, CharSequence replacement) 
	{
	    int targetLength = target.length();
	    if (targetLength == 0) 
	        return str;
	    
	    int i1 = 0, i2 = str.indexOf(target);
	    if (i2 < 0) 
	        return str;
	    
	    StringBuilder sb = new StringBuilder(targetLength > replacement.length() ? str.length() : str.length() * 2);
	    do 
	    {
	        sb.append(str, i1, i2).append(replacement);
	        i1 = i2 + targetLength;
	        i2 = str.indexOf(target, i1);
	    } while (i2 > 0);
	    
	    return sb.append(str, i1, str.length()).toString();
	}
	
	/**
	 * @param ch | Char to compare!
	 * @param chars | Chars to match!
	 * 
	 * @return True if inserted char is any of inserted chars!
	 * 
	 * @since 1.3.0 
	 */
	public static boolean isOneOf(int ch, char... chars)
	{
		if (chars.length > 0)
		{
			for (int i = 0, len = chars.length; i < len; i++)
				if (chars[i] == ch)
					return true;
		}
		return false;
	}
	
	/**
	 * @return {@link String#contains(CharSequence)} for char sequence!
	 * 
	 * @since 1.3.0
	 */
	public static boolean contains(CharSequence str, char... oneOf)
	{
		for (int i = 0, len = str.length(); i < len; i++) 
			if (isOneOf(str.charAt(i), oneOf))
				return true;
		return false;
	}
	
	/**
	 * @param str | String to display!
	 * @param pos | Position to display!
	 * 
	 * @return String with displayed position!
	 * Use for debugging or error printing!
	 * 
	 * @since 1.3.2
	 */
	public static String showPosInString(CharSequence str, int pos)
	{
		return str + "\n" + multilpy(' ', pos) + "^";	
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
			    <td>{@link Serializer#SerializeTo(File)}</td>
			</tr>
		    <tr>
			    <td>{@link Appendable}</td>
			    <td>{@link Serializer#SerializeTo(Appendable)}</td>
			</tr>
			<tr>
			    <td>{@link OutputStream}</td>
			    <td>{@link Serializer#SerializeTo(OutputStream)}</td>
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
			fromSerializer.SerializeTo((File) obj);
			return (T) obj;
		}
		
		if (obj instanceof Appendable)
		{
			fromSerializer.SerializeTo((Appendable) obj);
			return (T) obj;
		}
		
		if (obj instanceof OutputStream)
		{
			fromSerializer.SerializeTo((OutputStream) obj);
			return (T) obj;
		}
		
		if (obj instanceof URL)
		{
			URLConnection con = ((URL) obj).openConnection();
			con.setDoOutput(true);
			if (con instanceof HttpURLConnection)
				post(fromSerializer, (HttpURLConnection) con);
			else
				fromSerializer.SerializeTo(con.getOutputStream());
			return (T) con;
		}
		
		if (obj instanceof URLConnection)
		{
			if (obj instanceof HttpURLConnection)
				post(fromSerializer, (HttpURLConnection) obj);
			else
				fromSerializer.SerializeTo(((URLConnection) obj).getOutputStream());
			return (T) obj;
		}
		
		try
		{
			if (obj instanceof CharSequence)
			{
				if (indexOfNotInObj((CharSequence) obj, "http") == 0)
				{
					URLConnection con = new URL(obj.toString()).openConnection();
					con.setDoOutput(true);
					if (con instanceof HttpURLConnection)
						post(fromSerializer, (HttpURLConnection) con);
					else
						fromSerializer.SerializeTo(con.getOutputStream());
					return (T) con;
				}
			
				try
				{
					File file = new File(obj.toString());
					fromSerializer.SerializeTo(file);
					return (T) file;
				}
				catch (Exception e)
				{}
			}
		}
		catch (IOException e)
		{}
		
		return Scope.into(obj, fromSerializer, fieldNamesToUse);
	}
	
	/**
	 * @param newInstance | New instance of specific {@link Serializer}
	 * @param fromObj | Object to create serializer from!
	 * @param fieldNamesToUse | Array of obj field names to map into scopes variables using getters (read method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link GenericScope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
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
	public static Serializer from(Serializer newInstance, Object fromObj, String... fieldNamesToUse) throws Exception, IntrospectionException
	{
		if (fromObj instanceof CharSequence)
		{
			if (indexOfNotInObj((CharSequence) fromObj, "http") == 0)
				try
				{
					return newInstance.LoadFrom(new URL(fromObj.toString()).openStream());
				}
				catch (IOException e)
				{}
			
			try
			{
				return newInstance.LoadFrom(new File(fromObj.toString()));
			}
			catch (Exception e)
			{}

			return newInstance.LoadFrom((CharSequence) fromObj);
		}
		
		if (fromObj instanceof File)
			return newInstance.LoadFrom((File) fromObj);
		if (fromObj instanceof Reader)
			return newInstance.LoadFrom((Reader) fromObj);
		if (fromObj instanceof InputStream)
			return newInstance.LoadFrom((InputStream) fromObj);
		if (fromObj instanceof URL)
			return newInstance.LoadFrom(((URL) fromObj).openStream());
		if (fromObj instanceof URLConnection)
			return newInstance.LoadFrom(((URLConnection) fromObj).getInputStream());
		
		newInstance.addAll(Scope.from(fromObj, fieldNamesToUse));
		return newInstance;
	}
	
	/**
	 * This will serialize serializer into http query post request however this is not the best networking and you should implement your own http client if you want SerialX to serialize and deserialize remote content!
	 * 
	 * @param serializer | Serializer to post.
	 * @param conn | Http connection to use!
	 * 
	 * @throws IOException if posting failed!
	 * 
	 * @since 1.3.5
	 */
	public static void post(Serializer serializer, HttpURLConnection conn) throws IOException
	{
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : serializer.varEntrySet()) 
        {
            if (postData.length() != 0) 
            	postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8")).append('=');
            postData.append(URLEncoder.encode(serializer.getParsers().toString(param.getValue()).toString(), "UTF-8"));
        }
        
        for (Object param : serializer) 
        {
            if (postData.length() != 0) 
            	postData.append('&');
            postData.append(URLEncoder.encode(serializer.getParsers().toString(param).toString(), "UTF-8"));
        }
        
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
	}
	
	/**
	 * This is a "dummy" class that {@link Serializer} uses internally as an OOP programmatic interpretation of null. In otherwise this is wrapper object for null.
	 * Note: You should not be able to come in contact with this during serialization and loading, if you did then you most likely encountered and bug and you should report it!
	 * 
	 * @author PETO
	 *
	 * @since 1.2.2
	 */
	public static final class NULL
	{
		public static Object toOopNull(Object obj)
		{
			return obj == null ? new NULL() : obj;
		}
		
		@Override
		public boolean equals(Object obj) 
		{
			return obj == null || obj instanceof NULL;
		}
		
		@Override
		public String toString() 
		{
			return "null";
		}
	}
}