package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.multilpy;
import static org.ugp.serialx.Utils.splitValues;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.VariableParser;

/**
 * This converter is capable of converting {@link Map.Entry} and reading variables from {@link GenericScope} by using <code>$</code>!<br>
 * {@link VariableConverter#parse(String, Object...)} requires one additional Scope argument in args... at index 0!<br>
 * It manages assign operator <code>=</code> as well as access member operator also known as separator <code>"."</code>.<br>
 * Its case sensitive!<br>
 * Exact outputs of this converter are based on inserted scope!
 * 
 * @author PETO
 *	
 * @since 1.3.0
 * 
 * @see VariableParser
 */
public class VariableConverter extends VariableParser implements DataConverter
{
	protected boolean jsonStyle;
	
	public VariableConverter() 
	{
		this(false);
	}
	
	/**
	 * @param jsonStyle | If true, this converter will be using Json style of variables ("key" : value)!
	 * 
	 * @since 1.3.2
	 */
	public VariableConverter(boolean jsonStyle) 
	{
		setJsonStyle(jsonStyle);
	}
	
	/**
	 * Raw example of empty variable entry this converter can convert!
	 * 
	 * @since 1.3.0 
	 */
	public static final Entry<String, ?> RAW_VAR_ENTRY = NewVariable("", "null");
	
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String arg, Object... args)
	{
		if (args.length > 0 && arg.length() > 0 && args[0] instanceof GenericScope)
		{
			GenericScope<?, Object> scope = (GenericScope<?, Object>) args[0];
			int op0Index;
			if ((op0Index = isVarAssignment(arg)) > -1)
			{
				boolean getValueModif = arg.charAt(0) == '$', genericVar = args.length > 4 && args[4] == GenericScope.class;
				if (getValueModif)
				{
					arg = arg.substring(1);
					op0Index--;
				}
				
				String vars[] = splitValues(arg, op0Index, 0, 1, new char[] {'?'}, '=', ':'), valStr;

				Object val = null;
				int iVal = vars.length-1;
				if (vars.length > 1 && !(valStr = vars[iVal]).isEmpty())
				{
					val = myHomeRegistry.parse(valStr, args);
				}

				eachVar: for (int i = 0; i < iVal; i++) // Support for assigning multiple vars to the same value... Yea this is not the prettiest code but it does the job and mainly it does it fast so shut up!
				{
					String var = vars[i];
					if (!genericVar)
					{
						if (contains(var, ' '))
						{
							LogProvider.instance.logErr("Variable name \"" + var + "\" is invalid, blank characters are not allowed!", null);
							continue;
						}

						if ((op0Index = var.indexOf('.')) > -1)
						{
							String[] path = splitValues(var, op0Index, 0, 0, new char[0],  '.');
							int iLast = path.length-1, j = 0;
							
							backlook: do
							{
								Object sc;
								if ((sc = getMemberOperator(scope, path[0])) != VOID) // Attempt to get only when exists...
								{
									for (j = 1; j < iLast; j++) // Subscope/forward lookup (inner path only)...
										if ((sc = getMemberOperator(sc, path[j])) == null || sc == VOID)
											break backlook;
									
									setMemberOperator(myHomeRegistry, sc, path[iLast], val, false, args);
									continue eachVar;
								}
							}
							while ((scope = scope.getParent()) != null);
							
							LogProvider.instance.logErr("Path \"" + var + "\" cannot be set to \"" + val + "\" because \"" + path[j] + "\" is not a accessible or does not exist!", null);
							continue;
						}
					}
					
					setMemberOperator(myHomeRegistry, scope, var, val, genericVar, args);
				}

				return getValueModif ? val : VOID;
			}
			
			return parse(myHomeRegistry, arg, scope, args); //Reading vars from scope...
		}

		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Entry)
		{
			Entry<?, ?> var = (Entry<?, ?>) obj;
			int tabs = 0;
			if (args.length > 1 && args[1] instanceof Integer)
				tabs = (int) args[1];

			boolean jsonStyle = isJsonStyle(), genericVar = false;                
			Object key = (genericVar = !((key = var.getKey()) instanceof String)) ? myHomeRegistry.toString(key, args) : key, val = var.getValue();
			return new StringBuilder().append(jsonStyle && !genericVar ? "\""+key+"\"" : key)
					.append(val instanceof GenericScope && !((GenericScope<?, ?>) val).isEmpty() ? (jsonStyle ? " : " : " =\n" + multilpy('\t', tabs)) : (jsonStyle ? " : " : " = "))
					.append(myHomeRegistry.toString(val, args));
		}
		return CONTINUE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CharSequence getDescription(ParserRegistry myHomeRegistry, Object objToDescribe, Object... argsUsedConvert) 
	{
		Entry<String, ?> ent = (Entry<String, ?>) objToDescribe;
		return new StringBuilder(myHomeRegistry.getConverterFor(ent.getValue(), argsUsedConvert).getDescription(myHomeRegistry, ent.getValue(), argsUsedConvert)).append(" Stored by \"").append(ent.getKey()).append("\" variable!");
	}
	
	/**
	 * @param myHomeRegistry | {@link ParserRegistry} provided by caller, may or may not be used... 
	 * @param source | Source object to set the value member. 
	 * @param member | Name/key of the member to set.
	 * @param val | Value to set the member to.
	 * @param genericVar | If true, member is expected be generic (not only string) and further parsing is required, may or may not be used... 
	 * @param args | Some additional args to be used in case of parsing that are provided by called, may or may not be used... 
	 *
	 * @return By default it returns the previous value of the member. If member with provided name/key is not present in the source or its value is not possible to set, {@link VOID} should be returned! 
	 * 
	 * @since 1.3.7
	 */
	@SuppressWarnings("unchecked")
	public Object setMemberOperator(ParserRegistry myHomeRegistry, Object source, String member, Object val, boolean genericVar, Object... args)
	{
		if (source instanceof GenericScope)
		{
			if (val == VOID)
				return ((GenericScope<Object,?>) source).removeVariable(member);
			return ((GenericScope<Object, Object>) source).put(genericVar ? myHomeRegistry.parse(member, true, null, args) : member, val);
		}
		return VOID;
	}
	
	/**
	 * @return True if variables will be serialized using json style ("key" : value)!
	 * 
	 * @since 1.3.2
	 */
	public boolean isJsonStyle() 
	{
		return jsonStyle;
	}

	/**
	 * @param jsonStyle | If true, this converter will be using Json style of variables ("key" : value)!
	 * 
	 * @since 1.3.2
	 */
	public void setJsonStyle(boolean jsonStyle) 
	{
		this.jsonStyle = jsonStyle;
	}

	/**
	 * @param s | CharSequence to search!
	 * 
	 * @return Index of first assignment operator ('=' or ':') if inserted expression is variable assignment expression such as <code>variable = 4</code>, otherwise -1!
	 * 
	 * @since 1.3.0
	 */
	public static int isVarAssignment(CharSequence s)
	{
		for (int i = 0, brackets = 0, quote = 0, len = s.length(), oldCh = -1, chNext; i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
	
			if (quote % 2 == 0)
			{
				if (ch == '?')
					return -1;
				else if (brackets == 0 && (ch == '=' || ch == ':') && !(oldCh == '=' || oldCh == ':' || oldCh == '!' || oldCh == '>'|| oldCh == '<') && (i >= len-1 || !((chNext = s.charAt(i+1)) == '=' || chNext == ':' || chNext ==  '!' || chNext == '>'|| chNext == '<')))
					return i;	
				else if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
					if (brackets > 0)
						brackets--;
			}
			oldCh = ch;
		}
		return -1;
	}
	
//	public static <K, V> V getValueOf(GenericScope<K, V> scope, K... pathToScope) 
//	{
//		
//		if (scope.containsVariable(pathToScope[0]))
//			return scope.getGenericScope(pathToScope);
//	}
	
	/**
	 * @param varName | Name of variable.
	 * @param varValue | Value of variable.
	 * 
	 * @return New entry with varName as name and varValue as value!
	 * 
	 * @param <T> | Generic type of variables value.
	 * 
	 * @since 1.3.5 
	 */
	public static <T> Entry<String, T> NewVariable(String varName, T varValue)
	{
		return new AbstractMap.SimpleImmutableEntry<>(varName, varValue);
	}
}
