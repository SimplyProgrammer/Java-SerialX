package org.ugp.serialx.converters.imports;

import java.util.Collection;

import org.ugp.serialx.converters.imports.ImportConverter.Imports;

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
	 * @return Array of provided imports!
	 * 
	 * @since 1.3.5
	 */
	public Imports getImports();
	
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
}