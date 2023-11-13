package org.ugp.serialx.juss;

import static org.ugp.serialx.Utils.Clone;
import static org.ugp.serialx.Utils.InvokeStaticFunc;
import static org.ugp.serialx.Utils.indexOfNotInObj;
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
import org.ugp.serialx.Utils.NULL;
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
	public static final ParserRegistry JUSS_PARSERS = new ParserRegistry(new OperationGroups(), new VariableConverter(), new StringConverter(), new ObjectConverter(), new ArrayConverter(), new NumberConverter(), new BooleanConverter(), new CharacterConverter(), new NullConverter(), new SerializableBase64Converter());
	
	/**
	 * {@link ParserRegistry} with all parsers required to parse JUSS with additional operators.
	 * <br>
	 * Since 1.3.7 this requires "org.ugp.serialx.converters.Operators" from SerialX "operators" modules to be present on the classpath!
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
	 * Set this on true and program will generate comments and report!<br>
	 * Note: Keep this on false to achieve the best performance!
	 * 
	 * @since 1.0.5
	 */
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
	public Scope clone() 
	{
		Scope scope = super.clone();
		if (scope instanceof JussSerializer)
		{
			((JussSerializer) scope).setGenerateComments(isGenerateComments());
			((JussSerializer) scope).setParsers(getParsers());
			((JussSerializer) scope).setProtocols(getProtocols());
		}
		return scope;
	}
	
	@Override
	public JussSerializer emptyClone(Scope parent) 
	{
		JussSerializer srl = emptyClone(new JussSerializer(), parent);
		srl.setGenerateComments(isGenerateComments());
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
			return Clone(this, getParsers(), new Object[] {-9999, 0, this, getProtocols(), isGenerateComments()}, this, null, null, getProtocols());
		return super.clone();
	}
	
	/**
	 * @param name | Name of variable.
	 * @param value | Value of variable.
	 * 
	 * @return Variable in JUSS form to serialize as [name] = [value].
	 * 
	 * @since 1.1.5
	 */
	@Override
	public <T> String Var(String name, T value)
	{
		return Var(name, value, false);
	}
	
	/**
	 * @param name | Name of variable.
	 * @param value | Value of variable.
	 * @param isValue | True if variables value supposed to by visible also during value loading.
	 * 
	 * @return Variable in JUSS form to serialize as [name] = [value].
	 * 
	 * @since 1.1.5
	 */
	public <T> String Var(String name, T value, boolean isValue)
	{
		return Code((isValue ? "$" : "") + name + " = " + getParsers().toString(value, 0, 0, this, getProtocols(), isGenerateComments()) + (generateComments ? "; //Object of " + value.getClass().getName() + ": \"" + value + "\" inserted manually! Stored by \"" + name + "\" variable!" : ""));
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
			sb.append(getParsers().toString(args[i], 0, 0, this, getProtocols(), isGenerateComments())).append(i < len-1 ? " " : "");
		return Code(cls.getName() + "::" + staticMethodName + (args.length <= 0 ? "" : " ") + sb);
	}
	
	/**
	 * @param source | Source {@link Appendable} to serialize variables and objects into!
	 * @param args | Additional arguments to use, in case of {@link JussSerializer} this should be array of length 6 with 0. argument being this pointer to this serializer, argument 1. and 2. being integers signifying number of tabs and index of parsing iteration (used primarily by {@link ObjectConverter}), argument 3. containing {@link ProtocolRegistry} of this {@link Serializer}, argument 4. being of type {@link Class} containing information about class that is curently being serialized (used primarily by {@link ObjectConverter}), and argument 5. being boolean signifying whether or not code comments are supposed to be generated! 
	 * 
	 * @return Source {@link Appendable} with variables and objects serialized in specific format.
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <A extends Appendable> A SerializeTo(A source, Object... args) throws IOException
	{	
		Map<String, ?> variables = variables();
		List<Object> objs = values();
		int valuesLen = objs.size(), varLen = variables.size(), i = 0, tabs = 0;
		
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
			args[5] = isGenerateComments();

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
			for (Entry<String, ?> var : variables.entrySet()) //Vars
			{
				appandVar(source, reg.toString(var, args), var, tabs, i >= varLen-1 && valuesLen <= 0);
				if (generateComments && (!(var.getValue() instanceof Scope) || ((Scope) var.getValue()).isEmpty()))
					GenerateComment(source, reg, var);
			
				if (i++ < varLen-1 || valuesLen > 0)
					source.append('\n');
			}
		}
		
		for (i = 0; i < valuesLen; i++) //Values
		{
			Object obj = objs.get(i);
			CharSequence serialized = reg.toString(obj, args);
			
			appandVal(source, serialized, obj, tabs, i >= valuesLen-1);
			if (generateComments && (!(obj instanceof Scope) || ((Scope) obj).isEmpty()))
				GenerateComment(source, reg, obj);
		
			if (i < valuesLen-1)
				source.append('\n');
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
		source.append(multilpy('\t', tabs)).append(serializedVar);
		if (isLast && var.getValue() instanceof Scope)
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
		source.append(multilpy('\t', tabs)).append(serializedVal);
		if (isLast && value instanceof Scope || serializedVal != null && indexOfNotInObj(serializedVal, '/') != -1)
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
	 * @param formatingArgs | Additional arguments to use. In case of JussSerializer, this should be array of length 4 with 0. argument will being this pointer of this scope (it can be also boolean signifying if formating is required), 1. and 2. argument are null (they are used by JUSS parsers) and argument 3. will be {@link ProtocolRegistry} used by this {@link Serializer}, and additional argument 4. being of type {@link Class} containing information about class that is curently being serialized (used primarily by {@link ObjectConverter}).
	 * 
	 * @return This scope after loading data from reader (you most likely want to return "this")!
	 * 
	 * @since 1.3.2
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Scope> S LoadFrom(Reader reader, Object... args)
	{	
		boolean formatRequired = true;
		
		if (args.length < 4)
			args = Arrays.copyOf(args, 4);
		if (args[0] instanceof Boolean)
			formatRequired = (boolean) args[0];
		args[0] = this;
		if (args[3] == null)
			args[3] = getProtocols();
		
		String str = readAndFormat(reader, formatRequired);
		List<Object> objs = splitAndParse(str, 0, args);
		addAll(objs);
		
		//double t0 = System.nanoTime();
		/*Registry<DataParser> reg = DataParser.REGISTRY;
		String lastLine = null;
		int quote = 0, multLineCom = -1, brackets = 0;
		try
		{
			BufferedReader lineReader = new BufferedReader(reader);
			//String blanks = new String(new char[] {32, 9, 10, 12, 13});
			for (String line = lineReader.readLine(); line != null; line = lineReader.readLine())
			{
				for (int i = 0, len = line.length(), com = -1, lastIndex = 0; i < len; i++)
				{
					char ch = line.charAt(i);
					if (ch == '/' && i < len-1 && line.charAt(i+1) == '/')
						com++;
					else if (multLineCom <= -1 && ch == '"')
						quote++;
					
					boolean notString = quote % 2 == 0;
					if (multLineCom > -1 || com > -1) //Is comment
					{
						if (multLineCom > 0 && ch == '*' && i < len-1 && line.charAt(++i) == '/')
							com = multLineCom = -1;
					}
					else if (notString && ch == '/' && i < len-1 && line.charAt(i+1) == '*')
						i += multLineCom = 1;
					/*else if (notString && blanks.indexOf(ch) > -1)
					{
						if ((chBefore = i > 0 ? line.charAt(i-1) : 0) != ';' && chBefore != '{' && blanks.indexOf(chBefore) <= -1)
							sb.append(" ");
					}*/
					/*else if (notString && i < str.length()-1 && (ch == '!' && str.charAt(i+1) == '!' || ch == '-' && str.charAt(i+1) == '-' || ch == '+' && str.charAt(i+1) == '+'))
						i++;*
					else
					{
						if (notString)
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
							else if (brackets == 0 && (ch == ';' || ch == ','))
							{
								//System.out.println(lastIndex + " " + i);
								String str = line.substring(lastIndex == 0 ? 0 : lastIndex + 1, lastIndex = i);
								//System.out.println(str);
								if (!(str = str.trim()).isEmpty())
								{
									Object obj = ParseObjectHandleNull(reg, str, true, result);
									if (obj != VOID)
										result.add(obj);
								}
							}
						}
					}
				}
				lastLine = line;
			}
			lineReader.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		//double t = System.nanoTime();
		//System.out.println((t-t0)/1000000);

		if (lastLine != null && indexOfNotInObj(lastLine, ';', ',') <= -1)
		{
			if (!(lastLine = lastLine.trim()).isEmpty())
			{
				Object obj = ParseObjectHandleNull(reg, lastLine, true, result);
				if (obj != VOID)
					result.add(obj);
			}
		}*/

		if (parent == null)
			getImports().removeImportsOf(this);
