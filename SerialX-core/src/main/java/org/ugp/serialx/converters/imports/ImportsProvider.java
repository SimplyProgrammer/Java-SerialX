package org.ugp.serialx.converters.imports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.ugp.serialx.Scope;
import org.ugp.serialx.Serializer;

/**
 * This interface is supposed to be implemented by class that can provide array of imports!
 * 
 * @author PETO
 *
 * @since 1.3.5
 */
public interface ImportsProvider
{
	/**
	 * Cache of Classes to their respective names/aliases.<br>
	 * Note: Treat as read only if possible!
	 * 
	 * @since 1.3.8
	 */
	public static final Map<String, Class<?>> CLASS_CACHE = new HashMap<String, Class<?>>();
	
	/**
	 * List of global shared common registered imports!
	 * 
	 * @since 1.3.0
	 */
	public static final Imports IMPORTS = new Imports(new Import(Serializer.class), new Import(Scope.class), new Import(ArrayList.class), new Import(Math.class), new Import(Double.class), new Import(Integer.class), new Import(Double.class, "double"), new Import(Integer.class, "int"), new Import(String.class), new Import(System.class));
	
	/**
	 * @return Array of provided imports!
	 * 
	 * @since 1.3.5
	 */
	Imports getImports();
	
	/**
	 * @param obj | Object to get imports from (its supposed to be instance of {@link ImportsProvider} or collection of {@link Import})!
	 * 
	 * @return Imports of object or null if object can't provide any!
	 * 
	 * @since 1.3.5
	 */
	@SuppressWarnings("unchecked")
	public static Imports importsOf(Object obj)
	{
		if (obj == null)
			return null;
		
		if (obj instanceof ImportsProvider)
			return ((ImportsProvider) obj).getImports();
			
		try 
		{
			return new Imports((Collection<Import>) obj);
		} 
		catch (Exception e) 
		{
			return null;
		}
	}
	
	/**
	 * @param importProvider | Object to get imports from!
	 * @param aliasOrName | Alias of class or its full name!
     * 
	 * @return Class with inserted alias picked from {@link Imports#IMPORTS} similar to {@link Imports#getClassFor(String)} but this will also search via {@link Class#forName(String, boolean, ClassLoader)} if there is no import with required alias!
	 * 
	 * @throws ClassNotFoundException when {@link Class#forName(String, boolean, ClassLoader)} throws!
	 * 
	 * @since 1.3.5
	 * 
	 * @see ImportsProvider#importsOf(Object)
	 */
	public static Class<?> forName(Object importProvider, String aliasOrName) throws ClassNotFoundException
	{
		return forName(importProvider, aliasOrName, true, Imports.class.getClassLoader());
	}
	
	/**
	 * @param importProvider | Object to get imports from!
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
	 * 
	 * @see ImportsProvider#importsOf(Object)
	 */
	public static Class<?> forName(Object importProvider, String aliasOrName, boolean initialize, ClassLoader loader) throws ClassNotFoundException
	{
		Class<?> cls = getClassFor(importProvider, aliasOrName);
		if (cls != null)
			return cls;
		
	    if ((cls = CLASS_CACHE.get(aliasOrName)) != null) 
	    	 return cls;
		if (aliasOrName.indexOf('.') > 0) 
		{
			cls = Class.forName(aliasOrName, initialize, loader);
			CLASS_CACHE.put(aliasOrName, cls);
			return cls;
		}

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
	 * @param importProvider | Object to get imports from!
	 * @param cls | Class to get alias for!
	 * 
	 * @return Alias of class picked from {@link Imports#IMPORTS} or name of class if there is no import for this class!
	 * 
	 * @since 1.3.5
	 * 
	 * @see ImportsProvider#importsOf(Object)
	 */
	public static String getAliasFor(Object importProvider, Class<?> cls)
	{
		Imports imports = importsOf(importProvider);
		return imports != null ? imports.getAliasFor(cls) : cls.getName();
	}
	
	/**
	 * @param importProvider | Object to get imports from!
	 * @param alias | Alias of class to obtain!
	 * 
	 * @return Class with inserted alias picked from {@link Imports#IMPORTS} or null if there is no import with required alias!
	 * 
	 * @since 1.3.5
	 * 
	 * @see ImportsProvider#importsOf(Object)
	 */
	public static Class<?> getClassFor(Object importProvider, String alias)
	{
		Imports imports = importsOf(importProvider);
		return imports != null ? imports.getClassFor(alias) : null;
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
		 * @param imports | Initial content of registry.
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

		    if ((cls = CLASS_CACHE.get(aliasOrName)) != null) 
		    	 return cls;
			if (aliasOrName.indexOf('.') > 0) 
			{
				cls = Class.forName(aliasOrName, initialize, loader);
				CLASS_CACHE.put(aliasOrName, cls);
				return cls;
			}
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