package org.ugp.serialx.protocols;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ugp.serialx.LogProvider;
import org.ugp.serialx.Registry;
import org.ugp.serialx.Serializer;

/**
 * This class supposed to be used for serializing and unserializing objects in Java by turning them in to array of Objects that can be serialized by {@link Serializer} or used by some higher entities. Also this class is capable of turning array of these objects back in to that very same Object! <br>
 * {@link SerializationProtocol#unserialize(Class, Object...)} and {@link SerializationProtocol#serialize(Object)} are used for this conversion.
 * Instance of SerializationProtocol should be registered into {@link SerializationProtocol#REGISTRY} in order to work, also only one instance of each SerializationProtocol should be used and accessed via this registry! <br>
 * Note: Protocols should not be serializable in any way!<br>
 * 
 * @author PETO
 *
 * @param <T> | Generic type of object to use protocol for.
 * 
 * @since 1.0.0
 */
public abstract class SerializationProtocol<T>
{
	/**
	 * This is serialization protocol registry. This is place where your {@link SerializationProtocol} implementations should be registered in order to work properly! Do not add there two protocols applicable for exactly same classes!
	 * Also I recommend to register protocols in generic order of object that are they applicable for! In other words If some object Foo has child classes, protocol of these classes should be registered after each other.
	 * Defaultly there are registered protocols from ugp.org.SerialX.protocols.
	 * 
	 * @since 1.3.0
	 */
	public static final ProtocolRegistry REGISTRY = new ProtocolRegistry(/*This might be unsafe: new UniversalObjectInstantiationProtocol<>(Object.class), new SelfSerializableProtocol(SelfSerializable.class),*/ new ListProtocol(), new MapProtocol(), new StringProtocol(), new ScopeProtocol(), new EnumProtocol());
	
	/**
	 * This mode is for protocols that are used for serialization only!
	 * 
	 * @since 1.3.5
	 */
	public static final byte MODE_SERIALIZE = 0;
	
	/**
	 * This mode is for protocols that are used for deserialization only!
	 * 
	 * @since 1.3.5
	 */
	public static final byte MODE_DESERIALIZE = 1; 
	
	/**
	 * This mode is for protocols that chat can both serialize and deserialize!
	 * 
	 * @since 1.3.5
	 */
	public static final byte MODE_ALL = 2;
	
