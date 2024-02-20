package org.ugp.serialx.juss.converters;

import static org.ugp.serialx.Utils.Clone;
import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.fastReplace;
import static org.ugp.serialx.Utils.multilpy;
import static org.ugp.serialx.Utils.splitValues;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Utils;
import org.ugp.serialx.Utils.NULL;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;

/**
 * This converter is capable of converting {@link Map.Entry} and reading variables from {@link Scope} via "$"!
 * {@link VariableConverter#parse(String, Object...)} required one additional Scope argument in args... argument!
 * Its case insensitive!<br>
 * Exact outputs of this converter are based on inserted scope!
 * 
 * @author PETO
 *	
 * @since 1.3.0
 */
public class VariableConverter implements DataConverter
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
			GenericScope<Object, Object> scope = (GenericScope<Object, Object>) args[0];
			boolean genericVar = args.length > 4 && args[4] == GenericScope.class;
			if (isVarAssignment(arg))
			{
				String[] enrty = splitValues(arg, 0, false, new char[] {'?'}, '=', ':');

				Object obj = null;
				String objString = enrty[enrty.length-1];
				
				if (enrty.length > 1 && !objString.isEmpty())
				{
					obj = myHomeRegistry.parse(objString, args);
				}

				for (int i = 0; i < enrty.length-1; i++)
				{
					if (!genericVar && contains(enrty[i] = enrty[i].trim(), ' '))
						LogProvider.instance.logErr("Variable name \"" + enrty[i] + "\" is invalid, blank characters are not allowed!", null);
					else
					{
						if ((enrty[i] = fastReplace(enrty[i], "$", "")).indexOf('.') > -1)
						{
							String[] tree = splitValues(enrty[i], '.');
							GenericScope<Object, Object> sc = (GenericScope<Object, Object>) scope.getGenericScope((Object[]) Arrays.copyOfRange(tree, 0, tree.length-1));
							if (sc != null)
							{
								if (obj == VOID)
									sc.variables().remove(enrty[i]);
								else
									sc.put(genericVar ? myHomeRegistry.parse(tree[tree.length-1], true, null, args) : tree[tree.length-1], obj);
							}
							else
								LogProvider.instance.logErr("Variable \"" + tree[tree.length-2] +"\" was not declared as scope in its scope so variable \"" + tree[tree.length-1] +"\" cant be set to \"" + obj + "\"!", null);
						}
						else if (obj == VOID)
							scope.variables().remove(enrty[i]);
						else
							scope.put(genericVar ? myHomeRegistry.parse(enrty[i], true, null, args) : enrty[i], obj);
					}
				}
				if (arg.charAt(0) == '$')
					return obj;
				return VOID;
			}
			else if (arg.charAt(0) == '$' && !contains(arg, ' ', '+', '-', '*', '/', '%', '>', '<', '=', '&', '|', '^', '?', '='))
			{
//				Object obj; 
//				if ((arg = fastReplace(arg, "$", "")).indexOf('.') > -1)
//				{
//					Object[] path = splitValues(fastReplace(fastReplace(arg, "::new", ""), "::class", ""), '.');
//					GenericScope<Object, Object> sc = (GenericScope<Object, Object>) scope.getGenericScope(Arrays.copyOfRange(path, 0, path.length-1)); //TODO: Prevent neccesity of scope parent inheritance.
//					obj = sc == null ? null : sc.variables().get(path[path.length-1]);
//					/*if (sc == null || !sc.containsVariable(tree[tree.length-1]))
//						LogProvider.instance.logErr("Variable \"" + tree[tree.length-1] + "\" was not declared in \"" + arg.substring(0, arg.length() - tree[tree.length-1].length() - 1) + "\"! Defaulting to null!");*/
//				}
//				else
//				{
//					String str = fastReplace(fastReplace(arg, "::new", ""), "::class", "");
//					/*if (!scope.containsVariable(str))
//						LogProvider.instance.logErr("Variable \"" + str + "\" was not declared! Defaulting to null!");*/
//					obj = scope.variables().get(str);
//				}
				
				Object obj; 
				if ((arg = fastReplace(arg, "$", "")).indexOf('.') > -1)
				{
					Object[] path = splitValues(fastReplace(fastReplace(arg, "::new", ""), "::class", ""), '.');
					if ((obj = scope.get(path)) == null && !scope.variables().containsKey(path[0]))
					{
						for (GenericScope<Object, Object> parent = scope.getParent(); parent != null; parent = parent.getParent())
							if (parent.variables().containsKey(path[0]))
							{
								obj = parent.get(path);
								break;
							}
					}
						
//					GenericScope<Object, Object> sc = (GenericScope<Object, Object>) scope.getGenericScope(Arrays.copyOfRange(path, 0, path.length-1)); //TODO: Prevent neccesity of scope parent inheritance.
					/*if (sc == null || !sc.containsVariable(tree[tree.length-1]))
						LogProvider.instance.logErr("Variable \"" + tree[tree.length-1] + "\" was not declared in \"" + arg.substring(0, arg.length() - tree[tree.length-1].length() - 1) + "\"! Defaulting to null!");*/
				}
				else
				{
					String str = fastReplace(fastReplace(arg, "::new", ""), "::class", "");
					/*if (!scope.containsVariable(str))
						LogProvider.instance.logErr("Variable \"" + str + "\" was not declared! Defaulting to null!");*/
					if ((obj = scope.variables().getOrDefault(str, VOID)) == VOID)
					{
						for (GenericScope<Object, Object> parent = scope.getParent(); parent != null; parent = parent.getParent())
							if ((obj = parent.variables().getOrDefault(str, VOID)) != VOID)
								break;
					}
				}

				if (obj == null || obj == VOID) // Was not found...
					return null;
				return arg.endsWith("::class") ? obj.getClass() : arg.endsWith("::new") ? Clone(obj) : obj;
			}
		}
		return CONTINUE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (obj instanceof Entry)
		{
			Entry<Object, Object> var = (Entry<Object, Object>) obj;	
			int tabs = 0;
			if (args.length > 1 && args[1] instanceof Integer)
				tabs = (int) args[1];

			boolean jsonStyle = isJsonStyle(), genericVar = false;                
			Object key = (genericVar = !((key = var.getKey()) instanceof String)) ? myHomeRegistry.toString(key, args) : key, val = var.getValue();
			return new StringBuilder().append(jsonStyle && !genericVar ? "\""+key+"\"" : key).append(val instanceof Scope && !((Scope) val).isEmpty() ? (jsonStyle ? " : " : " =\n" + multilpy('\t', tabs)) : (jsonStyle ? " : " : " = ")).append(myHomeRegistry.toString(val, args));
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
	 * @return true if inserted expression is variable assignment expression such as <code>variable = 4</code> otherwise false!
	 * 
	 * @since 1.3.0
	 */
	public static boolean isVarAssignment(CharSequence s)
	{
		for (int i = 0, brackets = 0, quote = 0, len = s.length(), oldCh = -1, chNext; i < len; i++)
		{
			char ch = s.charAt(i);
			if (ch == '"')
				quote++;
	
			if (quote % 2 == 0)
			{
				if (ch == '?')
					return false;
				else if (brackets == 0 && (ch == '=' || ch == ':') && !(oldCh == '=' || oldCh == ':' || oldCh == '!' || oldCh == '>'|| oldCh == '<') && (i >= len-1 || !((chNext = s.charAt(i+1)) == '=' || chNext == ':' || chNext ==  '!' || chNext == '>'|| chNext == '<')))
					return true;	
				else if ((ch | ' ') == '{')
					brackets++;
				else if ((ch | ' ') == '}')
					if (brackets > 0)
						brackets--;
			}
			oldCh = ch;
		}
		return false;
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