//		else
//			for (Map.Entry<?, ?> ent : parent.varEntrySet())
//				if (variables().get(ent.getKey()) == ent.getValue())
//					variables().remove(ent.getKey());//TODO: Prevent neccesity of scope parent inheritance.
		return (S) this;
	}
	
	/**
	 * @return Formated content of reader ready to parse!
	 * 
	 * @since 1.3.2
	 */
	protected String readAndFormat(Reader reader, boolean format)
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
					sb.append(lastNotBlank == '}' || lastNotBlank == ']' ? ';' : ' ');
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

		return sb.toString();
	}

	/**
	 * @return List of objects parsed from given formatted string!
	 * 
	 * @since 1.3.2
	 */
	protected List<Object> splitAndParse(String formattedStr, int offset, Object... parserArgs)
	{
		List<Object> result = new ArrayList<>();

		ParserRegistry reg = getParsers();

		//DataParser[] parsers = new DataParser[DataParser.REGISTRY.size()];
		int brackets = 0, quote = 0, lastIndex = 0;
		//boolean isBracketSplit = false;
		for (int i = 0, len = formattedStr.length(); i < len; i++)
		{
			char ch = formattedStr.charAt(i);
			if (ch == '"')
				quote++;
			
			if (quote % 2 == 0)
			{
				/*if (isBracketSplit)
					add = 0;
				isBracketSplit = false;*/
				if (brackets == 0 && (ch == ';' || ch == ',')/* || (brackets == 1 && (isBracketSplit = ch == '}' || ch == ']'))*/)
				{
					String str = formattedStr.substring(lastIndex == 0 ? 0 : lastIndex + 1, lastIndex = i /*+ (isBracketSplit ? 1 : 0)*/);
					if (!(str = str.trim()).isEmpty())
					{
						Object obj = parseObject(reg, str, parserArgs);
						if (obj != VOID)
							result.add(obj);
					}
					//add = 1;
				}
				else if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
				{
					if (brackets > 0)
						brackets--;
					else
						throw new IllegalArgumentException("Missing opening bracket in: " + formattedStr);
				}
			}
		}
		
		if (quote % 2 != 0)
			throw new IllegalArgumentException("Unclosed or missing quotes in: " + formattedStr);
		else if (brackets > 0)
			throw new IllegalArgumentException("Unclosed brackets in: " + formattedStr); 
		else 
		{
			String str = formattedStr.substring(lastIndex == 0 ? 0 : lastIndex + 1, formattedStr.length());
			if (!(str = str.trim()).isEmpty())
			{
				Object obj = parseObject(reg, str, parserArgs);
				if (obj != VOID)
					result.add(obj);
			}
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
		return NULL.toOopNull(registry.parse(str, parserArgs));
	}
	
	/**
	 * @param variable | Variable to clone!
	 * 
	 * @return Clone of value stored by variable with inserted name or null if there is no such a one!
	 * <br><br>
	 * Note: Cloning is done by {@link Serializer#Clone(Object, Registry, Object[], Object...))}!
	 * 
	 * @since 1.3.2
	 */
	public <T> T cloneOf(String variableName)
	{
		return cloneOf(variableName, null);
	}
	
	/**
	 * @param variable | Variable to clone!
	 * @param defaultValue | Default value to return.
	 * 
	 * @return Clone of value stored by variable with inserted name or defaultValue if there is no such a one!
	 * <br><br>
	 * Note: Cloning is done by {@link Serializer#Clone(Object, Registry, Object[], Object...))}!
	 * 
	 * @since 1.3.2
	 */
	public <T> T cloneOf(String variableName , T defaultValue)
	{
		T obj = get(variableName , defaultValue);
		if (obj == defaultValue)
			return defaultValue;
		return Clone(obj, getParsers(), new Object[] {-99999, 0, this, getProtocols(), isGenerateComments()}, this, null, null, getProtocols());
	}

	/**
	 * @param valueIndex | Index of independent value. Also can be negative, in this case u will get elements from back!
	 * {@link IndexOutOfBoundsException} will be thrown if index is too big!
	 * 
	 * @return Clone of independent value with valueIndex!
	 * <br><br>
	 * Note: Cloning is done by {@link Serializer#Clone(Object, Registry, Object[], Object...))}!
	 * 
	 * @since 1.3.2
	 */
	public <T> T cloneOf(int valueIndex)
	{
		T obj = get(valueIndex);
		return Clone(obj, getParsers(), new Object[] {-99999, 0, this, getProtocols(), isGenerateComments()}, this, null, null, getProtocols());
	}
	
	/**
	 * @return True if comment supposed to be generated!
	 * 
	 * @since 1.3.2
	 */
	public boolean isGenerateComments() 
	{
		return generateComments;
	}

	/**
	 * @param generateComments | If true, comments will be generated during serialization!
	 * 
	 * @since 1.3.2
	 */
	public void setGenerateComments(boolean generateComments)
	{
		this.generateComments = generateComments;
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
	 * @param fieldNamesToUse | Array of obj field names to map into scopes variables using getters (read method)! {@link PropertyDescriptor}s of these fields will be obtained using {@link GenericScope#getPropertyDescriptorsOf(Class, String...)}! This is used only as a last (default) option!
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
	public static JussSerializer from(Object fromObj, String... fieldNamesToUse) throws IntrospectionException, Exception
	{
		return (JussSerializer) Serializer.from(new JussSerializer(), fromObj, fieldNamesToUse);
	}
}
