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
 * {@link VariableConverter#parse(String, Object...)} required one additional Scope argument in args... at index 0!
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
	 * @param myHomeRegistry | Registry where this parser is registered provided by {@link DataParser#parseObj(Registry, String, boolean, Class[], Object...)} otherwise it demands on implementation (it should not be null)!
	 * @param str | Source string (preferably with some variables to read)!
	 * @param scope | Source scope to read from, can't be null!
	 * @param args | Some additional args. This can be anything and it demands on implementation of DataParser.
	 * 
	 * @return Value of variable read from scope is str was suitable. Special return types are {@link DataParser#VOID} and {@link DataParser#CONTINUE}. Continue will ignore this parser and jump to another one in registry.
	 * 
	 * @since 1.3.7
	 */
	@SuppressWarnings("unchecked")
	protected Object parse(ParserRegistry myHomeRegistry, String str, GenericScope<?, Object> scope, Object... args) 
	{
		if (str.charAt(0) == '$' && !contains(str, ' ', '+', '-', '*', '/', '%', '>', '<', '=', '&', '|', '^', '?', '='))
		{

			boolean clsModif = str.endsWith("::class"), newModif = false; // Handle modifiers...
			if (clsModif)
				str = str.substring(0, str.length()-7);
			else if (newModif = str.endsWith("::new"))
				str = str.substring(0, str.length()-5);
			
			Object obj = null;
			if ((str = str.substring(1)).indexOf('.') > -1)
			{
				Object[] path = splitValues(str, '.');
				int iLast = path.length-1;
				
				backlook: do 
				{
					Object sc = scope.variables().getOrDefault(path[0], VOID);
					if (sc instanceof GenericScope) // The first one has to be scope!
					{
						for (int i = 1; i < iLast; i++) // Subscope/forward lookup...
							if (!((sc = ((GenericScope<Object, ?>) sc).get(path[i])) instanceof GenericScope))
							{
								// LogProvider.instance.logErr("Value of path \"" + arg + "\" cannot be dereferenced because \"" + path[i] + "\" is not a scope but " + sc + "!", null);
								break backlook;
							}
						
						obj = ((GenericScope<?, Object>) sc).variables().get(path[iLast]);
						break;
					}
					
					if (sc != VOID) // = variable was defined in parent but it is not a scope, it means we want to break cos we can't deref that = treat the path as invalid (undefined)...
					{
						// LogProvider.instance.logErr("Value of path \"" + arg + "\" cannot be dereferenced because \"" + path[0] + "\" is not a scope but " + sc + "!", null);
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
			return clsModif ? obj.getClass() : newModif ? Clone(obj, scope instanceof Serializer ? ((Serializer) scope).getParsers() : DataParser.REGISTRY, new Object[] {}, new Scope()) : obj;
		}
		return CONTINUE;
	}
}
