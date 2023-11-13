package org.ugp.serialx.juss.converters;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;
import org.ugp.serialx.converters.imports.Import;
import org.ugp.serialx.converters.imports.ImportsProvider;
import org.ugp.serialx.converters.imports.ImportsProvider.Imports;

/**
 * This parser maintains list of {@link Imports#IMPORTS} represented as {@link Import}s. Where are registered imports imported by user as well as temporary imports that are parsed! Result of parsing will be always added to imports list and {@link DataParser#VOID} will be returned!
 * Parsing example: <code>import java.util.ArrayList</code> will add temporary {@link Import} of java.util.ArrayList or <code>java.lang.String => Word</code> will import java.lang.String as Word!
 * Imports will be converted to string just by calling toString!<br>
 * <br>
 * This parser requires additional parser arg at index 0 of type {@link ImportsProvider} that will obtain managed imports! This arg is required during both parsing and converting!
 * 
 * @author PETO
 *
 * @since 1.3.0
 */
public class ImportConverter implements DataConverter 
{	
	@Override
	public Object parse(ParserRegistry myHomeRegistry, String str, Object... args) 
	{
		if (args.length > 0 && args[0] instanceof ImportsProvider)
		{
			Imports imports = ((ImportsProvider) args[0]).getImports();
			int index;
			if (str.startsWith("import "))
			{
				try 
				{
					if ((str = str.substring(7).trim()).indexOf("=>") > -1)
						return parse(myHomeRegistry, str, args);
					imports.add(new Import(imports.forName(str), (ImportsProvider) args[0]));
				} 
				catch (ClassNotFoundException e) 
				{
					LogProvider.instance.logErr("Unable to import " + str + " because there is no such a class!", e);
				}
				return VOID;
			}
			else if ((index = (str = str.trim()).indexOf("=>")) > -1)
			{
				try 
				{
					imports.add(new Import(imports.forName(str.substring(0, index).trim()), str.substring(index+2).trim(), (ImportsProvider) args[0]));
				} 
				catch (ClassNotFoundException e) 
				{
					LogProvider.instance.logErr("Unable to import " + str.substring(0, index).trim() + " because there is no such a class!", e);
				}
				catch (Exception e2)
				{
					LogProvider.instance.logErr(e2.getMessage(), e2);
				}
				return VOID;
			}
		}
		return CONTINUE;
	}

	@Override
	public CharSequence toString(ParserRegistry myHomeRegistry, Object obj, Object... args) 
	{
		if (args.length > 0 && args[0] instanceof ImportsProvider && obj instanceof Import)
		{
			return obj.toString();
		}
		return CONTINUE;
	}
	
	@Override
	public String getDescription(ParserRegistry myHomeRegistry, Object objToDescribe, Object... argsUsedConvert) 
	{
		return "Import of " + ((Import) objToDescribe).getCls() + " as " + ((Import) objToDescribe).getClsAlias();
	}
}