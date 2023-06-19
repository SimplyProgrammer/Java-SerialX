package org.ugp.serialx.converters.imports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.DataConverter;
import org.ugp.serialx.converters.DataParser;

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
	/**
	 * List of global shared common registered imports!
	 * 
	 * @since 1.3.0
	 */
	public static final Imports IMPORTS = new Imports(new Import(Serializer.class), new Import(Scope.class), new Import(ArrayList.class), new Import(Math.class), new Import(Double.class), new Import(Integer.class), new Import(Double.class, "double"), new Import(Integer.class, "int"), new Import(String.class), new Import(System.class));
	
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
	
	/**
	 * Collection used to store and operate with {@link Import}!
	 * 
	 * @author PETO
	 *
	 * @since 1.3.5
	 * 
	 * @see ImportsProvider
	 */
	public static class Imports extends ArrayList<Import> implements Cloneable, ImportsProvider
	{
		private static final long serialVersionUID = 8487976264622823940L;
		
		/**
		 * Constructs an {@link Imports} with the specified initial capacity.
		 * 
		 * @param initialSize | Initial capacity.
		 * 
		 * @since 1.3.5
		 */
		public Imports(int initialSize) 
		{
			super(initialSize);
		}
		
		/**
		 * Constructs an {@link Imports} with inserted imports c.
		 * 
		 * @param c | Initial content of registry.
		 * 
		 * @since 1.3.5
		 */
		public Imports(Collection<? extends Import> c) 
		{
			super(c);
		}
		
		/**
		 * Constructs an {@link Imports} with inserted imports.
		 * 
		 * @param parsers | Initial content of registry.
		 * 
		 * @since 1.3.5
		 */
		public Imports(Import... imports) 
		{
			addAll(imports);
		}
		
		@Override
		public Imports clone()
		{
			return new Imports(this);
		}
		
		@Override
		public Imports getImports() 
		{
			return this;
		}
		
		/**
		 * @param imports | Imports to add.
		 * 
		 * @return {@link ArrayList#addAll(Collection)};
		 * 
		 * @since 1.3.5
		 */
		public boolean addAll(Import... imports) 
		{
			return super.addAll(Arrays.asList(imports));
		}
		
		/**
		 * @param aliasOrName | Alias of class or its full name!
	     * 
		 * @return Class with inserted alias picked from {@link Imports#IMPORTS} similar to {@link Imports#getClassFor(String)} but this will also search via {@link Class#forName(String, boolean, ClassLoader)} if there is no import with required alias!
		 * 
		 * @throws ClassNotFoundException when {@link Class#forName(String, boolean, ClassLoader)} throws!
		 * 
		 * @since 1.3.5
		 */
		public Class<?> forName(String aliasOrName) throws ClassNotFoundException
		{
			return forName(aliasOrName, true, Imports.class.getClassLoader());
		}
		
		/**
		 * @param aliasOrName | Alias of class or its full name!
		 * @param initialize | If true the class will be initialized. See Section 12.4 of <i>The Java Language Specification</i>.
	     * @param loader | Class loader from which the class must be loaded.
	     * 
		 * @return Class with inserted alias picked from {@link Imports#IMPORTS} similar to {@link Imports#getClassFor(String)} but this will also search via {@link Class#forName(String, boolean, ClassLoader)} if there is no import with required alias!
		 * If there are no dots in required alias and alias is not imported then null will be returned!
		 * 
		 * @throws ClassNotFoundException when {@link Class#forName(String, boolean, ClassLoader)} throws!
		 * 
		 * @since 1.3.5
		 */
		public Class<?> forName(String aliasOrName, boolean initialize, ClassLoader loader) throws ClassNotFoundException
		{
			Class<?> cls = getClassFor(aliasOrName);
			if (cls != null)
				return cls;
			if (aliasOrName.indexOf('.') > 0)
				return Class.forName(aliasOrName, initialize, loader);
			/*try
			{
				return Class.forName("java.lang."+aliasOrName, initialize, loader);
			}
			catch (Exception e)
			{
				return null;
			}*/
			return null;
		}
		
		/**
		 * @param cls | Class to get alias for!
		 * 
		 * @return Alias of class picked from {@link Imports#IMPORTS} or name of class if there is no import for this class!
		 * 
		 * @since 1.3.5
		 */
		public String getAliasFor(Class<?> cls)
		{
			for (int i = size() - 1; i >= 0; i--)
			{
				Import imp = get(i);
				if (imp.equals(cls))
					return imp.getClsAlias();
			}
			return cls.getName();
		}
		
		/**
		 * @param alias | Alias of class to obtain!
		 * 
		 * @return Class with inserted alias picked from {@link Imports#IMPORTS} or null if there is no import with required alias!
		 * 
		 * @since 1.3.5
		 */
		public Class<?> getClassFor(String alias)
		{
			for (int i = size() - 1; i >= 0; i--)
			{
				Import imp = get(i);
				if (imp.equals(alias))
					return imp.getCls();
			}
			return null;
		}
		
		/**
		 * @param owner | Owner of imports to get!
		 *  
		 * @return Imports of provided owner stored by this list!
		 * 
		 * @since 1.3.5
		 */
		public Imports importsOf(ImportsProvider owner)
		{
			Imports imports = new Imports();
			for (Import imp : imports) 
				if (imp.getOwner() == owner)
					imports.add(imp);
			return imports;
		}
		
		/**
		 * @param owner | Owner of imports to remove from this list!
		 * 
		 * @since 1.3.5
		 */
		public void removeImportsOf(ImportsProvider owner)
		{
			for (int i = size() - 1; i >= 0; i--)
				if (get(i).getOwner() == owner)
					remove(i);
		}
	}
}