	protected boolean isActive = true;
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof Class)
			return applicableFor().equals(obj);
		else if (obj instanceof SerializationProtocol)
			return applicableFor().equals(((SerializationProtocol<?>) obj).applicableFor());
		return super.equals(obj);
	}
	
	@Override
	public String toString() 
	{
		return getClass().getName() + "[" + applicableFor().getName() + "]";
	}
	
	/**
	 * @param object | The object.
	 * 
	 * @return Array of objects to serialize created from given object.
	 * 
	 * @since 1.0.0
	 */
	public abstract Object[] serialize(T object) throws Exception;
	
	/*/**
	 * @param object | The object.
	 * @param scope | Scope to use!
	 * 
	 * @return Scope of object to serialize created from given object.
	 * Note: Do not use WORK IN PROGRESS!
	 * 
	 * @since 1.2.2
	 *
	public Scope serializeAsScope(T object) 
	{
		return null;
	}*/
	
	/**
	 * @param objectClass | The class of object that should be created. This can be useful when object {@link T} has children classes with same constructors. You can use reflection to make protocol working also for these child classes!
	 * @param args | Args to create obj {@link T} from.
	 * 
	 * @return New instance of object {@link T} created from args. This supposed to be an instance of object, not null!
	 * 
	 * @throws Exception you can just pass exception to Serializer if you are not interested in handling it on your own.
	 * 
	 * @since 1.0.0
	 */
	public abstract T unserialize(Class<? extends T> objectClass, Object... args) throws Exception;
	
	/*/**
	 * @param objectClass | The class of object that should be created. This can be useful when object {@link T} has children classes with same constructors. You can use reflection to make protocol working also for these child classes!
	 * @param scope | Scope with data to create {@link T} from.
	 * 
	 * @return New instance of object {@link T} created from scopeOfArgs.
	 * 
	 * @since 1.2.0
	 *
	public T unserialize(Class<? extends T> objectClass, Scope scope) throws Exception
	{
		return null;
	}*/
	
	/**
	 * @return Class that can be serialized using this protocol.
	 * In other words it should return class of object {@link T}
	 * 
	 * @since 1.0.0
	 */
	public abstract Class<? extends T> applicableFor();
	
	/**
	 * @return Mode of this protocol. Default is {@link SerializationProtocol#MODE_ALL}!
	 *
	 * @see SerializationProtocol#MODE_ALL
	 * @see SerializationProtocol#MODE_DESERIALIZE
	 * @see SerializationProtocol#MODE_SERIALIZE
	 * 
	 * @since 1.3.5
	 */
	public byte getMode()
	{
		return MODE_ALL;
	}

	/**
	 * @return True if this protocol is active.
	 * Only active protocols can be used!
	 * 
	 * @since 1.0.0
	 */
	public boolean isActive() 
	{
		return isActive;
	}

	/**
	 * @param isActive | Set this on true to activate this protocol or set this on false to deactivate it so it will not be used. 
	 * This can be useful for example when you want to force program to serialize java.io.Serializable object using classic java.io.Serializable.
     * For example when you deactivate {@link StringProtocol} program will be forced to serialize all Strings via java.io.Serializable.
     * 
     * @since 1.0.0
	 */
	public void setActive(boolean isActive) 
	{
		this.isActive = isActive;
	}
	
	/**
	 * @param object | The object.
	 * 
	 * @return Array of objects to serialize created from given object. Object will be serialized via protocol picked from {@link SerializationProtocol#REGISTRY}.
	 * {@link SerializationProtocol#serialize(Class, Object...)} method of picked protocol will be called! Null will be returned if no protocol was found and you will be prompted with error message!
	 * 
	 * @since 1.3.0
	 */
	public static <O> Object[] serializeObj(O object) throws Exception
	{
		return serializeObj(REGISTRY, object);
	}
	
	/**
	 * @param registry | Registry to use!
	 * @param object | The object.
	 * 
	 * @return Array of objects to serialize created from given object. Object will be serialized via protocol picked from registry.
	 * {@link SerializationProtocol#serialize(Class, Object...)} method of picked protocol will be called! Null will be returned if no protocol was found and you will be prompted with error message!
	 * 
	 * @since 1.3.0
	 */
	public static <O> Object[] serializeObj(ProtocolRegistry registry, O object) throws Exception
	{
		SerializationProtocol<O> prot = registry.GetProtocolFor(object, MODE_SERIALIZE);
		if (prot == null)
		{
			LogProvider.instance.logErr("Unable to serialize \"" + object + "\" because there is no registered and active protocol for serializing " + object.getClass() + "!", null);
			return null;
		}
		return prot.serialize(object);
	}	
	
	/**
	 * @param objectClass | The class of object that should be created. This can be useful when object {@link O} has children classes with same constructors. You can use reflection to make protocol working also for these child classes! This class is also used to pick suitable protocol!
	 * @param args | Args to create obj {@link T} from.
	 * 
	 * @return New instance of object {@link T} created from args. Object will be created via protocol picked from {@link SerializationProtocol#REGISTRY}. If there is no protocol registered for objectClass null will be returned and you will be prompted in console!
	 * {@link SerializationProtocol#unserialize(Class, Object...)} method of picked protocol will be called!
	 * 
	 * @throws Exception when exception occurs while unserializing object using protocol!
	 * 
	 * @since 1.3.0
	 */
	public static <O> O unserializeObj(Class<? extends O> objectClass, Object... args) throws Exception 
	{
		return unserializeObj(REGISTRY, objectClass, args);
	}
	
	/**
	 * @param registry | Registry to use!
	 * @param objectClass | The class of object that should be created. This can be useful when object {@link O} has children classes with same constructors. You can use reflection to make protocol working also for these child classes! This class is also used to pick suitable protocol!
	 * @param args | Args to create obj {@link T} from.
	 * 
	 * @return New instance of object {@link T} created from args. Object will be created via protocol picked from registry. If there is no protocol registered for objectClass null will be returned and you will be prompted in console!
	 * {@link SerializationProtocol#unserialize(Class, Object...)} method of picked protocol will be called!
	 * 
	 * @throws Exception when exception occurs while unserializing object using protocol!
	 * 
	 * @since 1.3.0
	 */
	@SuppressWarnings("unchecked")
	public static <O> O unserializeObj(ProtocolRegistry registry, Class<? extends O> objectClass, Object... args) throws Exception 
	{
		SerializationProtocol<O> prot = (SerializationProtocol<O>) registry.GetProtocolFor(objectClass, MODE_DESERIALIZE);
		if (prot == null)
		{
			LogProvider.instance.logErr("Unable to unserialize " + Arrays.toString(args) + " because there is no registered and active protocol for unserializing \"" + objectClass + "\"!", null);
			return null;
		}
		return (O) prot.unserialize(objectClass, args);
	}
	
	/**
	 * ProtocolRegistry, place to register protocols!
	 * 
	 * @author PETO
	 *
	 * @since 1.0.0 (moved to SerializationProtocol since 1.3.0)
	 */
	public static class ProtocolRegistry extends Registry<SerializationProtocol<?>>
	{
		private static final long serialVersionUID = 1L;
		
		/**
		 * Constructs an {@link ProtocolRegistry} with the specified initial capacity.
		 * 
		 * @param initialSize | Initial capacity.
		 * 
		 * @since 1.3.5
		 */
		public ProtocolRegistry(int initialSize) 
		{
			super(initialSize);
		}
		
		/**
		 * Constructs an {@link ProtocolRegistry} with content of c.
		 * 
		 * @param c | Initial content of registry.
		 * 
		 * @since 1.3.5
		 */
		public ProtocolRegistry(Collection<? extends SerializationProtocol<?>> c) 
		{
			super(c);
		}
		
		/**
		 * Constructs an {@link ProtocolRegistry} with protocols.
		 * 
		 * @param protocols | Initial content of registry.
		 * 
		 * @since 1.3.0
		 */
		public ProtocolRegistry(SerializationProtocol<?>... protocols) 
		{
			super(protocols);
		}
		
		@Override
		public ProtocolRegistry clone()
		{
			return new ProtocolRegistry(this);
		}

		@Override
		public void add(int index, SerializationProtocol<?> element) 
		{
			if (GetProtocolFor(element.applicableFor()) != null && element.applicableFor() != Object.class)
				LogProvider.instance.logErr("Protocol applicable for \"" + element.applicableFor().getName() + "\" is already registred!", null);
			addDuplicatively(index, element);
		}
		
		/**
		 * @param protocolsClass | Protocols class.
		 * 
		 * @return Protocol by its class.
		 * 
		 * @since 1.0.0
		 */
		public SerializationProtocol<?> getProtocolByClass(Class<? extends SerializationProtocol<?>> protocolsClass)
		{
			for (SerializationProtocol<?> p : this) 
				if (p.getClass().equals(protocolsClass))
					return p;
			return null;
		}
		
		/**
		 * @return Sublist of protocols that are active and can be used.
		 * 
		 * @since 1.0.0
		 */
		public List<SerializationProtocol<?>> GetActiveProtocols()
		{
			return GetActiveProtocols(MODE_ALL);
		}	
		
		/**
		 * @return Sublist of protocols that are not active and can't be used.
		 * 
		 * @since 1.0.0
		 */
		public List<SerializationProtocol<?>> GetDeactivatedProtocols()
		{
			return GetDeactivatedProtocols(MODE_ALL);
		}
		
		/**
		 * @param mode | Mode of protocol to find.
		 * 
		 * @return Sublist of protocols that are active and can be used according to mode.
		 * 
		 * @since 1.3.5
		 */
		public List<SerializationProtocol<?>> GetActiveProtocols(byte mode)
		{
			List<SerializationProtocol<?>> resault = new ArrayList<>();
			
			for (SerializationProtocol<?> p : this) 
				if (p.isActive() && (p.getMode() == 2 || p.getMode() == mode))
					resault.add(p);
			return resault;
		}	
		
		/**
		 * @param mode | Mode of protocol to find.
		 * 
		 * @return Sublist of protocols that are not active and can't be used according to mode.
		 * 
		 * @since 1.3.5
		 */
		public List<SerializationProtocol<?>> GetDeactivatedProtocols(byte mode)
		{
			List<SerializationProtocol<?>> resault = new ArrayList<>();
			
			for (SerializationProtocol<?> p : this) 
				if (!p.isActive() && (p.getMode() == 2 || p.getMode() == mode))
					resault.add(p);
			return resault;
		}
		
		/**
		 * @param <O>
		 * @param obj | Object that is required protocol applicable for.
		 * 
		 * @return Protocol applicable for required objects class or null if there is no such an active protocol!
		 * 
		 * @since 1.0.5
		 */
		public <O> SerializationProtocol<O> GetProtocolFor(O obj)
		{
			return GetProtocolFor(obj, MODE_ALL);
		}
		
		/**
		 * @param <O>
		 * @param applicableFor | Class of object that is protocol applicable for.
		 * 
		 * @return Protocol applicable for required class or null if there is no such an active protocol!
		 * 
		 * @since 1.0.0
		 */
		public <O> SerializationProtocol<O> GetProtocolFor(Class<? extends O> applicableFor)
		{
			return GetProtocolFor(applicableFor, MODE_ALL);
		}
		
		/**
		 * @param <O>
		 * @param obj | Object that is required protocol applicable for.
		 * @param mode | Mode of protocol to find.
		 * 
		 * @return Protocol applicable for required objects class according to mode or null if there is no such an active protocol!
		 * 
		 * @since 1.3.5
		 */
		@SuppressWarnings("unchecked")
		public <O> SerializationProtocol<O> GetProtocolFor(O obj, byte mode)
		{
			return (SerializationProtocol<O>) GetProtocolFor(obj.getClass(), mode);
		}
		
		/**
		 * @param <O>
		 * @param applicableFor | Class of object that is protocol applicable for.
		 * @param mode | Mode of protocol to find.
		 * 
		 * @return Protocol applicable for required class according to mode or null if there is no such an active protocol!
		 * 
		 * @since 1.3.5
		 */
		@SuppressWarnings("unchecked")
		public <O> SerializationProtocol<O> GetProtocolFor(Class<? extends O> applicableFor, byte mode)
		{
			SerializationProtocol<O> protocol = null;
			for (int i = 0, len = size(), myMode = 0; i < len; i++) 
			{
				SerializationProtocol<?> p = get(i);
				if (p.isActive() && ((myMode = p.getMode()) == 2 || myMode == mode))
				{
					Class<?> applicable = p.applicableFor();
					if (applicable == applicableFor)
						return (SerializationProtocol<O>) p;
					else if (applicable.isAssignableFrom(applicableFor))
						protocol = (SerializationProtocol<O>) p;
				}
			}
			return protocol;
		}
		
		/**
		 * @param isActive | Activity state for all registered protocols.
		 * 
		 * @since 1.0.0
		 */
		public void setActivityForAll(boolean isActive)
		{
			for (SerializationProtocol<?> p : this) 
				p.setActive(isActive);
		}
	}
}
