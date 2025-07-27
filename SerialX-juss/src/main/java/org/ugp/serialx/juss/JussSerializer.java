package org.ugp.serialx.juss;

import static org.ugp.serialx.Utils.Clone;
import static org.ugp.serialx.Utils.ENDL;
import static org.ugp.serialx.Utils.InvokeStaticFunc;
import static org.ugp.serialx.Utils.indexOfNotInObj;
import static org.ugp.serialx.Utils.isOneOf;
import static org.ugp.serialx.Utils.multilpy;
import static org.ugp.serialx.converters.DataParser.VOID;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.Utils;
import org.ugp.serialx.converters.BooleanConverter;
import org.ugp.serialx.converters.CharacterConverter;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.DataParser.ParserRegistry;
import org.ugp.serialx.converters.NullConverter;
import org.ugp.serialx.converters.NumberConverter;
import org.ugp.serialx.converters.SerializableBase64Converter;
import org.ugp.serialx.converters.StringConverter;
import org.ugp.serialx.converters.imports.ImportsProvider;
import org.ugp.serialx.juss.converters.ArrayConverter;
import org.ugp.serialx.juss.converters.ImportConverter;
import org.ugp.serialx.juss.converters.ObjectConverter;
import org.ugp.serialx.juss.converters.OperationGroups;
import org.ugp.serialx.juss.converters.VariableConverter;
import org.ugp.serialx.protocols.SerializationProtocol.ProtocolRegistry;

/**
 * This is implementation of {@link Serializer} for serializing in default SerialX API implementation known as JUSS (Java universal serial script) which is Json like domain specific language that has extended functionality!
 * It should generate and work with .juss or .srlx files!
 * 
 * @author PETO
 *
 * @since 1.3.2
 */
@SuppressWarnings("serial")
public class JussSerializer extends Serializer implements ImportsProvider
{
	/**
	 * {@link ParserRegistry} with all parsers required to parse JUSS!
	 * 
	 * @since 1.3.2
	 */
	public static final ParserRegistry JUSS_PARSERS = new ParserRegistry(new ImportConverter(), new OperationGroups(), new VariableConverter(), new StringConverter(), new ObjectConverter(), new ArrayConverter(), new NumberConverter(), new BooleanConverter(), new CharacterConverter(), new NullConverter(), new SerializableBase64Converter());
	
	/**
	 * {@link ParserRegistry} with all parsers required to parse JUSS with additional operators.
	 * <br>
	 * Since 1.3.8 this requires "org.ugp.serialx.converters.Operators" from SerialX "operators" modules to be present on the classpath!
	 * 
	 * @since 1.3.2
	 */
	public static final ParserRegistry JUSS_PARSERS_AND_OPERATORS;
	
	static
	{
		JUSS_PARSERS.add(0, new ImportConverter());
		JUSS_PARSERS_AND_OPERATORS = JUSS_PARSERS.clone();
		
		try 
		{
			InvokeStaticFunc(Class.forName("org.ugp.serialx.converters.Operators"), "install", JUSS_PARSERS_AND_OPERATORS);
		} 
		catch (Exception e) 
		{}
	}
	
	protected Imports imports;
	
	/**
	 * @deprecated (1.3.9) SET 0b10 BIT OF {@link Serializer#getFormat()} TO 1 INSTEAD IF YOU WANT TO SET THIS ON TRUE. OR SIMPLY USE {@link JussSerializer#setGenerateComments(boolean)}
	 * 
	 * Set this on true and program will generate comments and report!<br>
	 * Note: Keep this on false to achieve the best performance!
	 * 
	 * @since 1.0.5
	 */
	@Deprecated
	protected boolean generateComments = false;
	
	/**
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JussSerializer(Object... values) 
	{
		this(null, values);
	}
	
	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JussSerializer(Map<String, ?> variablesMap, Object... values) 
	{
		this(variablesMap, values == null ? null : new ArrayList<>(Arrays.asList(values)));
	}
	
	/**
	 * @param sourceScope | Scope with initial content!
	 * 
	 * @since 1.3.5
	 */
	public JussSerializer(GenericScope<String, ?> sourceScope)
	{
		this(sourceScope == null ? null : sourceScope.variables(), sourceScope == null ? null : sourceScope.values());
	}

