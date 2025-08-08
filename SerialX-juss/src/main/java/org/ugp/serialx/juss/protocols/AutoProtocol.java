package org.ugp.serialx.juss.protocols;


import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ugp.serialx.GenericScope;
import org.ugp.serialx.Scope;
import org.ugp.serialx.protocols.SerializationProtocol;
import org.ugp.serialx.utils.Utils;

/**
 * This is automatic protocol that will automatically serialize every or selected field in object that has valid and public getter and setter!
 * This protocol is applicable on anything you want however condition of use is absence of final field otherwise {@link AutoProtocol#createBlankInstance(Class)} should be overridden. ALso you should {@link AutoProtocol#createBlankInstance(Class)} when you object is to complex!
 * 
 * @author PETO
 *
 * @param <T> | Generic type of object to use protocol for.
 * 
 * @since 1.2.2
 */
public class AutoProtocol<T> extends SerializationProtocol<T> 
{	
	static
	{
		UNIVERSAL_INTROSPECTION = Arrays.asList();
	}
	
	/**
	 * Universal instance of AutoProtocol that is applicable for any {@link Object}!
	 * 
	 * @since 1.3.5
	 * 
	 * @see AutoProtocol#UNIVERSAL_SCOPE
	 */
	public static final AutoProtocol<Object> UNIVERSAL = new AutoProtocol<>(Object.class, AutoProtocol.UNIVERSAL_INTROSPECTION);
	
	/**
	 * Universal instance of AutoProtocol that is applicable for any {@link Object}!
	 * Object will be serialized as {@link Scope}!
	 * 
	 * @since 1.3.5
	 */
	public static final AutoProtocol<Object> UNIVERSAL_SCOPE = new AutoProtocol<>(Object.class, true, AutoProtocol.UNIVERSAL_INTROSPECTION);
	
	/**
	 * This is information for {@link AutoProtocol} to always introspect structure of every object before that is being serialized or deserialized, which means all fields with public and valid getters and setters will be serialized for every object!
	 * <br>
	 * This is default behavior of {@link AutoProtocol} when no fields are specified in constructor!
	 * 
	 * @since 1.3.5
	 */
	public static final List<PropertyDescriptor> UNIVERSAL_INTROSPECTION;
	
	protected final Class<T> applicableFor;
	protected List<PropertyDescriptor> fieldDescriptors;
	protected HashMap<Class<?>, List<PropertyDescriptor>> cache = new HashMap<>();
	protected boolean useScope;
	
	/**
	 * @param applicableFor | Class that can be serialized using this protocol.
	 * @param fieldsToSerialize | Names of fields to serialize, if empty array is put there then all fields with public and valid getters and setters will be serialized!
	 * 
	 * @throws IntrospectionException when there are no field with valid and public getters and setters.
	 * 
	 * @since 1.2.2
	 */
	public AutoProtocol(Class<T> applicableFor, String... fieldsToSerialize) throws IntrospectionException
	{
		this(applicableFor, false, fieldsToSerialize);
	}
	
	/**
	 * @param applicableFor | Class that can be serialized using this protocol.
	 * @param fieldsToSerialize | PropertyDescriptor of fields to serialize, if null all fields width valid and public getters and setters will be serialized!
	 * 
	 * @since 1.3.5
	 */
	public AutoProtocol(Class<T> applicableFor, List<PropertyDescriptor> fieldsToSerialize)
	{
		this(applicableFor, false, fieldsToSerialize);
	}
	
	/**
	 * @param applicableFor | Class that can be serialized using this protocol.
	 * @param useScope | If true, objects will be serialized using scope which is longer but more readable!
	 * @param fieldsToSerialize | Names of fields to serialize, if this array is empty or null then all fields with public and valid getters and setters will be serialized!
	 * 
	 * @throws IntrospectionException when there are no fields with valid and public getters and setters.
	 * 
	 * @since 1.3.2
	 */
	public AutoProtocol(Class<T> applicableFor, boolean useScope, String... fieldsToSerialize) throws IntrospectionException
	{
		this(applicableFor, useScope, fieldsToSerialize == null || fieldsToSerialize.length == 0 ? UNIVERSAL_INTROSPECTION : Scope.getPropertyDescriptorsOf(applicableFor, fieldsToSerialize));
	}
	
