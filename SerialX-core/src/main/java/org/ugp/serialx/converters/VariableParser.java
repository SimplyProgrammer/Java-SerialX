package org.ugp.serialx.converters;

import static org.ugp.serialx.Utils.Clone;
import static org.ugp.serialx.Utils.contains;
import static org.ugp.serialx.Utils.splitValues;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;

/**
 * This parser is capable of reading variables from {@link GenericScope} by using "$"!
 * {@link VariableConverter#parse(String, Object...)} required one additional Scope argument in args... at index 0!<br>
 * It also manages access member operator also known as separator <code>"."</code>.
 * Its case sensitive!<br>
 * Exact outputs of this converter are based on inserted scope!
 * 
 * @author PETO
 *	
 * @since 1.3.7
 */
public class VariableParser implements DataParser
{
	@SuppressWarnings("unchecked")
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		if (args.length > 0 && str.length() > 0 && args[0] instanceof GenericScope)
		{
			return parse(myHomeRegistry, str, (GenericScope<Object, Object>) args[0], args);
		}
		return CONTINUE;
	}
	
	/**
	 * @param source | Source object to get value of the member from (may or may not be null). Source should not be modified!
	 * @param member | Name/key of the member to get.
	 * 
	 * @return The value of member from given source. You can think about this as ekvivalent to <code>source.member</code> in Java. If member with provided name/key is not present in the source or its value is not possible to get, {@link VOID} has to be returned! If source can't be accessed/dereferenced, <code>null</code> has to be returned!<br>
	 * Note: This method is meant to be overridden in order to add support for accessing multiple sources because by default it supports only {@link GenericScope}
	 * 
	 * @since 1.3.7
	 */
	@SuppressWarnings("unchecked")
	public Object getMemberOperator(Object source, Object member)
	{
		if (source instanceof GenericScope)
			return ((GenericScope<?, Object>) source).variables().getOrDefault(member, VOID);
		return null;
	}

	/**
	 * @param myHomeRegistry | Registry where this parser is registered provided by {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)} otherwise it demands on implementation (it should not be null)!
	 * @param str | Source string, should not be null or empty (preferably with some variables to read)!
	 * @param scope | Source scope to read from, can't be null!
	 * @param args | Some additional args. This can be anything and it demands on implementation of DataParser.
	 * 
	 * @return Value of variable read from scope is str was suitable. Special return types are {@link DataParser#VOID} and {@link DataParser#CONTINUE}. Continue will ignore this parser and jump to another one in registry.
	 * 
	 * @since 1.3.7
	 */
	protected Object parse(ParserRegistry myHomeRegistry, String str, GenericScope<?, Object> scope, Object... args)
	{
		if (str.charAt(0) == '$' && !contains(str = str.substring(1), ' ', '+', '-', '*', '/', '%', '>', '<', '=', '&', '|', '^', '?', '='))
		{
			boolean clsModif = str.endsWith("::class"), newModif = false; // Handle modifiers...
			if (clsModif)
				str = str.substring(0, str.length()-7);
			else if (newModif = str.endsWith("::new"))
				str = str.substring(0, str.length()-5);
			
			Object obj = null;
			if (str.indexOf('.') > -1)
			{
				String[] path = splitValues(str, '.');
				int iLast = path.length-1;
				
				backlook: do 
				{
					Object sc;
					if ((sc = getMemberOperator(scope, path[0])) != VOID) // Attempt to get only when exists...
					{
						for (int i = 1; i < iLast; i++) // Subscope/forward lookup (inner path only)...
							if ((sc = getMemberOperator(sc, path[i])) == null || sc == VOID)
							{
								// LogProvider.instance.logErr("Value of path \"" + arg + "\" cannot be dereferenced because \"" + path[i] + "\" is not a scope but " + sc + "!", null);
								break backlook;
							}
						
						obj = getMemberOperator(sc, path[iLast]);
						break;
					}
				}
				while ((scope = scope.getParent()) != null);
			}
			else
			{
				do 
				{
					if ((obj = scope.variables().getOrDefault(str, VOID)) != VOID)
						break;
				}
				while ((scope = scope.getParent()) != null);
			}

			if (obj == null || obj == VOID) // When was not found...
				return null;
			return clsModif ? obj.getClass() : newModif ? Clone(obj, scope instanceof Serializer ? ((Serializer) scope).getParsers() : DataParser.REGISTRY, new Object[0], new Scope()) : obj;
		}

		return CONTINUE;
	}
}