	/**
	 * @param variablesMap | Initial variables to be added in to this scope!
	 * @param values | Initial independent values to be added in to this scope!
	 * 
	 * @since 1.3.2
	 */
	public JussSerializer(Map<String, ?> variablesMap, Collection<?> values) 
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
	public JussSerializer(Registry<DataParser> parsers, Map<String, ?> variablesMap, Collection<?> values) 
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
	public JussSerializer(Registry<DataParser> parsers, ProtocolRegistry protocols, Map<String, ?> variablesMap, Collection<?> values, Scope parent) 
	{
		super(parsers, protocols, variablesMap, values, parent);
	}
	
	@Override
	public JussSerializer emptyClone(Scope parent)
	{
		JussSerializer srl = emptyClone(new JussSerializer(), parent);
		return srl;
	}
	
	@Override
	public ParserRegistry getParsers() 
	{
		return parsers != null ? parsers : (parsers = JUSS_PARSERS.clone());
	}
	
	@Override
	public Imports getImports() 
	{
		if (imports == null)
			imports = ImportsProvider.IMPORTS.clone();
		return imports;
	}
	
	/**
	 * @param absoluteClone | If true this scope will be cloned using {@link Serializer#Clone(Object, Registry, Object[], Object...)}, if false {@link Scope#clone()}!
	 * 
	 * @return Clone of this scope!
	 * 
	 * @since 1.3.2
	 */
	public Scope clone(boolean absoluteClone) 
	{
		if (absoluteClone)
			return Clone(this, getParsers(), new Object[] {this, -9999, 0, getProtocols(), getFormat()}, this, null, null, getProtocols());
		return super.clone();
	}
	
	/**
	 * @deprecated (1.3.9) DO NOT USE THIS, USE {@link VariableConverter#NewVariable(String, Object)} OR VARIATION OF {@link AbstractMap.SimpleEntry} INSTEAD!
	 * 
	 * @param name | Name of variable.
	 * @param value | Value of variable.
	 * 
	 * @return Variable in JUSS form to serialize as [name] = [value].
	 * 
	 * @since 1.1.5
	 */
	@Deprecated
	@Override
	public <T> String Var(String name, T value)
	{
		return Var(name, value, false);
	}
	
	/**
	 * @deprecated (1.3.9) DO NOT USE THIS, USE {@link VariableConverter#NewVariable(String, Object)} OR VARIATION OF {@link AbstractMap.SimpleEntry} INSTEAD!
	 * 
	 * @param name | Name of variable.
	 * @param value | Value of variable.
	 * @param isValue | True if variables value supposed to by visible also during value loading.
	 * 
	 * @return Variable in JUSS form to serialize as [name] = [value].
	 * 
	 * @since 1.1.5
	 */
	@Deprecated
	public <T> String Var(String name, T value, boolean isValue)
	{
		return Code((isValue ? "$" : "") + name + " = " + getParsers().toString(value, this, 0, 0, getProtocols(), getFormat()) + (isGenerateComments() ? "; //Object of " + value.getClass().getName() + ": \"" + value + "\" inserted manually! Stored by \"" + name + "\" variable!" : ""));
	}
	