	/**
	 * @param applicableFor | Class that can be serialized using this protocol.
	 * @param useScope | If true, objects will be serialized using scope which is longer but more readable!
	 * @param fieldsToSerialize | PropertyDescriptor of fields to serialize, if null {@link AutoProtocol#UNIVERSAL_INTROSPECTION} will take place!
	 * 
	 * @since 1.3.5
	 */
	public AutoProtocol(Class<T> applicableFor, boolean useScope, List<PropertyDescriptor> fieldsToSerialize)
	{
		this.applicableFor = applicableFor;
		setUseScope(useScope);
		this.fieldDescriptors = fieldsToSerialize == null ? UNIVERSAL_INTROSPECTION : fieldsToSerialize;
		
		if (fieldDescriptors == UNIVERSAL_INTROSPECTION)
			try 
			{
				Scope.getPropertyDescriptorsOf(applicableFor, cache);
			} 
			catch (IntrospectionException e)
			{}
	}
	
	/**
	 * @param objectClass | Class to create new instance of!
	 * 
	 * @return New blank instance of required class! When not override, it returns {@link Utils#Instantiate(Class)} 
	 * 
	 * @throws Exception if any exception occurs (based on implementation).
	 * 
	 * @since 1.2.2
	 */
	public T createBlankInstance(Class<? extends T> objectClass) throws Exception
	{
		return Utils.Instantiate(objectClass);
	}
	
	@Override
	public Object[] serialize(T object) throws Exception
	{
		List<PropertyDescriptor> fieldDescriptors = getFieldDescriptors();
		if (fieldDescriptors == UNIVERSAL_INTROSPECTION)
			fieldDescriptors = Scope.getPropertyDescriptorsOf(object.getClass(), cache);

		if (isUseScope())
			return new Object[] {Scope.from(object, fieldDescriptors)};

		int size = fieldDescriptors.size();
		Object[] args = new Object[size];
		for (int i = 0; i < size; i++) 
			args[i] = fieldDescriptors.get(i).getReadMethod().invoke(object);
		return args;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T unserialize(Class<? extends T> objectClass, Object... args) throws Exception 
	{
		List<PropertyDescriptor> fieldDescriptors = getFieldDescriptors();
		if (fieldDescriptors == UNIVERSAL_INTROSPECTION)
			fieldDescriptors = Scope.getPropertyDescriptorsOf(objectClass, cache);

		T obj = createBlankInstance(objectClass);
		if (isUseScope() && args.length == 1 && args[0] instanceof Scope)
		{
			return Scope.into(obj, (Scope) args[0], fieldDescriptors);
		}
		
		for (int i = 0, size = Math.min(fieldDescriptors.size(), args.length); i < size; i++)
		{
			Method setter = fieldDescriptors.get(i).getWriteMethod();
			Type expectedType = setter.getGenericParameterTypes()[0];
			setter.invoke(obj, args[i] instanceof GenericScope && expectedType != args[i].getClass() ? ((GenericScope<String, ?>) args[i]).toObject(expectedType) : args[i]);
		}
		return obj;
	}
	
	/**
	 * @return PropertyDescriptors of variables that are used by this protocol!<br>
	 * Note: I would recommend to tread this as read only and not modify anything that you are not sure of. This will ensure correct functionality of this protocol...
	 * 
	 * @since 1.3.2
	 */
	public List<PropertyDescriptor> getFieldDescriptors()
	{
		return this.fieldDescriptors;
	}

	@Override
	public Class<? extends T> applicableFor() 
	{
		return applicableFor;
	}

	/**
	 * @return If true, objects will be serialized using scope which is longer but more readable!
	 * 
	 * @since 1.3.2
	 */
	public boolean isUseScope() 
	{
		return useScope;
	}

	/**
	 * @param useScope | If true, objects will be serialized using scope which is longer but more readable!
	 * 
	 * @since 1.3.2
	 */
	public void setUseScope(boolean useScope) 
	{
		this.useScope = useScope;
	}
}
