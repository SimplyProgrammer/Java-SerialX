package org.ugp.serialx.converters.imports;

import java.lang.reflect.Type;

import org.ugp.serialx.Serializer;
import org.ugp.serialx.converters.imports.ImportsProvider.Imports;

/**
 * This class is represents single import. It stores target class of import and its alias! <br>
 * This imports are stored and managed in {@link Imports} and are used by many parsers and {@link Serializer}!
 * 
 * @author PETO
 *
 * @since 1.3.0
 * 
 * @see Import
 */
public class Import implements Cloneable, Type
{
	protected final Class<?> cls;
	protected final String alias;
	protected final ImportsProvider owner;
	
	/**
	 * @param cls | Class to create import for! Alias of this class will be its simple name!
	 * 
	 * @since 1.3.0
	 */
	public Import(Class<?> cls) 
	{
		this(cls, cls.getSimpleName());
	}
	
	/**
	 * @param cls | Class to create import for! Alias of this class will be its simple name!
	 * @param owner | Owner/provider of this Import!
	 * 
	 * @since 1.3.5
	 */
	public Import(Class<?> cls, ImportsProvider owner) 
	{
		this(cls, cls.getSimpleName(), owner);
	}
	
	/**
	 * @param cls | Class to create import for! 
	 * @param alias | Alias of class!
	 * 
	 * @since 1.3.5
	 */
	public Import(Class<?> cls, String alias) 
	{
		this(cls, alias, null);
	}
	
	
	/**
	 * @param cls | Class to create import for! 
	 * @param alias | Alias of class!
	 * @param owner | Owner/provider of this Import!
	 * 
	 * @throws IllegalArgumentException | When alias is invalid! Alias should follow java class naming conventions without necessity of package specification!
	 * 
	 * @since 1.3.5
	 */
	public Import(Class<?> cls, String alias, ImportsProvider owner) throws IllegalArgumentException
	{
		this.cls = cls;
		
		if (alias.isEmpty())
			throw new IllegalArgumentException("Import alias cant be empty! Try \"" + cls.getSimpleName() + "\" instead!");
		int ch0 = alias.charAt(0);
		if (ch0 == '_' || (ch0 = ch0 | ' ') >= 'a' && ch0 <= 'z')
		{
			this.alias = alias;
			this.owner = owner;
		}
		else
			throw new IllegalArgumentException("Import alias \"" + alias + "\" is illegal! Alias cant begin with special char or number! Try \"_" + alias + "\" instead!");
	}
	
	@Override
	public String toString() 
	{
		if (getCls().getSimpleName().equals(getClsAlias()))
			return "import " + getCls().getName();
		return getCls().getName() + " => " + getClsAlias();
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof String)
			return getClsAlias().equals(obj);
		else if (obj instanceof Class)
			return getCls().equals(obj);
		else if (obj instanceof Import)
			return getClsAlias().equals(((Import) obj).getClsAlias()) && getCls().equals(((Import) obj).getCls());
		return super.equals(obj);
	}
	
	@Override
	public Import clone()  
	{
		return new Import(getCls(), getClsAlias(), getOwner());
	}
	
	@Override
	public String getTypeName() 
	{
		return getClsAlias();
	}
	
	public Import clone(ImportsProvider newOwner)  
	{
		return new Import(getCls(), getClsAlias(), newOwner);
	}
	
	/**
	 * @return Class of this import!
	 * 
	 * @since 1.3.0
	 */
	public Class<?> getCls() 
	{
		return cls;
	}

	/**
	 * @return Alias for class!
	 * 
	 * @since 1.3.0
	 */
	public String getClsAlias() 
	{
		return alias;
	}

	/**
	 * @return Owner/provider of this import!
	 * 
	 * @since 1.3.5
	 */
	public ImportsProvider getOwner() 
	{
		return  owner;
	}
}