	/**
	 * @param cls | Class to invoke static member on!
	 * @param staticMethodName | Name of static member!
	 * @param args | Arguments of method (use this only if you are invoking method no field)!
	 * 
	 * @return Static member invocation in JUSS form to serialize as [class]::[method | field] [args]...
	 * <br><br>Note: This will not invoke member from class directly, it will be invoked during unserialization, also no checks are performed here so this will not notify you if member or class does not exist!<br>
	 * Also this will work only in combination {@link ObjectConverter}!
	 * 
	 * @since 1.2.2
	 */
	public String StaticMember(Class<?> cls, String staticMethodName, Object... args)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0, len = args.length; i < len; i++) 
			sb.append(getParsers().toString(args[i], this, 0, 0, getProtocols(), getFormat())).append(i < len-1 ? " " : "");
		return Code(cls.getName() + "::" + staticMethodName + (args.length <= 0 ? "" : " ") + sb);
	}
	
	/**
	 * @param source | Source {@link Appendable} to serialize variables and objects into!
	 * @param args | Additional arguments to use, in case of {@link JussSerializer} this should be array of length 6 with 0. argument being this pointer to this serializer, 
	 * argument 1. and 2. being integers signifying number of tabs and index of parsing iteration (used primarily by {@link ObjectConverter}), argument 3. containing {@link ProtocolRegistry} of this {@link Serializer}, 
	 * argument 4. being of type {@link Class} containing information about class that is currently being serialized (used primarily by {@link ObjectConverter} and should start as null), 
	 * and argument 5. being byte bit array containing the formating flags ({@link Serializer#getFormat()}) containing the information about if the code comments are supposed to be generated and/or indentation and newline characters inclusion! 
	 * 
	 * @return Source {@link Appendable} with variables and objects serialized in specific format.
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <A extends Appendable> A serializeTo(A source, Object... args) throws IOException
	{	
		Map<String, ?> variables = variables();
		List<Object> objs = values();
		int valuesLen = objs.size(), varLen = variables.size(), i = 0, tabs = 0;
		boolean generateComments = isGenerateComments();
		
		if (args.length < 6)
			args = Arrays.copyOf(args, 6);
		args[0] = this;
		if (args[1] == null)
			args[1] = tabs; //tabs
		else if (args[1] instanceof Integer)
			tabs = (int) args[1];
		if (args[2] == null)
			args[2] = 0; //index
		if (args[3] == null)
			args[3] = getProtocols();
		if (args[5] == null)
			args[5] = getFormat();

		source = source == null ? (A) new StringBuilder() : source;
		if (generateComments && varLen + valuesLen > 0) //Gen scope comment
		{
			if (tabs <= 0)
				source.append("//Date created: ").append(new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss z \n\n").format(new Date()));
			source.append(multilpy('\t', tabs)).append("//Scope serialization summary:\n");
			if (varLen > 0)
				source.append(multilpy('\t', tabs)).append("//").append(varLen+"").append(" variables!\n");

			if (valuesLen > 0)
				source.append(multilpy('\t', tabs)).append("//").append(valuesLen+"").append(" values!\n");
			if (tabs > -1)
				source.append('\n');
		}

		ParserRegistry reg = getParsers();
		
		if (varLen > 0)
		{
			if (generateComments)
			{
				for (Entry<String, ?> var : variables.entrySet()) //Vars
				{
					appandVar(source, reg.toString(var, args), var, tabs, i >= varLen-1 && valuesLen <= 0);
					if (!(var.getValue() instanceof Scope) || ((Scope) var.getValue()).isEmpty())
						GenerateComment(source, reg, var);
					
					if (++i < varLen || valuesLen > 0)
						source.append('\n');
				}
			}
			else if (format != 0)
			{
				for (Entry<String, ?> var : variables.entrySet()) //Vars
				{
					appandVar(source, reg.toString(var, args), var, tabs, i >= varLen-1 && valuesLen <= 0);
					
					if (++i < varLen || valuesLen > 0)
						source.append('\n');
				}
			}
			else // No formating
			{
				for (Entry<String, ?> var : variables.entrySet()) //Vars
				{
					appandVar(source, reg.toString(var, args), var, tabs, i++ >= varLen-1 && valuesLen <= 0);
				}
			}
		}
		
		if (generateComments)
		{
			for (i = 0; i < valuesLen; i++) //Values
			{
				if (i != 0)
					source.append('\n');
				
				Object obj = objs.get(i);
				CharSequence serialized = reg.toString(obj, args);
				
				appandVal(source, serialized, obj, tabs, i == valuesLen-1);
				if (!(obj instanceof Scope) || ((Scope) obj).isEmpty())
					GenerateComment(source, reg, obj);
			} 
		}
		else if (format != 0)
		{
			for (i = 0; i < valuesLen; i++) //Values
			{
				if (i != 0)
					source.append('\n');
				
				Object obj;
				appandVal(source, reg.toString(obj = objs.get(i), args), obj, tabs, i == valuesLen-1);
			} 
		}
		else // No formating
		{
			for (i = 0; i < valuesLen; i++) //Values
			{
				Object obj;
				appandVal(source, reg.toString(obj = objs.get(i), args), obj, tabs, i == valuesLen-1);
			} 
		}

		if (source instanceof Flushable)
			((Flushable) source).flush();
		if (tabs == 0)
			getImports().removeImportsOf(this);
		return source;
	}
	
	/**
	 * This should append serializedVar into source based on arguments, add separator and return source!
	 * 
	 * @since 1.3.2
	 */
	protected Appendable appandVar(Appendable source, CharSequence serializedVar, Entry<String, ?> var, int tabs, boolean isLast) throws IOException
	{
		if (format != 0)
			source.append(multilpy('\t', tabs));
		source.append(serializedVar);
		if (isLast)
			return source;
		return source.append(';');
	}
	
	/**
	 * This should append serializedVal into source based on arguments, add separator and return source!
	 * 
	 * @since 1.3.2
	 */
	protected Appendable appandVal(Appendable source, CharSequence serializedVal, Object value, int tabs, boolean isLast) throws IOException
	{
		if (format != 0)
			source.append(multilpy('\t', tabs));
		source.append(serializedVal);
		if (isLast || serializedVal != null && indexOfNotInObj(serializedVal, "//") != -1)
			return source;
		return source.append(';');
	}
	
	/**
	 * @return Generated description for obj, appended in source!
	 * 
	 * @since 1.3.2
	 */
	public Appendable GenerateComment(Appendable source, ParserRegistry registry, Object obj) throws IOException
	{	
		try
		{
			CharSequence comment = registry.getConverterFor(obj).getDescription(registry, obj);
			if (comment.length() > 0)
				return source.append(" //").append(comment);
		}
		catch (Exception e) 
		{
			return source.append(" //").append(DataConverter.getDefaultDescriptionFor(obj));
		}
		return source;
	}
	
	/**
	 * @param reader | Reader to read from!
	 * @param args | Additional arguments to use. In case of JussSerializer, this should be array of length 4 with 0. argument will being this pointer of this serializer, 
	 * 1. and 2. argument are null (they are used by JUSS parsers) and argument 3. will be {@link ProtocolRegistry} used by this {@link Serializer}, 
	 * and additional argument 4. being of type {@link Class} containing information about class that is currently being serialized (used primarily by {@link ObjectConverter} and should start as null).
	 * 
	 * @return This scope after loading data from reader (you most likely want to return "this")!
	 * 
	 * @throws IOException When reading the reader fails...
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Scope> S loadFrom(Reader reader, Object... args) throws IOException
	{	
//		boolean formatRequired = true;
		
		if (args.length < 4)
			args = Arrays.copyOf(args, 4);
//		if (args[0] instanceof Boolean)
//			formatRequired = (boolean) args[0];
		args[0] = this;
		if (args[3] == null)
			args[3] = getProtocols();
		
//		StringBuilder str = readAndFormat(reader, formatRequired);
//		List<Object> objs = splitAndParse(str, args);
//		addAll(objs);
		
		readAndParse(values(), reader, 512, args);
		
		if (parent == null)
			getImports().removeImportsOf(this);
		return (S) this;
	}
	
	/**
	 * This acts basically as a lexer for Juss and Json...
	 * 
	 * @param result | Resulting collection of objects to fill up with parsed objects from reader.
	 * @param reader | The reader to {@link Reader#read(char[], int, int)}.
	 * @param charBuffLen | Length of internal char buffer (number of chars that are processed at once). Recommended length is 512, in case of increase should be the power of 2.
	 * @param parserArgs | Additional arguments to use. Take a look at {@link Serializer#loadFrom(File, Object...)}.
	 * 
	 * @throws IOException If {@link Reader} instance throws it during reading...
	 * 
	 * @since 1.3.9
	 */
	public void readAndParse(Collection<Object> result, Reader reader, final int charBuffLen, Object... parserArgs) throws IOException
	{
		ParserRegistry reg = getParsers();
		
		StringBuilder sb = new StringBuilder();
		int len;
		
		char chars[] = new char[charBuffLen], oldCh = 0;
		readerLoop: for (int charsRead, /*state = 0,*/ brackets; (charsRead = reader.read(chars, 0, charBuffLen)) != -1; )
		{
			charsLoop: for (int i = 0; i < charsRead; i++)
			{
				char ch = chars[i];
			
				if (oldCh == '/') // Skip the comments...
				{
					if (ch == '/' || ch == '*') // // | /*
					{
						sb.setLength(len = (sb.length() - 1)); // Pop / and restore old char, important!
						oldCh = len == 0 ? 0 : sb.charAt(len - 1);
						for (;;) // Skip all till end of comm
						{
							if (++i >= charsRead) // Check and read on if necessary...
								if ((charsRead = reader.read(chars, 0, charBuffLen)) != -1)
									i = 0;
								else break readerLoop;

							if (ch == '/' ? 
								isOneOf(chars[i], ENDL) :
								chars[i] == '*' && (++i < charsRead ? chars[i] : reader.read()) == '/' // */
							) {
								continue charsLoop;
							}
						}
					}
				}

				if (ch == '\"' || ch == '\'')
				{
					sb.append(ch);
					for (oldCh = ch; ; ) // Append all chars until end of str/char
					{
						if (++i >= charsRead) // Check and read on if necessary...
							if ((charsRead = reader.read(chars, 0, charBuffLen)) != -1)
								i = 0;
							else break readerLoop;
						
						sb.append(ch = chars[i]);
						if (ch == oldCh)
							continue charsLoop; // break;
					}
				}
				else if ((ch | ' ') == '{')
				{
					sb.append(ch);
					brackets = 1;
					do
					{
						if (++i >= charsRead) // Check and read on if necessary...
							if ((charsRead = reader.read(chars, 0, charBuffLen)) != -1)
								i = 0;
							else break readerLoop;
						
						sb.append(ch = chars[i]);
						if ((ch |= ' ') == '{')
							brackets++;
						else if (ch == '}')
							brackets--;
						else if (ch == '\"' || ch == '\'')
						{
							for (oldCh = ch; ; ) // Append all chars until end of str/char
							{
								if (++i >= charsRead) // Check and read on if necessary...
									if ((charsRead = reader.read(chars, 0, charBuffLen)) != -1)
										i = 0;
									else break readerLoop;
								
								sb.append(ch = chars[i]);
								if (ch == oldCh)
									break;
							}
						}
					}
					while (brackets != 0);
				}
				else if (ch == ';' || ch == ',' || (oldCh == '}' && isOneOf(ch, ENDL)))
				{
					if ((len = sb.length()) != 0)
					{
						if (oldCh == ' ') // Trim
							sb.setLength(--len);
						
						if (len != 0)
						{
//							System.err.println("|" + sb.toString() + "| " + (oldCh == ' ') + "\n");
							Object obj = parseObject(reg, sb.toString(), parserArgs);
							if (obj != VOID)
								result.add(obj);
							sb.setLength(0);
						}
					}
					
					oldCh = 0;
					continue;
				}
				else if (ch <= ' ') // Handle blanks, skip unnecessary
				{
					if (oldCh > ' ') // First blank (in possible row)
						sb.append(oldCh /*= state*/ = ' ');
					continue;
				}
				else
					sb.append(ch);
				
				oldCh = ch;
			}
		}

		if ((len = sb.length()) == 0)
			return; 
			
		if (oldCh == ' ') // Trim
			sb.setLength(--len);

		if (len != 0)
		{
//			System.err.println("|" + sb.toString() + "| " + (oldCh) + "\n");
			Object obj = parseObject(reg, sb.toString(), parserArgs);
			if (obj != VOID)
				result.add(obj);
		}
	}
	
	/**
	 * @deprecated (1.3.9) DO NOT USE, IT IS SLOW AND POORLY WRITTEN, USE {@link JussSerializer#readAndParse(Collection, Reader, Object...)} INSTEAD!
	 * 
	 * @return Formated content of reader ready to parse! Should not modify this object in any way!
	 * 
	 * @since 1.3.2
	 */
	@Deprecated
	public StringBuilder readAndFormat(Reader reader, boolean format)
	{
		int quote = 0, multLineCom = -1;
		//int brackets = 0, lastIndex = 0, delChars = 0;
		StringBuilder sb = new StringBuilder();

		try
		{
			BufferedReader lineReader = new BufferedReader(reader);
			//String blanks = new String(new char[] {32, 9, 10, 12, 13});
			for (String line = lineReader.readLine(); line != null; line = lineReader.readLine())
			{
				if (format)
				{
					int lastNotBlank = -1;
					for (int i = 0, com = -1, len = line.length(); i < len; i++)
					{
						char ch = line.charAt(i);
						
						boolean notString = quote % 2 == 0;
						if (multLineCom > -1 || com > -1) //Is comment
						{
							if (multLineCom > 0 && ch == '*' && i < len-1 && line.charAt(++i) == '/')
								com = multLineCom = -1;
						}
						else if (notString && ch == '/' && i < len-1)
						{
							if (line.charAt(i+1) == '*')
								i += multLineCom = 1;
							else if (line.charAt(i+1) == '/')
								com++;
							else
								sb.append('/');
						}
						else if (notString && ch >= 9 && ch <= 13)
							sb.append(' ');
						else
						{
							/*if (notString)
							{
								if (ch | ' ') == '{' || ch == '[')
									brackets++;
								else if (ch == '}' || ch == ']')
								{
									if (brackets > 0)
										brackets--;
									else
										throw new IllegalArgumentException("Missing opening bracket in: " + line);
								}
								else if (brackets == 0 && isOneOf(ch, ';', ','))
								{
									System.out.println(sb);
									sb = new StringBuilder();
									continue;
								}
							}*/
							if (ch > 32)
							{
								lastNotBlank = ch;
								if (ch == '"')
									quote++;
							}
							sb.append(ch);
						}
					}
					sb.append((lastNotBlank | ' ') == '}' ? ';' : ' ');
				}
				else
					sb.append(line).append('\n');
			}
			lineReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return sb;
	}

	/**
	 * @deprecated (1.3.9) DO NOT USE, IT IS SLOW AND POORLY WRITTEN, USE {@link JussSerializer#readAndParse(Collection, Reader, Object...)} INSTEAD!
	 * 
	 * @return List of objects parsed from given formatted string!
	 * 
	 * @since 1.3.2
	 */
	@Deprecated
	public List<Object> splitAndParse(StringBuilder formattedStr, Object... parserArgs)
	{
		List<Object> result = new ArrayList<>();

		ParserRegistry reg = getParsers();

		//DataParser[] parsers = new DataParser[DataParser.REGISTRY.size()];
		int lastIndex = 0, len = formattedStr.length();
		//boolean isBracketSplit = false;
		for (int i = 0; i < len; i++)
		{
			int ch = formattedStr.charAt(i);
			if (ch == '"')
			{
				do if (++i >= len)
					throw new IllegalArgumentException("Unclosed or missing quotes in: " + formattedStr);
				while (formattedStr.charAt(i) != '"');
			}
			else if ((ch | ' ') == '{')
			{
				for (int brackets = 1; brackets != 0; )
				{
					if (++i >= len)
						throw new IllegalArgumentException("Missing ("+ brackets + ") closing bracket in: " + formattedStr);
					if ((ch = (formattedStr.charAt(i) | ' ')) == '{')
						brackets++;
					else if (ch == '}')
						brackets--;
					else if (ch == '"')
						while (++i < len && formattedStr.charAt(i) != '"');
				}
			}
			else if ((ch == ';' || ch == ',')/* || (brackets == 1 && (isBracketSplit = ch == '}' || ch == ']'))*/)
			{
				String str = formattedStr.substring(lastIndex == 0 ? 0 : lastIndex + 1, lastIndex = i /*+ (isBracketSplit ? 1 : 0)*/).trim();
				if (!str.isEmpty())
				{
//					System.out.println("|" + str + "|\n");
					Object obj = parseObject(reg, str, parserArgs);
					if (obj != VOID)
						result.add(obj);
				}
				//add = 1;
			}
		}

		String str = formattedStr.substring(lastIndex == 0 ? 0 : lastIndex + 1, len).trim();
		if (!str.isEmpty())
		{
//			System.out.println("|" + str + "|\n");
			Object obj = parseObject(reg, str, parserArgs);
			if (obj != VOID)
				result.add(obj);
		}
		return result;
	}
	 
//	/**
//	 * @return Object converted to string!
//	 * <br><br>
//	 * Note: Used by {@link JussSerializer#SerializeTo(Appendable, Object...)}!
//	 * 
//	 * @see DataConverter#objToString(Registry, String, Object...)
//	 * 
//	 * @since 1.3.5
//	 */
//	public CharSequence objectToStr(Registry<DataParser> registry, Object obj, Object... parserArgs)
//	{
//		return 
//	}
	
	/**
	 * @return Object parsed from str!
	 * <br><br>
	 * Note: Used by {@link JussSerializer#splitAndParse(String, int, Object...)}!
	 * 
	 * @see DataParser#parseObj(Registry, String, Object...)
	 * 
	 * @since 1.3.2
	 */
	protected Object parseObject(ParserRegistry registry, String str, Object... parserArgs)
	{
		return registry.parse(str, parserArgs);
	}
	
	/**
	 * @param variableKey | Variable to clone!
	 * 
	 * @return Clone of value stored by variable with inserted name or null if there is no such a one!
	 * <br><br>
	 * Note: Cloning is done by {@link Utils#Clone(Object, Collection, Object[], Object...)}!
	 * 
	 * @since 1.3.2
	 */
	public <T> T cloneOf(String variableKey)
	{
		return cloneOf(variableKey, null);
	}
	
	/**
	 * @param variableKey | Variable to clone!
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Clone of value stored by variable with inserted name or defaultValue if there is no such a one or given key contains null!
	 * <br><br>
	 * Note: Cloning is done by {@link Utils#Clone(Object, Collection, Object[], Object...)}!
	 * 
	 * @since 1.3.2
	 */
	public <T> T cloneOf(String variableKey, T defaultValue)
	{
		T obj = get(variableKey, defaultValue);
		if (obj == defaultValue)
			return defaultValue;
		return Clone(obj, getParsers(), new Object[] {this, -99999, 0, getProtocols(), getFormat()}, this, null, null, getProtocols());
	}

	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Clone of independent value with valueIndex!
	 * <br><br>
	 * Note: Cloning is done by {@link Utils#Clone(Object, Collection, Object[], Object...)}!
	 * 
	 * @since 1.3.2
	 */
	public <T> T cloneOf(int valueIndex)
	{
		T obj = get(valueIndex);
		return Clone(obj, getParsers(), new Object[] {this, -99999, 0, getProtocols(), getFormat()}, this, null, null, getProtocols());
	}
	
	/**
	 * @return True if comment supposed to be generated!
	 * 
	 * @since 1.3.2
	 */
	public boolean isGenerateComments() 
	{
		return (format & 0b10) != 0;
	}

	/**
	 * @param generateComments | If true, comments will be generated during serialization!
	 * 
	 * @since 1.3.2
	 */
	public void setGenerateComments(boolean generateComments)
	{
		if (generateComments)
			format |= 0b10;
		else
			format &= 0xff ^ 0b10;
	}
	
//	/**
//	 * @return True if used parsers and converters are cached during individual serializations and deserializations! This can improve performance a lot but in some cases might cause some strange artifacts!
//	 * 
//	 * @since 1.3.5
//	 * 
//	 * @see ParserRegistry#resetCache()
//	 */
//	public boolean isSerializationAutoCaching() 
//	{
//		return serializationAutoCaching;
//	}
//
//	/**
//	 * @param serializationAutoCaching | Set this on true to allow caching of used parsers and converters during individual serializations and deserializations! This can improve performance a lot but in some cases might cause some strange artifacts!
//	 * 
//	 * @since 1.3.5
//	 * 
//	 * @see ParserRegistry#resetCache()
//	 * @see ParserRegistry#destroyCache()
//	 */
//	public void setSerializationAutoCaching(boolean serializationAutoCaching) 
//	{
//		if (!(this.serializationAutoCaching = serializationAutoCaching))
//			getParsers().destroyCache();
//	}
	
	/**
	 * @param fromObj | Object to create serializer from!
	 * 
	 * @return {@link JussSerializer} created from given fromObj by mapping obj's fields into variables of created serializer via given fields (fieldNamesToUse) and conversion rules listed below!!<br><br>
	 * Table of specific Object --> JussSerializer conversions:
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
	 * @throws If something went wrong during deserialization!
	 * 
	 * @since 1.3.5
	 */
	public static JussSerializer from(Object fromObj) throws Exception
	{
		try 
		{
			return from(fromObj, new String[0]);
		}
		catch (IntrospectionException e)
		{
			return new JussSerializer(fromObj);
		}
	}
	
	/**
	 * @param fromObj | Object to create serializer from!
	 * @param fieldNamesToUse | Array of obj field names to map into scopes variables using getters (read method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link Scope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
	 * 
	 * @return {@link JussSerializer} created from given fromObj by mapping obj's fields into variables of created serializer via given fields (fieldNamesToUse) and conversion rules listed below!!<br><br>
	 * Table of specific Object --> JussSerializer conversions:
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
	 * @throws Exception if calling of some {@link PropertyDescriptor}s write method fails (should not happen often) or when something went wrong during deserialization!
	 * @throws IntrospectionException when there were no PropertyDescriptor found for obj class!
	 * 
	 * @since 1.3.5
	 */
	public static JussSerializer from(Object fromObj, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		return (JussSerializer) Serializer.from(new JussSerializer(), fromObj, fieldNamesToUse);
	}
}
